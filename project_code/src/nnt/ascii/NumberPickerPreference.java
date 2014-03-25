package nnt.ascii;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

/**
 * Number picker preference.
 *
 * @author A. Knyazhev, "SoftInvent" (http://softinvent.ru).
 */
public class NumberPickerPreference extends DialogPreference {
    private static final int DEFAULT_VALUE = 6;
    private NumberPicker numberPicker;
    private int currentValue;
    private int minValue;
    private int maxValue;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.dlg_number_picker);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            currentValue = getPersistedInt(DEFAULT_VALUE);
        } else {
            currentValue = (Integer) defaultValue;
            persistInt(currentValue);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, DEFAULT_VALUE);
    }

    @Override
    protected View onCreateDialogView() {
        View view = super.onCreateDialogView();
        numberPicker = (NumberPicker) view.findViewById(R.id.numberPicker);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setMinValue(minValue);
        numberPicker.setMaxValue(maxValue);
        numberPicker.setValue(currentValue);
        return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            currentValue = numberPicker.getValue();
            persistInt(currentValue);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState savedState = new SavedState(superState);
        savedState.value = currentValue;
        savedState.minValue = minValue;
        savedState.maxValue = maxValue;
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        currentValue = savedState.value;
        minValue = savedState.minValue;
        maxValue = savedState.maxValue;

        if (numberPicker != null) {
            numberPicker.setValue(currentValue);
            numberPicker.setMinValue(minValue);
            numberPicker.setMaxValue(maxValue);
        }
    }

    public int getValue() {
        return currentValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }


    private static class SavedState extends BaseSavedState {
        int value;
        int minValue;
        int maxValue;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            value = source.readInt();
            minValue = source.readInt();
            maxValue = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(value);
            dest.writeInt(minValue);
            dest.writeInt(maxValue);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
