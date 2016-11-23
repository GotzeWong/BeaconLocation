package com.kyvlabs.beaconadvertiser.data;

import android.os.Parcel;
import android.os.Parcelable;

//Incapsulating uuid+major+minor used for transfer between activities and DB
//Parcelable interface used to serialize
public class BeaconIds implements Parcelable {
    public static final Parcelable.Creator<BeaconIds> CREATOR
            = new Parcelable.Creator<BeaconIds>() {
        public BeaconIds createFromParcel(Parcel in) {
            return new BeaconIds(in);
        }

        public BeaconIds[] newArray(int size) {
            return new BeaconIds[size];
        }
    };
    private final String uuid;
    private final String major;
    private final String minor;

    private BeaconIds(Parcel parcel) {
        this.uuid = parcel.readString();
        this.major = parcel.readString();
        this.minor = parcel.readString();
    }

    public BeaconIds(String uuid, String major, String minor) {
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || ((Object) this).getClass() != o.getClass()) return false;

        BeaconIds ids = (BeaconIds) o;

        return !(major != null ? !major.equals(ids.major) : ids.major != null)
                && !(minor != null ? !minor.equals(ids.minor) : ids.minor != null)
                && !(uuid != null ? !uuid.equals(ids.uuid) : ids.uuid != null);

    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (major != null ? major.hashCode() : 0);
        result = 31 * result + (minor != null ? minor.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return uuid + ":" + major + ":" + minor;
    }

    public String getUuid() {
        return uuid;
    }

    public String getMajor() {
        return major;
    }

    public String getMinor() {
        return minor;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(uuid);
        parcel.writeString(major);
        parcel.writeString(minor);
    }


}
