package com.mock_project_java_cevc_hung.hunglpmockjava.controller.admin;

public final class AdminConstants {

    private AdminConstants() {
        //
    }
    
    public static final String ACTIVE_PAGE_TOURS = "tours";
    public static final String ACTIVE_PAGE_CATEGORIES = "categories";
    public static final String ACTIVE_PAGE_BOOKINGS = "bookings";
    public static final String ACTIVE_PAGE_REVIEWS = "reviews";
    public static final String ACTIVE_PAGE_REVENUE = "revenue";

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
    
    // User
    public static final String BASE_PATH_USERS = "/admin/users";
    public static final String VIEW_BASE_USERS = "admin/users/";
    public static final String VIEW_EDIT_USER = VIEW_BASE_USERS + "edit";
    public static final String REDIRECT_USERS = "redirect:" + BASE_PATH_USERS;
    public static final String MSG_USER_UPDATE_SUCCESS = "user.update.success";
    public static final String MSG_USER_UPDATE_ERROR = "user.update.error";
    public static final String MSG_USER_DELETE_SUCCESS = "user.delete.success";
    public static final String MSG_USER_DELETE_ERROR = "user.delete.error";
    public static final String MSG_USER_NOT_FOUND = "user.not_found";
    public static final String MSG_EMAIL_ALREADY_EXISTS = "email.already_exists";
}

