

package com.example.marik.maporganizer.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.location.Address;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.Date;
import java.util.UUID;

@Entity(tableName = "task_item")
public class TaskItem implements Parcelable, ClusterItem {


    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "_id")
    public UUID mId;
//
//    @ColumnInfo(name = "location")
//    private Location mLocation;

    @ColumnInfo(name = "latitude")
    private double mLatitude;

    @ColumnInfo(name = "longitude")
    private double mLongitude;

    @ColumnInfo(name = "choosed_address")
    private String mChoosedAddress;

    @ColumnInfo(name = "title")
    private String mTitle;

    @ColumnInfo(name = "description")
    private String mDescription;

    @ColumnInfo(name = "attach_photo")
    private boolean isAttached;

    @ColumnInfo(name = "image_uri")
    private String mImageUri;

    @ColumnInfo(name = "date")
    private Date mDate;

    @ColumnInfo(name = "isremind")
    private boolean mReminder;

    @ColumnInfo(name = "remind_time")
    private long mRemindtime;

    @ColumnInfo(name = "notify_by_place")
    private boolean mNotifyByPlace;

    @ColumnInfo(name = "alert_radius")
    private int mAlertRadius=100;

    public TaskItem() {
        setId(UUID.randomUUID());
    }


    public TaskItem(String title, String description, Date date, String addressLine) {
        this();
        mTitle = title;
        mDescription = description;
        mDate = date;
        mChoosedAddress = addressLine;
    }

    @NonNull
    public UUID getId() {
        return mId;
    }

    public void setId(@NonNull UUID id) {
        mId = id;
    }

    public String getImageUri() {
        return mImageUri;
    }

    public void setImageUri(String imageUri) {
        mImageUri = imageUri;
    }

    public int getAlertRadius() {
        return mAlertRadius;
    }

    public void setAlertRadius(int alertRadius) {
        mAlertRadius = alertRadius;
    }

    public boolean isAttached() {
        return isAttached;
    }

    public void setAttached(boolean attached) {
        isAttached = attached;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public String getChoosedAddress() {
        return mChoosedAddress;
    }

    public void setChoosedAddress(String choosedAddress) {
        mChoosedAddress = choosedAddress;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(mLatitude, mLongitude);
    }

    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return null;
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

    public long getRemindtime() {
        return mRemindtime;
    }

    public void setRemindtime(long remindtime) {
        mRemindtime = remindtime;
    }

    public boolean isNotifyByPlace() {
        return mNotifyByPlace;
    }

    public void setNotifyByPlace(boolean notifyByPlace) {
        mNotifyByPlace = notifyByPlace;
    }

    public static Creator<TaskItem> getCREATOR() {
        return CREATOR;
    }


    String idToString = Converters.toString(mId);

    protected TaskItem(Parcel in) {
        idToString = in.readString();
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
        mChoosedAddress = in.readString();
        mTitle = in.readString();
        mDescription = in.readString();
        isAttached = in.readByte() == 1;
        mImageUri = in.readString();
        long dateMillis = in.readLong();
        mDate = dateMillis > 0 ? new Date(in.readLong()) : null;
        mReminder = in.readByte() == 1;
        mRemindtime = in.readLong();
        mNotifyByPlace = in.readByte() == 1;
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
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);
        dest.writeString(idToString);
        dest.writeString(mChoosedAddress);
        dest.writeString(mTitle);
        dest.writeString(mDescription);
        dest.writeLong(mDate == null ? -1 : mDate.getTime());
        dest.writeByte((byte) (isAttached ? 1 : 2));
        dest.writeString(mImageUri);
        dest.writeByte((byte) (mReminder ? 1 : 0));
        dest.writeLong(mRemindtime);
        dest.writeByte((byte) (mNotifyByPlace ? 1 : 0));
        dest.writeInt(mAlertRadius);
    }
}
