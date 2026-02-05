package com.MaxHighReach;

import javafx.scene.image.Image;

public final class IconCache {

    private static Image load(String path) {
        return new Image(
            IconCache.class.getResource(path).toExternalForm(),
            true
        );
    }

    public static final Image CREATE_CONTRACTS   = load("/images/create-contracts.png");
    public static final Image PIN                = load("/images/pin.png");
    public static final Image CANCELLING         = load("/images/cancelling.png");
    public static final Image DROPPING_OFF       = load("/images/dropping-off.png");
    public static final Image SCHEDULING_SERVICE = load("/images/scheduling-service.png");
    public static final Image CALLING_OFF        = load("/images/calling-off.png");
    public static final Image PICKING_UP         = load("/images/picking-up.png");
    public static final Image CREATE_INVOICES    = load("/images/create-invoices.png");
    public static final Image SEND_TO_MAP        = load("/images/map.png");
    public static final Image EXPAND             = load("/images/expand.png");
    public static final Image REFRESH            = load("/images/refresh.png");
    public static final Image DELETE             = load("/images/delete.png");

    private IconCache() {}
}
