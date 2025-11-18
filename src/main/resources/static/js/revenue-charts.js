/**
 * Revenue Charts Initialization
 * This file handles all Chart.js visualizations for the revenue dashboard
 */

(function() {
    'use strict';

    // Wait for DOM to be ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initCharts);
    } else {
        initCharts();
    }

    function initCharts() {
        // Check if Chart.js is loaded
        if (typeof Chart === 'undefined') {
            console.error('Chart.js is not loaded');
            return;
        }

        // Initialize all charts
        initRevenueChart();
        initBookingChart();
        initRevenueShareChart();
        initCategoryRevenueChart();
        initTopRatedTourChart();
    }

    function initRevenueChart() {
        const ctx = document.getElementById('revenueChart');
        if (!ctx || !window.revenueChartLabels || window.revenueChartLabels.length === 0) {
            return;
        }

        const currencyFormatter = getCurrencyFormatter();

        new Chart(ctx, {
            type: 'line',
            data: {
                labels: window.revenueChartLabels,
                datasets: [{
                    label: window.i18nRevenueDataset || 'Revenue',
                    data: window.revenueChartValues,
                    borderColor: '#0d6efd',
                    backgroundColor: 'rgba(13,110,253,0.15)',
                    borderWidth: 3,
                    fill: true,
                    tension: 0.3,
                    pointRadius: 4,
                    pointBackgroundColor: '#0d6efd',
                    pointBorderColor: '#fff',
                    pointBorderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const value = context.parsed.y || 0;
                                return currencyFormatter.format(value);
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        ticks: {
                            callback: function(value) {
                                if (value >= 1_000_000_000) {
                                    return (value / 1_000_000_000).toFixed(1) + 'B';
                                }
                                if (value >= 1_000_000) {
                                    return (value / 1_000_000).toFixed(1) + 'M';
                                }
                                if (value >= 1_000) {
                                    return (value / 1_000).toFixed(1) + 'K';
                                }
                                return value;
                            }
                        },
                        grid: {
                            color: 'rgba(0,0,0,0.05)'
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    }

    function initBookingChart() {
        const ctx = document.getElementById('bookingChart');
        if (!ctx || !window.revenueChartBookings || window.revenueChartBookings.length === 0) {
            return;
        }

        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: window.revenueChartLabels,
                datasets: [{
                    label: window.i18nBookingDataset || 'Bookings',
                    data: window.revenueChartBookings,
                    backgroundColor: 'rgba(13,110,253,0.35)',
                    borderColor: '#0d6efd',
                    borderWidth: 1.5,
                    borderRadius: 4,
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0
                        },
                        grid: {
                            color: 'rgba(0,0,0,0.05)'
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    }

    function initRevenueShareChart() {
        const ctx = document.getElementById('revenueShareChart');
        if (!ctx || !window.revenueChartValues || window.revenueChartValues.length === 0) {
            return;
        }

        const currencyFormatter = getCurrencyFormatter();
        const palette = ['#0d6efd','#6610f2','#6f42c1','#d63384','#fd7e14','#20c997'];

        new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: window.revenueChartLabels,
                datasets: [{
                    label: window.i18nShareDataset || 'Revenue',
                    data: window.revenueChartValues,
                    backgroundColor: window.revenueChartLabels.map((_, idx) => palette[idx % palette.length]),
                    borderWidth: 0,
                    hoverOffset: 8
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom'
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const dataset = context.dataset.data;
                                const total = dataset.reduce((sum, value) => sum + value, 0);
                                const currentValue = context.parsed;
                                const percentage = total ? ((currentValue / total) * 100).toFixed(1) : 0;
                                return `${context.label}: ${currencyFormatter.format(currentValue)} (${percentage}%)`;
                            }
                        }
                    }
                },
                cutout: '65%'
            }
        });
    }

    function initCategoryRevenueChart() {
        const ctx = document.getElementById('categoryRevenueChart');
        if (!ctx || !window.categoryRevenueLabels || window.categoryRevenueLabels.length === 0) {
            return;
        }

        const currencyFormatter = getCurrencyFormatter();

        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: window.categoryRevenueLabels,
                datasets: [{
                    label: window.i18nCategoryDataset || 'Category revenue',
                    data: window.categoryRevenueValues,
                    backgroundColor: '#20c997',
                    borderColor: '#0f5132',
                    borderWidth: 1.5,
                    borderRadius: 6,
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const value = context.parsed.x || 0;
                                const bookingCount = window.categoryRevenueBookings[context.dataIndex] || 0;
                                const i18nBookings = window.i18nCategoryBookings || 'bookings';
                                return `${currencyFormatter.format(value)} · ${bookingCount} ${i18nBookings}`;
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        ticks: {
                            callback: function(value) {
                                return currencyFormatter.format(value);
                            }
                        },
                        grid: {
                            color: 'rgba(0,0,0,0.05)'
                        }
                    },
                    y: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    }

    function initTopRatedTourChart() {
        const ctx = document.getElementById('topRatedTourChart');
        if (!ctx || !window.topRatedTourLabels || window.topRatedTourLabels.length === 0) {
            return;
        }

        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: window.topRatedTourLabels,
                datasets: [{
                    label: window.i18nRatingDataset || 'Avg rating',
                    data: window.topRatedTourRatings,
                    backgroundColor: 'rgba(255,193,7,0.6)',
                    borderColor: '#ffc107',
                    borderWidth: 1.5,
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const rating = context.parsed.y || 0;
                                const reviews = window.topRatedTourReviews[context.dataIndex] || 0;
                                const i18nReviews = window.i18nRatingReviews || 'reviews';
                                return `${rating.toFixed(2)} ★ · ${reviews} ${i18nReviews}`;
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        suggestedMin: 0,
                        suggestedMax: 5,
                        ticks: {
                            stepSize: 1
                        },
                        grid: {
                            color: 'rgba(0,0,0,0.05)'
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    }

    function getCurrencyFormatter() {
        const currency = window.revenueCurrency || 'VND';
        return new Intl.NumberFormat(undefined, {
            style: 'currency',
            currency: currency,
            maximumFractionDigits: 0
        });
    }

})();

