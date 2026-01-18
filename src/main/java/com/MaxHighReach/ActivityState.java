package com.MaxHighReach;

import java.time.LocalDate;

import javafx.scene.Node;
import javafx.scene.shape.Circle;

/**
 * Holds persistent activity / menu state for a scene.
 *
 * This state survives scene transitions and is re-hydrated
 * when the scene is reloaded (e.g. via universal Back).
 *
 * Controllers should READ from and WRITE to this class,
 * but never replace the {@code current} instance.
 */
public class ActivityState {

    /* =========================
       FILTER / SELECTION STATE
       ========================= */

    /** Status filter: e.g. "Active", "Billable", "Upcoming", "Called Off" */
    public String selectedStatus;

    /** Selected customer name (nullable) */
    public String selectedCustomer;

    /** Selected date value (nullable) */
    public LocalDate selectedDate;

    /** Selected lift (nullable) */
    public Lift selectedLift;

    /** Selected view mode (nullable) */
    public String selectedView;


    /* =========================
       UI REPRESENTATION STATE
       ========================= */

    /**
     * Rendered status circle currently displayed in the menu.
     * This is re-used on scene restore instead of rebuilding.
     */
    public Circle currentStatusCircle;

    /**
     * Rendered date node currently displayed (label / hbox / etc).
     */
    public Node currentDateNode;


    /* =========================
       SINGLETON INSTANCE
       ========================= */

    /**
     * Single shared instance for this activity view.
     */
    public static final ActivityState current = new ActivityState();


    /* =========================
       LIFECYCLE
       ========================= */

    /** Reset to initial (empty) state */
    public static void clear() {
        current.selectedStatus = null;
        current.selectedCustomer = null;
        current.selectedDate = null;
        current.selectedLift = null;
        current.selectedView = null;

        current.currentStatusCircle = null;
        current.currentDateNode = null;
    }


    /* =========================
       CONVENIENCE HELPERS
       ========================= */

    public boolean hasStatus() {
        return selectedStatus != null;
    }

    public boolean hasCustomer() {
        return selectedCustomer != null;
    }

    public boolean hasDate() {
        return selectedDate != null && currentDateNode != null;
    }

    public boolean hasLift() {
        return selectedLift != null;
    }

    public boolean hasView() {
        return selectedView != null;
    }
}
