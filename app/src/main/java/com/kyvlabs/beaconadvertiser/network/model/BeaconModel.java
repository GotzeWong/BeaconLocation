package com.kyvlabs.beaconadvertiser.network.model;

public class BeaconModel {
    private String status;
    private String title;
    private String description;
    private String absolutePicture;
    private String link;
    private String uuid;
    private String minor;
    private String major;
    private String groupToBind;
    private String groupName;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMinor() {
        return minor;
    }

    public void setMinor(String minor) {
        this.minor = minor;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "BeaconModel{" +
                "ids= '" + uuid + ":" + major + ":" + minor + '\'' +
                "status='" + status + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", absolutePicture='" + absolutePicture + '\'' +
                ", link='" + link + '\'' +
                ", groupId='" + groupToBind + '\'' +
                ", groupName='" + groupName + '\'' +
                '}';
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAbsolutePicture() {
        return absolutePicture;
    }

    public void setAbsolutePicture(String absolutePicture) {
        this.absolutePicture = absolutePicture;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getGroupToBind() {
        return groupToBind;
    }

    public void setGroupToBind(String groupToBind) {
        this.groupToBind = groupToBind;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
