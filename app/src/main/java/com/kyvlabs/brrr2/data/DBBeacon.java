package com.kyvlabs.brrr2.data;

import com.kyvlabs.brrr2.network.model.BeaconModel;

//Beacon entity used to store and load from database/
public class DBBeacon extends BeaconRSSI  implements Comparable<DBBeacon>  {
    private BeaconIds ids;
    private String title;
    private String picture;
    private String description;
    private String link;
    //when we can show this beacon next time
    private long nextShowTime;
    private long nextUpdateTime;

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }

    private boolean isLiked = false;

    public DBBeacon() {
    }

    public DBBeacon(BeaconModel beaconModel) {

        ids = new BeaconIds(beaconModel.getUuid(), beaconModel.getMajor(), beaconModel.getMinor());
        title = beaconModel.getTitle();
        picture = beaconModel.getAbsolutePicture();
        description = beaconModel.getDescription();
        link = beaconModel.getLink();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DBBeacon beacon = (DBBeacon) o;

        if (description != null ? !description.equals(beacon.description) : beacon.description != null)
            return false;
        if (ids != null ? !ids.equals(beacon.ids) : beacon.ids != null) return false;
        if (picture != null ? !picture.equals(beacon.picture) : beacon.picture != null)
            return false;
        if (link != null ? !link.equals(beacon.link) : beacon.link != null)
            return false;
        return !(title != null ? !title.equals(beacon.title) : beacon.title != null);

    }

    @Override
    public int hashCode() {
        int result = ids != null ? ids.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (picture != null ? picture.hashCode() : 0);
        result = 31 * result + (link != null ? link.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DBBeacon{" +
                "ids=" + ids +
                ", title='" + title + '\'' +
                ", picture='" + picture + '\'' +
                ", link='" + link + '\'' +
                ", description='" + description + '\'' +
                ", nextShowTime=" + nextShowTime +
                ", nextupdateTime=" + nextUpdateTime +
                '}';
    }

    public BeaconIds getIds() {
        return ids;
    }

    public void setIds(BeaconIds ids) {
        this.ids = ids;
    }

    public long getNextShowTime() {
        return nextShowTime;
    }

    public void setNextShowTime(long nextShowTime) {
        this.nextShowTime = nextShowTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String image) {
        this.picture = image;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getNextUpdateTime() {
        return nextUpdateTime;
    }

    public void setNextUpdateTime(long nextUpdateTime) {
        this.nextUpdateTime = nextUpdateTime;
    }

    @Override
    public int compareTo(DBBeacon another) {
        return this.getRssi().compareTo(another.getRssi());
    }
}
