package com.mock_project_java_cevc_hung.hunglpmockjava.controller.admin;

public final class AdminConstants {

    private AdminConstants() {
        //
    }
    
    public static final String ACTIVE_PAGE_TOURS = "tours";
    public static final String ACTIVE_PAGE_CATEGORIES = "categories";
    public static final String ACTIVE_PAGE_BOOKINGS = "bookings";
    public static final String ACTIVE_PAGE_REVIEWS = "reviews";

    public static final String ATTR_ACTIVE_PAGE = "activePage";
    public static final String ATTR_SUCCESS = "success";
    public static final String ATTR_ERROR = "error";
    
    // Booking
    public static final String MSG_BOOKING_LOAD_ERROR = "booking.load.error";
    public static final String MSG_BOOKING_UPDATE_SUCCESS = "booking.update.success";
    public static final String MSG_BOOKING_UPDATE_ERROR = "booking.update.error";
    public static final String MSG_BOOKING_DELETE_SUCCESS = "booking.delete.success";
    public static final String MSG_BOOKING_DELETE_ERROR = "booking.delete.error";
    public static final String MSG_BOOKING_CANCEL_SUCCESS = "booking.cancel.success";
    public static final String MSG_BOOKING_CANCEL_ERROR = "booking.cancel.error";
    public static final String MSG_BOOKING_STATUS_UPDATE_SUCCESS = "booking.status.update.success";
    public static final String MSG_BOOKING_STATUS_UPDATE_ERROR = "booking.status.update.error";
    public static final String MSG_BOOKING_STATUS_INVALID = "booking.status.invalid";
    
    // Review
    public static final String MSG_REVIEW_APPROVE_SUCCESS = "review.approve.success";
    public static final String MSG_REVIEW_APPROVE_ERROR = "review.approve.error";
    public static final String MSG_REVIEW_REJECT_SUCCESS = "review.reject.success";
    public static final String MSG_REVIEW_REJECT_ERROR = "review.reject.error";
    public static final String MSG_REVIEW_DELETE_SUCCESS = "review.delete.success";
    public static final String MSG_REVIEW_DELETE_ERROR = "review.delete.error";
}

