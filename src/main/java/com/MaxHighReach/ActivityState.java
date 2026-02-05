package com.MaxHighReach;

import java.time.LocalDate;

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
        return selectedDate != null;
    }

    public boolean hasLift() {
        return selectedLift != null;
    }

    public boolean hasView() {
        return selectedView != null;
    }

    public String toDebugString() {
        return """
            ActivityState
            -------------
            status   = %s
            customer = %s
            date     = %s
            lift     = %s
            view     = %s
            """.formatted(
                selectedStatus,
                selectedCustomer,
                selectedDate,
                selectedLift != null ? selectedLift.getSerialNumber() : null,
                selectedView
            );
    }
    
}
