package jp.co.miosys.aitec.models;

import java.util.List;

/**
 * Created by Duc on 3/6/2018.
 */

public class Example {
    public String lifetimeDuration;
    public List<IceServer> iceServers;
    public String blockStatus;
    public String iceTransportPolicy;

    public Example(String lifetimeDuration, List<IceServer> iceServers, String blockStatus, String iceTransportPolicy) {
        this.lifetimeDuration = lifetimeDuration;
        this.iceServers = iceServers;
        this.blockStatus = blockStatus;
        this.iceTransportPolicy = iceTransportPolicy;
    }

    public String getLifetimeDuration() {
        return lifetimeDuration;
    }

    public void setLifetimeDuration(String lifetimeDuration) {
        this.lifetimeDuration = lifetimeDuration;
    }

    public List<IceServer> getIceServers() {
        return iceServers;
    }

    public void setIceServers(List<IceServer> iceServers) {
        this.iceServers = iceServers;
    }

    public String getBlockStatus() {
        return blockStatus;
    }

    public void setBlockStatus(String blockStatus) {
        this.blockStatus = blockStatus;
    }

    public String getIceTransportPolicy() {
        return iceTransportPolicy;
    }

    public void setIceTransportPolicy(String iceTransportPolicy) {
        this.iceTransportPolicy = iceTransportPolicy;
    }
}
