package org.omnia.pushsdk.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public abstract class EventBase {

    @SerializedName("id")
    private String id;
    
    @SerializedName("type")
    private String type;

    @SerializedName("time")
    private String time;

    @SerializedName("variant_uuid")
    private String variantUuid;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setTime(Date time) {
        this.time = String.format("%d", time.getTime() / 1000L);
    }

    public String getVariantUuid() {
        return variantUuid;
    }

    public void setVariantUuid(String variantUuid) {
        this.variantUuid = variantUuid;
    }

    public EventBase() {
        this.type = getEventType();
    }

    protected abstract String getEventType();

    @Override
    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }

        if (!(o instanceof EventBase)) {
            return false;
        }

        final EventBase other = (EventBase) o;

        if (other.id == null && id != null) {
            return false;
        }
        if (other.id != null && id == null) {
            return false;
        }
        if (other.id != null && id != null && !(other.id.equals(id))) {
            return false;
        }

        if (other.type == null && type != null) {
            return false;
        }
        if (other.type != null && type == null) {
            return false;
        }
        if (other.type != null && type != null && !(other.type.equals(type))) {
            return false;
        }

        if (other.time == null && time != null) {
            return false;
        }
        if (other.time != null && time == null) {
            return false;
        }
        if (other.time != null && time != null && !(other.time.equals(time))) {
            return false;
        }

        if (other.variantUuid == null && variantUuid != null) {
            return false;
        }
        if (other.variantUuid != null && variantUuid == null) {
            return false;
        }
        if (other.variantUuid != null && variantUuid != null && !(other.variantUuid.equals(variantUuid))) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = (result * 31) + (id == null ? 0 : id.hashCode());
        result = (result * 31) + (type == null ? 0 : type.hashCode());
        result = (result * 31) + (time == null ? 0 : time.hashCode());
        result = (result * 31) + (variantUuid == null ? 0 : variantUuid.hashCode());
        return result;
    }
}