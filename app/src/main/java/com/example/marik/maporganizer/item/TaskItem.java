package com.example.marik.maporganizer.item;

import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class TaskItem implements Parcelable{

    private Address mAddress;
    private String mChoosedAddress;
    private String mTitle;
    private String mDescription;
    private Date mDate;
    private boolean mReminder;
    private String mRemindtime;
    private boolean mNotifyByPlace;
    private int mAlertRadius;


    public TaskItem() {
    }

    public Address getAddress() {
        return mAddress;
    }

    public void setAddress(Address address) {
        mAddress = address;
    }

    public String getChoosedAddress() {
        return mChoosedAddress;
    }

    public void setChoosedAddress(String choosedAddress) {
        mChoosedAddress = choosedAddress;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isReminder() {
        return mReminder;
    }

    public void setReminder(boolean reminder) {
        mReminder = reminder;
    }

    public String getRemindtime() {
        return mRemindtime;
    }

    public void setRemindtime(String remindtime) {
        mRemindtime = remindtime;
    }

    public boolean isNotifyByPlace() {
        return mNotifyByPlace;
    }

    public void setNotifyByPlace(boolean notifyByPlace) {
        mNotifyByPlace = notifyByPlace;
    }

    public int getRadius() {
        return mAlertRadius;
    }

    public void setRadius(int radius) {
        mAlertRadius = radius;
    }

    public static Creator<TaskItem> getCREATOR() {
        return CREATOR;
    }

    protected TaskItem(Parcel in) {
        mAddress = in.readParcelable(Address.class.getClassLoader());
        mChoosedAddress = in.readString();
        mTitle = in.readString();
        mDescription = in.readString();
        mDate = new Date(in.readLong());
        mReminder = in.readByte() ==1;
        mRemindtime = in.readString();
        mNotifyByPlace = in.readByte() ==1;
        mAlertRadius = in.readInt();
    }

    public static final Creator<TaskItem> CREATOR = new Creator<TaskItem>() {
        @Override
        public TaskItem createFromParcel(Parcel in) {
            return new TaskItem(in);
        }

        @Override
        public TaskItem[] newArray(int size) {
            return new TaskItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mAddress, flags);
        dest.writeString(mChoosedAddress);
        dest.writeString(mTitle);
        dest.writeString(mDescription);
        dest.writeLong(mDate.getTime());
        dest.writeByte((byte) (mReminder ? 1 : 0));
        dest.writeString(mRemindtime);
        dest.writeByte((byte) (mNotifyByPlace ? 1 : 0));
        dest.writeInt(mAlertRadius);
    }
}
