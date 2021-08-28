package com.termux.x11;

import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.view.SurfaceView;
import android.annotation.SuppressLint;
import android.view.Gravity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.app.PendingIntent;
import android.content.Intent;
import android.view.KeyEvent;

import java.lang.Character;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

import com.termux.shared.terminal.io.extrakeys.ExtraKeyButton;
import com.termux.shared.terminal.io.extrakeys.ExtraKeysView;
import com.termux.shared.terminal.io.extrakeys.SpecialButton;

import static com.termux.shared.terminal.io.extrakeys.ExtraKeysConstants.PRIMARY_KEY_CODES_FOR_STRINGS;

public class TerminalExtraKeys implements ExtraKeysView.IExtraKeysView {

    private final View.OnKeyListener mEventListener;

    public TerminalExtraKeys(@NonNull View.OnkeyListener eventlistener) {
        mEventListener = eventlistener;
    }

    SurfaceView lorieView = findViewById(R.id.lorieView);

    private final KeyCharacterMap mVirtualKeyboardKeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
    static final String ACTION_START_PREFERENCES_ACTIVITY = "com.termux.x11.start_preferences_activity";

    @Override
    public void onExtraKeyButtonClick(View view, ExtraKeyButton buttonInfo, Button button) {
        if (buttonInfo.isMacro()) {
            String[] keys = buttonInfo.getKey().split(" ");
            boolean ctrlDown = false;
            boolean altDown = false;
            boolean shiftDown = false;
            boolean fnDown = false;
            for (String key : keys) {
                if (SpecialButton.CTRL.getKey().equals(key)) {
                    ctrlDown = true;
                } else if (SpecialButton.ALT.getKey().equals(key)) {
                    altDown = true;
                } else if (SpecialButton.SHIFT.getKey().equals(key)) {
                    shiftDown = true;
                } else if (SpecialButton.FN.getKey().equals(key)) {
                    fnDown = true;
                } else {
                    onTerminalExtraKeyButtonClick(view, key, ctrlDown, altDown, shiftDown, fnDown);
                    ctrlDown = false;
		    altDown = false;
		    shiftDown = false;
		    fnDown = false;
                }
            }
        } else {
            onTerminalExtraKeyButtonClick(view, buttonInfo.getKey(), false, false, false, false);
        }
    }

    protected void onTerminalExtraKeyButtonClick(View view, String key, boolean ctrlDown, boolean altDown, boolean shiftDown, boolean fnDown) {
        if (PRIMARY_KEY_CODES_FOR_STRINGS.containsKey(key)) {
            Integer keyCode = PRIMARY_KEY_CODES_FOR_STRINGS.get(key);
            if (keyCode == null) return;
            int metaState = 0;
            if (ctrlDown) metaState |= KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON;
            if (altDown) metaState |= KeyEvent.META_ALT_ON | KeyEvent.META_ALT_LEFT_ON;
            if (shiftDown) metaState |= KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON;
            if (fnDown) metaState |= KeyEvent.META_FUNCTION_ON;

            KeyEvent keyEvent = new KeyEvent(0, 0, KeyEvent.ACTION_UP, keyCode, 0, metaState);
	    mEventListener.onKey(lorieView, keyCode, keyEvent);

        } else {
            // not a control char
            key.codePoints().forEach(codePoint -> {
		KeyEvent[] events = mVirtualKeyboardKeyCharacterMap.getEvents(toChars(codePoint));
                    if (events != null) {
                        for (KeyEvent event : events) {
			    Integer keyCode = event.getKeyCode();
			    mEventListener.onKey(lorieView, keyCode, keyEvent);
			}
		    }
            });
        }
    }

    @Override
    public boolean performExtraKeyButtonHapticFeedback(View view, ExtraKeyButton buttonInfo, Button button) {
        return false;
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void onTerminalExtraKeyButtonClick(View view, String key, boolean ctrlDown, boolean altDown, boolean shiftDown, boolean fnDown) {
        if ("KEYBOARD".equals(key)) {

	    if (act.kbd!=null) {
	        act.kbd.requestFocus();
                KeyboardUtils.toggleKeyboardVisibility(act);
	    }

	} else if ("DRAWER".equals(key)) {
	     Intent preferencesIntent = new Intent(getApplicationContext(), LoriePreferences.class);
             preferencesIntent.setAction(ACTION_START_PREFERENCES_ACTIVITY);

	     PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
             PendingIntent pendingPreferencesIntent = PendingIntent.getActivity(getApplicationContext(), 0, preferencesIntent, 0);

        } else if ("PASTE".equals(key)) {

	    ClipboardManager clipboard = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = clipboard.getPrimaryClip();

	    if (clipData != null) {
            	CharSequence paste = clipData.getItemAt(0).coerceToText(mActivity);
            	if (!TextUtils.isEmpty(paste)) lorieView.paste(paste.toString());
            }

        } else {
            super.onTerminalExtraKeyButtonClick(view, key, ctrlDown, altDown, shiftDown, fnDown);
        }
    }

}
