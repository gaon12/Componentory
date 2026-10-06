package xyz.gaon.componentory.lab

import xyz.gaon.componentory.R

enum class LabComponent(
    val label: String,
    val labelRes: Int,
    val descriptionRes: Int,
    val platformSource: String? = null,
    val material2Function: String? = null,
    val material3Function: String? = material2Function,
    val minimumApi: Int = 1,
    val initialValue: Int = 0,
    val category: ComponentCategory = ComponentCategory.ACTION,
) {
    BUTTON(
        "Button",
        R.string.component_button,
        R.string.component_button_description,
        platformSource = "android.widget.Button",
        material2Function = "Button",
        material3Function = "Button",
        minimumApi = 1,
        initialValue = 0,
    ),
    CHECKBOX(
        "Checkbox",
        R.string.component_checkbox,
        R.string.component_checkbox_description,
        platformSource = "android.widget.CheckBox",
        category = ComponentCategory.SELECTION,
        material2Function = "Checkbox",
        material3Function = "Checkbox",
        minimumApi = 1,
        initialValue = 0,
    ),
    RADIO(
        "Radio buttons",
        R.string.component_radio,
        R.string.component_radio_description,
        platformSource = "android.widget.RadioButton",
        category = ComponentCategory.SELECTION,
        material2Function = "RadioButton",
        material3Function = "RadioButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    SWITCH(
        "Switch",
        R.string.component_switch,
        R.string.component_switch_description,
        platformSource = "android.widget.Switch",
        category = ComponentCategory.SELECTION,
        material2Function = "Switch",
        material3Function = "Switch",
        minimumApi = 14,
        initialValue = 0,
    ),
    TEXT_FIELD(
        "Text field",
        R.string.component_text_field,
        R.string.component_text_field_description,
        platformSource = "android.widget.EditText",
        category = ComponentCategory.INPUT,
        material2Function = "TextField",
        material3Function = "TextField",
        minimumApi = 1,
        initialValue = 0,
    ),
    SLIDER(
        "Slider",
        R.string.component_slider,
        R.string.component_slider_description,
        platformSource = "android.widget.SeekBar",
        category = ComponentCategory.INPUT,
        material2Function = "Slider",
        material3Function = "Slider",
        minimumApi = 1,
        initialValue = 50,
    ),
    RANGE_SLIDER(
        "Range slider",
        R.string.component_range_slider,
        R.string.component_range_slider_description,
        material2Function = "RangeSlider",
        initialValue = 20,
        category = ComponentCategory.INPUT,
    ),
    PROGRESS(
        "Progress",
        R.string.component_progress,
        R.string.component_progress_description,
        platformSource = "android.widget.ProgressBar",
        category = ComponentCategory.INDICATOR,
        material2Function = "LinearProgressIndicator",
        material3Function = "LinearProgressIndicator",
        minimumApi = 1,
        initialValue = 50,
    ),
    CIRCULAR_PROGRESS(
        "Circular progress (determinate)",
        R.string.component_circular_progress,
        R.string.component_circular_progress_description,
        material2Function = "CircularProgressIndicator",
        initialValue = 50,
        category = ComponentCategory.INDICATOR,
    ),
    INDETERMINATE_LINEAR_PROGRESS(
        "Linear progress (indeterminate)",
        R.string.component_indeterminate_linear_progress,
        R.string.component_indeterminate_linear_progress_description,
        platformSource = "android.widget.ProgressBar",
        material2Function = "LinearProgressIndicator",
        category = ComponentCategory.INDICATOR,
    ),
    INDETERMINATE_CIRCULAR_PROGRESS(
        "Circular progress (indeterminate)",
        R.string.component_indeterminate_circular_progress,
        R.string.component_indeterminate_circular_progress_description,
        platformSource = "android.widget.ProgressBar",
        material2Function = "CircularProgressIndicator",
        category = ComponentCategory.INDICATOR,
    ),
    HORIZONTAL_DIVIDER(
        "Horizontal divider",
        R.string.component_horizontal_divider,
        R.string.component_horizontal_divider_description,
        material2Function = "Divider",
        material3Function = "HorizontalDivider",
        category = ComponentCategory.LAYOUT,
    ),
    VERTICAL_DIVIDER(
        "Vertical divider",
        R.string.component_vertical_divider,
        R.string.component_vertical_divider_description,
        material3Function = "VerticalDivider",
        category = ComponentCategory.LAYOUT,
    ),
    LEGACY_DIVIDER(
        "Legacy divider",
        R.string.component_legacy_divider,
        R.string.component_legacy_divider_description,
        material3Function = "Divider",
        category = ComponentCategory.LAYOUT,
    ),
    CARD(
        "Card",
        R.string.component_card,
        R.string.component_card_description,
        material2Function = "Card",
        category = ComponentCategory.LAYOUT,
    ),
    ELEVATED_CARD(
        "Elevated card",
        R.string.component_elevated_card,
        R.string.component_elevated_card_description,
        material3Function = "ElevatedCard",
        category = ComponentCategory.LAYOUT,
    ),
    OUTLINED_CARD(
        "Outlined card",
        R.string.component_outlined_card,
        R.string.component_outlined_card_description,
        material3Function = "OutlinedCard",
        category = ComponentCategory.LAYOUT,
    ),
    SURFACE(
        "Surface",
        R.string.component_surface,
        R.string.component_surface_description,
        material2Function = "Surface",
        category = ComponentCategory.LAYOUT,
    ),
    BADGE(
        "Badge (number)",
        R.string.component_badge,
        R.string.component_badge_description,
        material2Function = "Badge",
        initialValue = 7,
        category = ComponentCategory.INDICATOR,
    ),
    DOT_BADGE(
        "Dot badge",
        R.string.component_dot_badge,
        R.string.component_dot_badge_description,
        material2Function = "Badge",
        category = ComponentCategory.INDICATOR,
    ),
    BADGED_BOX(
        "Badged icon",
        R.string.component_badged_box,
        R.string.component_badged_box_description,
        material2Function = "BadgedBox",
        initialValue = 7,
        category = ComponentCategory.INDICATOR,
    ),
    DIALOG(
        "Dialog",
        R.string.component_dialog,
        R.string.component_dialog_description,
        platformSource = "android.app.AlertDialog",
        category = ComponentCategory.FEEDBACK,
        material2Function = "AlertDialog",
        material3Function = "AlertDialog",
        minimumApi = 1,
        initialValue = 0,
    ),
    BASIC_ALERT_DIALOG(
        "Basic alert dialog",
        R.string.component_basic_alert_dialog,
        R.string.component_basic_alert_dialog_description,
        material3Function = "BasicAlertDialog",
        category = ComponentCategory.FEEDBACK,
        initialValue = 0,
    ),
    DATE_PICKER(
        "Date picker",
        R.string.component_date_picker,
        R.string.component_date_picker_description,
        platformSource = "android.widget.DatePicker",
        material3Function = "DatePicker",
        category = ComponentCategory.PICKER,
    ),
    CALENDAR_VIEW(
        "Calendar view",
        R.string.component_calendar_view,
        R.string.component_calendar_view_description,
        platformSource = "android.widget.CalendarView",
        minimumApi = 11,
        category = ComponentCategory.PICKER,
    ),
    DATE_RANGE_PICKER(
        "Date range picker",
        R.string.component_date_range_picker,
        R.string.component_date_range_picker_description,
        material3Function = "DateRangePicker",
        category = ComponentCategory.PICKER,
    ),
    DATE_PICKER_DIALOG(
        "Date picker dialog",
        R.string.component_date_picker_dialog,
        R.string.component_date_picker_dialog_description,
        platformSource = "android.app.DatePickerDialog",
        material2Function = null,
        material3Function = "DatePickerDialog",
        category = ComponentCategory.PICKER,
    ),
    TIME_PICKER(
        "Time picker",
        R.string.component_time_picker,
        R.string.component_time_picker_description,
        platformSource = "android.widget.TimePicker",
        material3Function = "TimePicker",
        category = ComponentCategory.PICKER,
    ),
    TIME_INPUT(
        "Time input",
        R.string.component_time_input,
        R.string.component_time_input_description,
        material3Function = "TimeInput",
        category = ComponentCategory.PICKER,
    ),
    TIME_PICKER_DIALOG(
        "Time picker dialog",
        R.string.component_time_picker_dialog,
        R.string.component_time_picker_dialog_description,
        platformSource = "android.app.TimePickerDialog",
        material2Function = null,
        material3Function = "TimePickerDialog",
        category = ComponentCategory.PICKER,
    ),
    TEXT_CLOCK(
        "Text clock",
        R.string.component_text_clock,
        R.string.component_text_clock_description,
        platformSource = "android.widget.TextClock",
        material2Function = null,
        material3Function = null,
        minimumApi = 17,
        category = ComponentCategory.CONTENT,
    ),
    ANALOG_CLOCK(
        "Analog clock",
        R.string.component_analog_clock,
        R.string.component_analog_clock_description,
        platformSource = "android.widget.AnalogClock",
        material2Function = null,
        material3Function = null,
        category = ComponentCategory.LEGACY,
    ),
    DIGITAL_CLOCK(
        "Digital clock",
        R.string.component_digital_clock,
        R.string.component_digital_clock_description,
        platformSource = "android.widget.DigitalClock",
        material2Function = null,
        material3Function = null,
        category = ComponentCategory.LEGACY,
    ),
    CHRONOMETER(
        "Chronometer",
        R.string.component_chronometer,
        R.string.component_chronometer_description,
        platformSource = "android.widget.Chronometer",
        material2Function = null,
        material3Function = null,
        category = ComponentCategory.CONTENT,
    ),
    SCROLL_VIEW(
        "Scroll view",
        R.string.component_scroll_view,
        R.string.component_scroll_view_description,
        platformSource = "android.widget.ScrollView",
        category = ComponentCategory.LAYOUT,
    ),
    HORIZONTAL_SCROLL_VIEW(
        "Horizontal scroll view",
        R.string.component_horizontal_scroll_view,
        R.string.component_horizontal_scroll_view_description,
        platformSource = "android.widget.HorizontalScrollView",
        minimumApi = 3,
        category = ComponentCategory.LAYOUT,
    ),
    VIEW_ANIMATOR(
        "View animator",
        R.string.component_view_animator,
        R.string.component_view_animator_description,
        platformSource = "android.widget.ViewAnimator",
        category = ComponentCategory.LAYOUT,
    ),
    VIEW_SWITCHER(
        "View switcher",
        R.string.component_view_switcher,
        R.string.component_view_switcher_description,
        platformSource = "android.widget.ViewSwitcher",
        category = ComponentCategory.LAYOUT,
    ),
    VIEW_FLIPPER(
        "View flipper",
        R.string.component_view_flipper,
        R.string.component_view_flipper_description,
        platformSource = "android.widget.ViewFlipper",
        category = ComponentCategory.LAYOUT,
    ),
    TEXT_SWITCHER(
        "Text switcher",
        R.string.component_text_switcher,
        R.string.component_text_switcher_description,
        platformSource = "android.widget.TextSwitcher",
        category = ComponentCategory.LAYOUT,
    ),
    IMAGE_SWITCHER(
        "Image switcher",
        R.string.component_image_switcher,
        R.string.component_image_switcher_description,
        platformSource = "android.widget.ImageSwitcher",
        category = ComponentCategory.LAYOUT,
    ),
    FRAME_LAYOUT(
        "Frame layout",
        R.string.component_frame_layout,
        R.string.component_frame_layout_description,
        platformSource = "android.widget.FrameLayout",
        category = ComponentCategory.LAYOUT,
    ),
    LINEAR_LAYOUT(
        "Linear layout",
        R.string.component_linear_layout,
        R.string.component_linear_layout_description,
        platformSource = "android.widget.LinearLayout",
        category = ComponentCategory.LAYOUT,
    ),
    TABLE_LAYOUT(
        "Table layout",
        R.string.component_table_layout,
        R.string.component_table_layout_description,
        platformSource = "android.widget.TableLayout",
        category = ComponentCategory.LAYOUT,
    ),
    GRID_LAYOUT(
        "Grid layout",
        R.string.component_grid_layout,
        R.string.component_grid_layout_description,
        platformSource = "android.widget.GridLayout",
        minimumApi = 14,
        category = ComponentCategory.LAYOUT,
    ),
    RELATIVE_LAYOUT(
        "Relative layout",
        R.string.component_relative_layout,
        R.string.component_relative_layout_description,
        platformSource = "android.widget.RelativeLayout",
        category = ComponentCategory.LAYOUT,
    ),
    SPACE(
        "Space",
        R.string.component_space,
        R.string.component_space_description,
        platformSource = "android.widget.Space",
        minimumApi = 14,
        category = ComponentCategory.LAYOUT,
    ),
    ABSOLUTE_LAYOUT(
        "Absolute layout",
        R.string.component_absolute_layout,
        R.string.component_absolute_layout_description,
        platformSource = "android.widget.AbsoluteLayout",
        category = ComponentCategory.LEGACY,
    ),
    LIST_VIEW(
        "List view",
        R.string.component_list_view,
        R.string.component_list_view_description,
        platformSource = "android.widget.ListView",
        category = ComponentCategory.LAYOUT,
    ),
    GRID_VIEW(
        "Grid view",
        R.string.component_grid_view,
        R.string.component_grid_view_description,
        platformSource = "android.widget.GridView",
        category = ComponentCategory.LAYOUT,
    ),
    EXPANDABLE_LIST_VIEW(
        "Expandable list view",
        R.string.component_expandable_list_view,
        R.string.component_expandable_list_view_description,
        initialValue = 1,
        platformSource = "android.widget.ExpandableListView",
        category = ComponentCategory.LAYOUT,
    ),
    ZOOM_CONTROLS(
        "Zoom controls",
        R.string.component_zoom_controls,
        R.string.component_zoom_controls_description,
        initialValue = 5,
        platformSource = "android.widget.ZoomControls",
        category = ComponentCategory.LEGACY,
    ),
    ZOOM_BUTTON(
        "Zoom button",
        R.string.component_zoom_button,
        R.string.component_zoom_button_description,
        initialValue = 5,
        platformSource = "android.widget.ZoomButton",
        category = ComponentCategory.LEGACY,
    ),
    ZOOM_BUTTONS_CONTROLLER(
        "Zoom buttons controller",
        R.string.component_zoom_buttons_controller,
        R.string.component_zoom_buttons_controller_description,
        initialValue = 5,
        minimumApi = 4,
        platformSource = "android.widget.ZoomButtonsController",
        category = ComponentCategory.LEGACY,
    ),
    ADAPTER_VIEW_FLIPPER(
        "Adapter view flipper",
        R.string.component_adapter_view_flipper,
        R.string.component_adapter_view_flipper_description,
        minimumApi = 11,
        platformSource = "android.widget.AdapterViewFlipper",
        category = ComponentCategory.LAYOUT,
    ),
    STACK_VIEW(
        "Stack view",
        R.string.component_stack_view,
        R.string.component_stack_view_description,
        minimumApi = 11,
        platformSource = "android.widget.StackView",
        category = ComponentCategory.LAYOUT,
    ),
    PLAIN_DIALOG(
        "Plain dialog",
        R.string.component_plain_dialog,
        R.string.component_plain_dialog_description,
        platformSource = "android.app.Dialog",
        category = ComponentCategory.FEEDBACK,
    ),
    PROGRESS_DIALOG(
        "Progress dialog",
        R.string.component_progress_dialog,
        R.string.component_progress_dialog_description,
        platformSource = "android.app.ProgressDialog",
        category = ComponentCategory.LEGACY,
    ),
    TOAST(
        "Toast",
        R.string.component_toast,
        R.string.component_toast_description,
        platformSource = "android.widget.Toast",
        category = ComponentCategory.FEEDBACK,
    ),
    TAB_HOST(
        "Tab host",
        R.string.component_tab_host,
        R.string.component_tab_host_description,
        platformSource = "android.widget.TabHost",
        category = ComponentCategory.LEGACY,
    ),
    GALLERY(
        "Gallery",
        R.string.component_gallery,
        R.string.component_gallery_description,
        platformSource = "android.widget.Gallery",
        category = ComponentCategory.LEGACY,
    ),
    SLIDING_DRAWER(
        "Sliding drawer",
        R.string.component_sliding_drawer,
        R.string.component_sliding_drawer_description,
        minimumApi = 3,
        platformSource = "android.widget.SlidingDrawer",
        category = ComponentCategory.LEGACY,
    ),
    TWO_LINE_LIST_ITEM(
        "Two-line list item",
        R.string.component_two_line_list_item,
        R.string.component_two_line_list_item_description,
        platformSource = "android.widget.TwoLineListItem",
        category = ComponentCategory.LEGACY,
    ),
    POPUP_MENU(
        "Popup menu",
        R.string.component_popup_menu,
        R.string.component_popup_menu_description,
        platformSource = "android.widget.PopupMenu",
        material2Function = "DropdownMenu",
        minimumApi = 11,
        category = ComponentCategory.NAVIGATION,
    ),
    POPUP_WINDOW(
        "Popup window",
        R.string.component_popup_window,
        R.string.component_popup_window_description,
        platformSource = "android.widget.PopupWindow",
        category = ComponentCategory.NAVIGATION,
    ),
    LIST_POPUP_WINDOW(
        "List popup window",
        R.string.component_list_popup_window,
        R.string.component_list_popup_window_description,
        platformSource = "android.widget.ListPopupWindow",
        minimumApi = 11,
        category = ComponentCategory.NAVIGATION,
    ),
    TOOLBAR(
        "Toolbar",
        R.string.component_toolbar,
        R.string.component_toolbar_description,
        platformSource = "android.widget.Toolbar",
        minimumApi = 21,
        category = ComponentCategory.NAVIGATION,
    ),
    ACTION_MENU_VIEW(
        "Action menu view",
        R.string.component_action_menu_view,
        R.string.component_action_menu_view_description,
        platformSource = "android.widget.ActionMenuView",
        minimumApi = 21,
        category = ComponentCategory.NAVIGATION,
    ),
    WEB_VIEW(
        "Web view",
        R.string.component_web_view,
        R.string.component_web_view_description,
        platformSource = "android.webkit.WebView",
        minimumApi = 1,
        category = ComponentCategory.CONTENT,
    ),
    QUICK_CONTACT_BADGE(
        "Quick contact badge",
        R.string.component_quick_contact_badge,
        R.string.component_quick_contact_badge_description,
        platformSource = "android.widget.QuickContactBadge",
        minimumApi = 5,
        category = ComponentCategory.CONTENT,
    ),
    DIALER_FILTER(
        "Dialer filter",
        R.string.component_dialer_filter,
        R.string.component_dialer_filter_description,
        platformSource = "android.widget.DialerFilter",
        minimumApi = 1,
        category = ComponentCategory.LEGACY,
    ),
    VIDEO_VIEW(
        "Video view",
        R.string.component_video_view,
        R.string.component_video_view_description,
        platformSource = "android.widget.VideoView",
        minimumApi = 1,
        category = ComponentCategory.MEDIA,
        initialValue = 0,
    ),
    MEDIA_CONTROLLER(
        "Media controller",
        R.string.component_media_controller,
        R.string.component_media_controller_description,
        platformSource = "android.widget.MediaController",
        minimumApi = 1,
        category = ComponentCategory.MEDIA,
    ),
    EDGE_EFFECT(
        "Edge effect",
        R.string.component_edge_effect,
        R.string.component_edge_effect_description,
        platformSource = "android.widget.EdgeEffect",
        minimumApi = 14,
        category = ComponentCategory.CONTENT,
        initialValue = 0,
    ),
    NAVIGATION_BAR(
        "Navigation bar",
        R.string.component_navigation_bar,
        R.string.component_navigation_bar_description,
        material2Function = "BottomNavigation",
        material3Function = "NavigationBar",
        category = ComponentCategory.NAVIGATION,
        initialValue = 1,
    ),
    NAVIGATION_RAIL(
        "Navigation rail",
        R.string.component_navigation_rail,
        R.string.component_navigation_rail_description,
        material2Function = "NavigationRail",
        material3Function = "NavigationRail",
        category = ComponentCategory.NAVIGATION,
        initialValue = 1,
    ),
    SHORT_NAVIGATION_BAR(
        "Short navigation bar",
        R.string.component_short_navigation_bar,
        R.string.component_short_navigation_bar_description,
        material3Function = "ShortNavigationBar",
        category = ComponentCategory.NAVIGATION,
        initialValue = 1,
    ),
    WIDE_NAVIGATION_RAIL(
        "Wide navigation rail",
        R.string.component_wide_navigation_rail,
        R.string.component_wide_navigation_rail_description,
        material3Function = "WideNavigationRail",
        category = ComponentCategory.NAVIGATION,
        initialValue = 1,
    ),
    MODAL_WIDE_NAVIGATION_RAIL(
        "Modal wide navigation rail",
        R.string.component_modal_wide_navigation_rail,
        R.string.component_modal_wide_navigation_rail_description,
        material3Function = "ModalWideNavigationRail",
        category = ComponentCategory.NAVIGATION,
        initialValue = 1,
    ),
    TAB_ROW(
        "Tab row",
        R.string.component_tab_row,
        R.string.component_tab_row_description,
        material2Function = "TabRow",
        material3Function = "TabRow",
        category = ComponentCategory.NAVIGATION,
        initialValue = 1,
    ),
    SCROLLABLE_TAB_ROW(
        "Scrollable tab row",
        R.string.component_scrollable_tab_row,
        R.string.component_scrollable_tab_row_description,
        material2Function = "ScrollableTabRow",
        material3Function = "ScrollableTabRow",
        category = ComponentCategory.NAVIGATION,
        initialValue = 1,
    ),
    PRIMARY_TAB_ROW(
        "Primary tab row",
        R.string.component_primary_tab_row,
        R.string.component_primary_tab_row_description,
        material3Function = "PrimaryTabRow",
        category = ComponentCategory.NAVIGATION,
        initialValue = 1,
    ),
    SECONDARY_TAB_ROW(
        "Secondary tab row",
        R.string.component_secondary_tab_row,
        R.string.component_secondary_tab_row_description,
        material3Function = "SecondaryTabRow",
        category = ComponentCategory.NAVIGATION,
        initialValue = 1,
    ),
    PRIMARY_SCROLLABLE_TAB_ROW(
        "Primary scrollable tab row",
        R.string.component_primary_scrollable_tab_row,
        R.string.component_primary_scrollable_tab_row_description,
        material3Function = "PrimaryScrollableTabRow",
        category = ComponentCategory.NAVIGATION,
        initialValue = 1,
    ),
    SECONDARY_SCROLLABLE_TAB_ROW(
        "Secondary scrollable tab row",
        R.string.component_secondary_scrollable_tab_row,
        R.string.component_secondary_scrollable_tab_row_description,
        material3Function = "SecondaryScrollableTabRow",
        category = ComponentCategory.NAVIGATION,
        initialValue = 1,
    ),
    LIST_ITEM(
        "List item",
        R.string.component_list_item,
        R.string.component_list_item_description,
        material2Function = "ListItem",
        material3Function = "ListItem",
        category = ComponentCategory.LAYOUT,
    ),
    TOP_APP_BAR(
        "Top app bar",
        R.string.component_top_app_bar,
        R.string.component_top_app_bar_description,
        material2Function = "TopAppBar",
        material3Function = "TopAppBar",
        category = ComponentCategory.NAVIGATION,
        initialValue = 0,
    ),
    CENTER_ALIGNED_TOP_APP_BAR(
        "Center-aligned top app bar",
        R.string.component_center_top_app_bar,
        R.string.component_center_top_app_bar_description,
        material3Function = "CenterAlignedTopAppBar",
        category = ComponentCategory.NAVIGATION,
        initialValue = 0,
    ),
    MEDIUM_TOP_APP_BAR(
        "Medium top app bar",
        R.string.component_medium_top_app_bar,
        R.string.component_medium_top_app_bar_description,
        material3Function = "MediumTopAppBar",
        category = ComponentCategory.NAVIGATION,
        initialValue = 0,
    ),
    LARGE_TOP_APP_BAR(
        "Large top app bar",
        R.string.component_large_top_app_bar,
        R.string.component_large_top_app_bar_description,
        material3Function = "LargeTopAppBar",
        category = ComponentCategory.NAVIGATION,
        initialValue = 0,
    ),
    BOTTOM_APP_BAR(
        "Bottom app bar",
        R.string.component_bottom_app_bar,
        R.string.component_bottom_app_bar_description,
        material2Function = "BottomAppBar",
        material3Function = "BottomAppBar",
        category = ComponentCategory.NAVIGATION,
        initialValue = 0,
    ),
    APP_BAR_ROW(
        "App bar row",
        R.string.component_app_bar_row,
        R.string.component_app_bar_row_description,
        material3Function = "AppBarRow",
        category = ComponentCategory.NAVIGATION,
        initialValue = 0,
    ),
    APP_BAR_COLUMN(
        "App bar column",
        R.string.component_app_bar_column,
        R.string.component_app_bar_column_description,
        material3Function = "AppBarColumn",
        category = ComponentCategory.NAVIGATION,
        initialValue = 0,
    ),
    MODAL_NAVIGATION_DRAWER(
        "Modal navigation drawer",
        R.string.component_modal_drawer,
        R.string.component_modal_drawer_description,
        material2Function = "ModalDrawer",
        material3Function = "ModalNavigationDrawer",
        category = ComponentCategory.NAVIGATION,
        initialValue = 0,
    ),
    DISMISSIBLE_NAVIGATION_DRAWER(
        "Dismissible navigation drawer",
        R.string.component_dismissible_drawer,
        R.string.component_dismissible_drawer_description,
        material3Function = "DismissibleNavigationDrawer",
        category = ComponentCategory.NAVIGATION,
        initialValue = 0,
    ),
    PERMANENT_NAVIGATION_DRAWER(
        "Permanent navigation drawer",
        R.string.component_permanent_drawer,
        R.string.component_permanent_drawer_description,
        material3Function = "PermanentNavigationDrawer",
        category = ComponentCategory.NAVIGATION,
    ),
    BOTTOM_DRAWER(
        "Bottom drawer",
        R.string.component_bottom_drawer,
        R.string.component_bottom_drawer_description,
        material2Function = "BottomDrawer",
        material3Function = null,
        category = ComponentCategory.NAVIGATION,
        initialValue = 0,
    ),
    EXPOSED_DROPDOWN(
        "Exposed dropdown menu box",
        R.string.component_exposed_dropdown,
        R.string.component_exposed_dropdown_description,
        material2Function = "ExposedDropdownMenuBox",
        material3Function = "ExposedDropdownMenuBox",
        category = ComponentCategory.INPUT,
    ),
    SCAFFOLD(
        "Scaffold",
        R.string.component_scaffold,
        R.string.component_scaffold_description,
        material2Function = "Scaffold",
        material3Function = "Scaffold",
        category = ComponentCategory.LAYOUT,
    ),
    BOTTOM_SHEET_SCAFFOLD(
        "Bottom sheet scaffold",
        R.string.component_bottom_sheet_scaffold,
        R.string.component_bottom_sheet_scaffold_description,
        material2Function = "BottomSheetScaffold",
        material3Function = "BottomSheetScaffold",
        category = ComponentCategory.LAYOUT,
        initialValue = 0,
    ),
    MODAL_BOTTOM_SHEET(
        "Modal bottom sheet",
        R.string.component_modal_bottom_sheet,
        R.string.component_modal_bottom_sheet_description,
        material2Function = "ModalBottomSheetLayout",
        material3Function = "ModalBottomSheet",
        category = ComponentCategory.LAYOUT,
        initialValue = 0,
    ),
    BACKDROP_SCAFFOLD(
        "Backdrop scaffold",
        R.string.component_backdrop_scaffold,
        R.string.component_backdrop_scaffold_description,
        material2Function = "BackdropScaffold",
        material3Function = null,
        category = ComponentCategory.LAYOUT,
        initialValue = 0,
    ),
    PLAIN_TOOLTIP(
        "Plain tooltip",
        R.string.component_plain_tooltip,
        R.string.component_plain_tooltip_description,
        material3Function = "PlainTooltip",
        category = ComponentCategory.FEEDBACK,
    ),
    RICH_TOOLTIP(
        "Rich tooltip",
        R.string.component_rich_tooltip,
        R.string.component_rich_tooltip_description,
        material3Function = "RichTooltip",
        category = ComponentCategory.FEEDBACK,
    ),
    SEARCH_BAR(
        "Search bar",
        R.string.component_search_bar,
        R.string.component_search_bar_description,
        material3Function = "SearchBar",
        category = ComponentCategory.INPUT,
    ),
    DOCKED_SEARCH_BAR(
        "Docked search bar",
        R.string.component_docked_search_bar,
        R.string.component_docked_search_bar_description,
        material3Function = "DockedSearchBar",
        category = ComponentCategory.INPUT,
    ),
    TOP_SEARCH_BAR(
        "Top search bar",
        R.string.component_top_search_bar,
        R.string.component_top_search_bar_description,
        material3Function = "TopSearchBar",
        category = ComponentCategory.INPUT,
    ),
    EXPANDED_DOCKED_SEARCH_BAR(
        "Expanded docked search bar",
        R.string.component_expanded_docked_search_bar,
        R.string.component_expanded_docked_search_bar_description,
        material3Function = "ExpandedDockedSearchBar",
        category = ComponentCategory.INPUT,
    ),
    MULTI_BROWSE_CAROUSEL(
        "Multi-browse carousel",
        R.string.component_multi_browse_carousel,
        R.string.component_multi_browse_carousel_description,
        material3Function = "carousel.HorizontalMultiBrowseCarousel",
        category = ComponentCategory.LAYOUT,
    ),
    UNCONTAINED_CAROUSEL(
        "Uncontained carousel",
        R.string.component_uncontained_carousel,
        R.string.component_uncontained_carousel_description,
        material3Function = "carousel.HorizontalUncontainedCarousel",
        category = ComponentCategory.LAYOUT,
    ),
    PULL_TO_REFRESH(
        "Pull to refresh",
        R.string.component_pull_to_refresh,
        R.string.component_pull_to_refresh_description,
        material3Function = "pulltorefresh.PullToRefreshBox",
        category = ComponentCategory.LAYOUT,
    ),
    CENTERED_HERO_CAROUSEL(
        "Centered hero carousel",
        R.string.component_centered_hero_carousel,
        R.string.component_centered_hero_carousel_description,
        material3Function = "carousel.HorizontalCenteredHeroCarousel",
        category = ComponentCategory.LAYOUT,
    ),
    SWIPE_TO_DISMISS(
        "Swipe to dismiss",
        R.string.component_swipe_to_dismiss,
        R.string.component_swipe_to_dismiss_description,
        material2Function = "SwipeToDismiss",
        material3Function = "SwipeToDismissBox",
        category = ComponentCategory.ACTION,
        initialValue = 0,
    ),
    SNACKBAR(
        "Snackbar",
        R.string.component_snackbar,
        R.string.component_snackbar_description,
        material2Function = "Snackbar",
        material3Function = "Snackbar",
        category = ComponentCategory.FEEDBACK,
        initialValue = 0,
    ),
    SHARE_ACTION_PROVIDER(
        "Share action provider",
        R.string.component_share_action_provider,
        R.string.component_share_action_provider_description,
        platformSource = "android.widget.ShareActionProvider",
        minimumApi = 14,
        category = ComponentCategory.ACTION,
        initialValue = 0,
    ),
    TOGGLE_BUTTON(
        "Toggle button",
        R.string.component_toggle_button,
        R.string.component_toggle_button_description,
        platformSource = "android.widget.ToggleButton",
        category = ComponentCategory.SELECTION,
        material2Function = null,
        material3Function = null,
        minimumApi = 1,
        initialValue = 0,
    ),
    IMAGE_BUTTON(
        "Image button",
        R.string.component_image_button,
        R.string.component_image_button_description,
        platformSource = "android.widget.ImageButton",
        material2Function = null,
        material3Function = null,
        minimumApi = 1,
        initialValue = 0,
    ),
    RATING(
        "Rating bar",
        R.string.component_rating,
        R.string.component_rating_description,
        platformSource = "android.widget.RatingBar",
        category = ComponentCategory.PICKER,
        material2Function = null,
        material3Function = null,
        minimumApi = 1,
        initialValue = 0,
    ),
    NUMBER_PICKER(
        "Number picker",
        R.string.component_number_picker,
        R.string.component_number_picker_description,
        platformSource = "android.widget.NumberPicker",
        category = ComponentCategory.PICKER,
        material2Function = null,
        material3Function = null,
        minimumApi = 11,
        initialValue = 5,
    ),
    OUTLINED_BUTTON(
        "Outlined button",
        R.string.component_outlined_button,
        R.string.component_outlined_button_description,
        platformSource = null,
        material2Function = "OutlinedButton",
        material3Function = "OutlinedButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    TEXT_BUTTON(
        "Text button",
        R.string.component_text_button,
        R.string.component_text_button_description,
        platformSource = null,
        material2Function = "TextButton",
        material3Function = "TextButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    ELEVATED_BUTTON(
        "Elevated button",
        R.string.component_elevated_button,
        R.string.component_elevated_button_description,
        platformSource = null,
        material2Function = null,
        material3Function = "ElevatedButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    TONAL_BUTTON(
        "Tonal button",
        R.string.component_tonal_button,
        R.string.component_tonal_button_description,
        platformSource = null,
        material2Function = null,
        material3Function = "FilledTonalButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    ICON_BUTTON(
        "Icon button",
        R.string.component_icon_button,
        R.string.component_icon_button_description,
        platformSource = null,
        material2Function = "IconButton",
        material3Function = "IconButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    ICON_TOGGLE(
        "Icon toggle button",
        R.string.component_icon_toggle,
        R.string.component_icon_toggle_description,
        platformSource = null,
        material2Function = "IconToggleButton",
        material3Function = "IconToggleButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    FILLED_ICON_BUTTON(
        "Filled icon button",
        R.string.component_filled_icon_button,
        R.string.component_filled_icon_button_description,
        platformSource = null,
        material2Function = null,
        material3Function = "FilledIconButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    FILLED_ICON_TOGGLE(
        "Filled icon toggle",
        R.string.component_filled_icon_toggle,
        R.string.component_filled_icon_toggle_description,
        platformSource = null,
        material2Function = null,
        material3Function = "FilledIconToggleButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    TONAL_ICON_BUTTON(
        "Tonal icon button",
        R.string.component_tonal_icon_button,
        R.string.component_tonal_icon_button_description,
        platformSource = null,
        material2Function = null,
        material3Function = "FilledTonalIconButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    TONAL_ICON_TOGGLE(
        "Tonal icon toggle",
        R.string.component_tonal_icon_toggle,
        R.string.component_tonal_icon_toggle_description,
        platformSource = null,
        material2Function = null,
        material3Function = "FilledTonalIconToggleButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    OUTLINED_ICON_BUTTON(
        "Outlined icon button",
        R.string.component_outlined_icon_button,
        R.string.component_outlined_icon_button_description,
        platformSource = null,
        material2Function = null,
        material3Function = "OutlinedIconButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    OUTLINED_ICON_TOGGLE(
        "Outlined icon toggle",
        R.string.component_outlined_icon_toggle,
        R.string.component_outlined_icon_toggle_description,
        platformSource = null,
        material2Function = null,
        material3Function = "OutlinedIconToggleButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    FAB(
        "Floating action button",
        R.string.component_fab,
        R.string.component_fab_description,
        platformSource = null,
        material2Function = "FloatingActionButton",
        material3Function = "FloatingActionButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    EXTENDED_FAB(
        "Extended floating action button",
        R.string.component_extended_fab,
        R.string.component_extended_fab_description,
        platformSource = null,
        material2Function = "ExtendedFloatingActionButton",
        material3Function = "ExtendedFloatingActionButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    SMALL_FAB(
        "Small floating action button",
        R.string.component_small_fab,
        R.string.component_small_fab_description,
        platformSource = null,
        material2Function = null,
        material3Function = "SmallFloatingActionButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    LARGE_FAB(
        "Large floating action button",
        R.string.component_large_fab,
        R.string.component_large_fab_description,
        platformSource = null,
        material2Function = null,
        material3Function = "LargeFloatingActionButton",
        minimumApi = 1,
        initialValue = 0,
    ),
    CHIP(
        "Chip",
        R.string.component_chip,
        R.string.component_chip_description,
        material2Function = "Chip",
        material3Function = null,
        category = ComponentCategory.SELECTION,
    ),
    ASSIST_CHIP(
        "Assist chip",
        R.string.component_assist_chip,
        R.string.component_assist_chip_description,
        material2Function = null,
        material3Function = "AssistChip",
        category = ComponentCategory.SELECTION,
    ),
    ELEVATED_ASSIST_CHIP(
        "Elevated assist chip",
        R.string.component_elevated_assist_chip,
        R.string.component_elevated_assist_chip_description,
        material2Function = null,
        material3Function = "ElevatedAssistChip",
        category = ComponentCategory.SELECTION,
    ),
    FILTER_CHIP(
        "Filter chip",
        R.string.component_filter_chip,
        R.string.component_filter_chip_description,
        material2Function = "FilterChip",
        material3Function = "FilterChip",
        category = ComponentCategory.SELECTION,
    ),
    ELEVATED_FILTER_CHIP(
        "Elevated filter chip",
        R.string.component_elevated_filter_chip,
        R.string.component_elevated_filter_chip_description,
        material2Function = null,
        material3Function = "ElevatedFilterChip",
        category = ComponentCategory.SELECTION,
    ),
    INPUT_CHIP(
        "Input chip",
        R.string.component_input_chip,
        R.string.component_input_chip_description,
        material2Function = null,
        material3Function = "InputChip",
        category = ComponentCategory.SELECTION,
    ),
    SUGGESTION_CHIP(
        "Suggestion chip",
        R.string.component_suggestion_chip,
        R.string.component_suggestion_chip_description,
        material2Function = null,
        material3Function = "SuggestionChip",
        category = ComponentCategory.SELECTION,
    ),
    ELEVATED_SUGGESTION_CHIP(
        "Elevated suggestion chip",
        R.string.component_elevated_suggestion_chip,
        R.string.component_elevated_suggestion_chip_description,
        material2Function = null,
        material3Function = "ElevatedSuggestionChip",
        category = ComponentCategory.SELECTION,
    ),
    TRI_STATE_CHECKBOX(
        "Tri-state checkbox",
        R.string.component_tri_state_checkbox,
        R.string.component_tri_state_checkbox_description,
        material2Function = "TriStateCheckbox",
        material3Function = "TriStateCheckbox",
        category = ComponentCategory.SELECTION,
    ),
    SINGLE_SEGMENTED(
        "Single-choice segmented buttons",
        R.string.component_single_segmented,
        R.string.component_single_segmented_description,
        material2Function = null,
        material3Function = "SingleChoiceSegmentedButtonRow",
        category = ComponentCategory.SELECTION,
    ),
    MULTI_SEGMENTED(
        "Multi-choice segmented buttons",
        R.string.component_multi_segmented,
        R.string.component_multi_segmented_description,
        material2Function = null,
        material3Function = "MultiChoiceSegmentedButtonRow",
        category = ComponentCategory.SELECTION,
    ),
    OUTLINED_TEXT_FIELD(
        "Outlined text field",
        R.string.component_outlined_text_field,
        R.string.component_outlined_text_field_description,
        material2Function = "OutlinedTextField",
        category = ComponentCategory.INPUT,
    ),
    SECURE_TEXT_FIELD(
        "Secure text field",
        R.string.component_secure_text_field,
        R.string.component_secure_text_field_description,
        material2Function = "SecureTextField",
        category = ComponentCategory.INPUT,
    ),
    OUTLINED_SECURE_TEXT_FIELD(
        "Outlined secure text field",
        R.string.component_outlined_secure_text_field,
        R.string.component_outlined_secure_text_field_description,
        material2Function = "OutlinedSecureTextField",
        category = ComponentCategory.INPUT,
    ),
    AUTOCOMPLETE(
        "Autocomplete",
        R.string.component_autocomplete,
        R.string.component_autocomplete_description,
        platformSource = "android.widget.AutoCompleteTextView",
        category = ComponentCategory.INPUT,
    ),
    MULTI_AUTOCOMPLETE(
        "Multi autocomplete",
        R.string.component_multi_autocomplete,
        R.string.component_multi_autocomplete_description,
        platformSource = "android.widget.MultiAutoCompleteTextView",
        category = ComponentCategory.INPUT,
    ),
    SPINNER(
        "Spinner",
        R.string.component_spinner,
        R.string.component_spinner_description,
        platformSource = "android.widget.Spinner",
        category = ComponentCategory.INPUT,
    ),
    TEXT(
        "Text",
        R.string.component_text,
        R.string.component_text_description,
        platformSource = "android.widget.TextView",
        material2Function = "Text",
        category = ComponentCategory.CONTENT,
    ),
    CHECKED_TEXT_VIEW(
        "Checked text view",
        R.string.component_checked_text_view,
        R.string.component_checked_text_view_description,
        platformSource = "android.widget.CheckedTextView",
        category = ComponentCategory.CONTENT,
    ),
    ICON(
        "Icon",
        R.string.component_icon,
        R.string.component_icon_description,
        platformSource = "android.widget.ImageView",
        material2Function = "Icon",
        category = ComponentCategory.CONTENT,
    ),
    SEARCH_VIEW(
        "Search view",
        R.string.component_search_view,
        R.string.component_search_view_description,
        platformSource = "android.widget.SearchView",
        minimumApi = 11,
        category = ComponentCategory.INPUT,
    );

    val source: String
        get() =
            platformSource
                ?: material2Function?.let { "androidx.compose.material.$it" }
                ?: "androidx.compose.material3.${requireNotNull(material3Function)}"

    val isIconToggle: Boolean
        get() =
            this in listOf(ICON_TOGGLE, FILLED_ICON_TOGGLE, TONAL_ICON_TOGGLE, OUTLINED_ICON_TOGGLE)

    val isFloatingAction: Boolean
        get() = this in listOf(FAB, EXTENDED_FAB, SMALL_FAB, LARGE_FAB)

    val isSecureInput: Boolean
        get() = this == SECURE_TEXT_FIELD || this == OUTLINED_SECURE_TEXT_FIELD

    val isDeterminateProgress: Boolean
        get() = this == PROGRESS || this == CIRCULAR_PROGRESS

    val isDivider: Boolean
        get() = this in listOf(HORIZONTAL_DIVIDER, VERTICAL_DIVIDER, LEGACY_DIVIDER)

    val isContainer: Boolean
        get() = this in listOf(CARD, ELEVATED_CARD, OUTLINED_CARD, SURFACE)

    val isInlineTime: Boolean
        get() = this == TIME_PICKER || this == TIME_INPUT

    val isClockDisplay: Boolean
        get() = this in listOf(TEXT_CLOCK, ANALOG_CLOCK, DIGITAL_CLOCK)

    val isScrollContainer: Boolean
        get() = this == SCROLL_VIEW || this == HORIZONTAL_SCROLL_VIEW

    val isViewSwitcher: Boolean
        get() =
            this in
                listOf(VIEW_ANIMATOR, VIEW_SWITCHER, VIEW_FLIPPER, TEXT_SWITCHER, IMAGE_SWITCHER)

    // Logical page count a switcher steps through; TextSwitcher and ImageSwitcher
    // reuse their two internal children for more content.
    val switcherPageCount: Int
        get() =
            when (this) {
                VIEW_SWITCHER,
                IMAGE_SWITCHER -> 2
                else -> 4
            }

    val isFrameworkLayout: Boolean
        get() =
            this in
                listOf(
                    FRAME_LAYOUT,
                    LINEAR_LAYOUT,
                    TABLE_LAYOUT,
                    GRID_LAYOUT,
                    RELATIVE_LAYOUT,
                    SPACE,
                    ABSOLUTE_LAYOUT,
                )

    val isAdapterList: Boolean
        get() = this in listOf(LIST_VIEW, GRID_VIEW, EXPANDABLE_LIST_VIEW)

    val isZoomControl: Boolean
        get() = this in listOf(ZOOM_CONTROLS, ZOOM_BUTTON, ZOOM_BUTTONS_CONTROLLER)

    val isAdapterAnimator: Boolean
        get() = this == ADAPTER_VIEW_FLIPPER || this == STACK_VIEW

    // Samples whose widget lives in a transient window instead of the panel.
    val isTransientWindow: Boolean
        get() = this == PLAIN_DIALOG || this == PROGRESS_DIALOG || this == TOAST

    // TabHost draws three tabs; scrollable rows need enough tabs to overflow
    // the fixed sample width.
    val tabCount: Int
        get() =
            when (this) {
                TAB_HOST,
                TAB_ROW,
                PRIMARY_TAB_ROW,
                SECONDARY_TAB_ROW -> 3
                SCROLLABLE_TAB_ROW,
                PRIMARY_SCROLLABLE_TAB_ROW,
                SECONDARY_SCROLLABLE_TAB_ROW -> 8
                else -> 0
            }

    // Deprecated containers that keep real interaction but no library twin.
    val isLegacyContainer: Boolean
        get() = this == GALLERY || this == SLIDING_DRAWER || this == TWO_LINE_LIST_ITEM

    // Anchored popup objects that are not View children of the panel.
    val isPopupWindow: Boolean
        get() = this == POPUP_WINDOW || this == LIST_POPUP_WINDOW

    // Menu hosts that report the last invoked action as their state.
    val isMenuHost: Boolean
        get() = this == TOOLBAR || this == ACTION_MENU_VIEW

    // Self-contained surfaces whose real content lives inside the widget.
    val isContentSurface: Boolean
        get() = this == WEB_VIEW || this == QUICK_CONTACT_BADGE

    // Media widgets backed by the bundled clip in res/raw.
    val isMediaWidget: Boolean
        get() = this == VIDEO_VIEW || this == MEDIA_CONTROLLER

    // EdgeEffect is a drawable-like effect, not a View; a host View draws it.
    val isEdgeEffect: Boolean
        get() = this == EDGE_EFFECT

    val isNavigationSuite: Boolean
        get() =
            this == NAVIGATION_BAR ||
                this == NAVIGATION_RAIL ||
                this == SHORT_NAVIGATION_BAR ||
                this == WIDE_NAVIGATION_RAIL ||
                this == MODAL_WIDE_NAVIGATION_RAIL

    val isTabRow: Boolean
        get() =
            this == TAB_ROW ||
                this == SCROLLABLE_TAB_ROW ||
                this == PRIMARY_TAB_ROW ||
                this == SECONDARY_TAB_ROW ||
                this == PRIMARY_SCROLLABLE_TAB_ROW ||
                this == SECONDARY_SCROLLABLE_TAB_ROW

    // App bars count action clicks instead of carrying selection state.
    val isAppBar: Boolean
        get() =
            this == TOP_APP_BAR ||
                this == CENTER_ALIGNED_TOP_APP_BAR ||
                this == MEDIUM_TOP_APP_BAR ||
                this == LARGE_TOP_APP_BAR ||
                this == BOTTOM_APP_BAR ||
                this == APP_BAR_ROW ||
                this == APP_BAR_COLUMN

    // Drawers whose sheet state is real and copyable; the permanent drawer
    // always shows its sheet, so it carries no open state.
    val isToggleableDrawer: Boolean
        get() =
            this == MODAL_NAVIGATION_DRAWER ||
                this == DISMISSIBLE_NAVIGATION_DRAWER ||
                this == BOTTOM_DRAWER

    val isDrawerSuite: Boolean
        get() = isToggleableDrawer || this == PERMANENT_NAVIGATION_DRAWER

    // Sheets whose open state is real and copyable; every sheet in this group
    // exposes a genuine state object that a button and dismissals both drive.
    val isSheetSuite: Boolean
        get() =
            this == BOTTOM_SHEET_SCAFFOLD || this == MODAL_BOTTOM_SHEET || this == BACKDROP_SCAFFOLD

    // Tooltips are transient overlays; the samples show a real TooltipBox but
    // carry no copyable inputs.
    val isTooltip: Boolean
        get() = this == PLAIN_TOOLTIP || this == RICH_TOOLTIP

    // The Material 3 carousel variants share one item model; the selected item
    // index is the copyable input.
    val isCarousel: Boolean
        get() =
            this == MULTI_BROWSE_CAROUSEL ||
                this == UNCONTAINED_CAROUSEL ||
                this == CENTERED_HERO_CAROUSEL

    // The samples draw three destination items; the selected index copies.
    val navigationItemCount: Int
        get() = if (isNavigationSuite) 3 else 0

    val galleryItemCount: Int
        get() = if (this == GALLERY) 6 else 0

    // Logical page count an adapter animator steps through.
    val adapterPageCount: Int
        get() =
            when (this) {
                ADAPTER_VIEW_FLIPPER -> 4
                STACK_VIEW -> 6
                else -> 0
            }

    val zoomLevelMax: Int
        get() = if (isZoomControl) 10 else 0

    // The API level that deprecated the framework source, or null when the
    // component is not deprecated.
    val deprecatedApi: Int?
        get() =
            when (this) {
                ABSOLUTE_LAYOUT -> 3
                GALLERY -> 16
                DIGITAL_CLOCK,
                SLIDING_DRAWER,
                TWO_LINE_LIST_ITEM -> 17
                ANALOG_CLOCK -> 23
                ZOOM_BUTTON,
                ZOOM_BUTTONS_CONTROLLER,
                DIALER_FILTER,
                PROGRESS_DIALOG -> 26
                ZOOM_CONTROLS -> 29
                TAB_HOST -> 30
                else -> null
            }

    val listRowCount: Int
        get() =
            when (this) {
                LIST_VIEW -> 6
                GRID_VIEW -> 9
                EXPANDABLE_LIST_VIEW -> 3
                else -> 0
            }

    val isIndeterminateProgress: Boolean
        get() = this == INDETERMINATE_LINEAR_PROGRESS || this == INDETERMINATE_CIRCULAR_PROGRESS

    val isBadge: Boolean
        get() = this in listOf(BADGE, DOT_BADGE, BADGED_BOX)

    val isCountedBadge: Boolean
        get() = this == BADGE || this == BADGED_BOX

    val usesIcon: Boolean
        get() =
            this == ICON ||
                this == BADGED_BOX ||
                this == IMAGE_BUTTON ||
                isIconToggle ||
                isFloatingAction ||
                this in
                    listOf(ICON_BUTTON, FILLED_ICON_BUTTON, TONAL_ICON_BUTTON, OUTLINED_ICON_BUTTON)

    fun matchesSearch(query: String, context: android.content.Context? = null): Boolean {
        val term = query.trim()
        val names =
            listOfNotNull(
                label,
                context?.getString(labelRes),
                context?.getString(descriptionRes),
                platformSource,
                material2Function?.let { "androidx.compose.material.$it" },
                material3Function?.let { "androidx.compose.material3.$it" },
            ) +
                when (this) {
                    TIME_PICKER_DIALOG ->
                        listOf("android.widget.TimePicker", "androidx.compose.material3.TimeInput")
                    POPUP_MENU ->
                        listOf(
                            "androidx.compose.material.DropdownMenuItem",
                            "androidx.compose.material3.DropdownMenuItem",
                        )
                    else -> emptyList()
                }
        return term.isEmpty() || names.any { it.contains(term, ignoreCase = true) }
    }

    fun feedback(
        context: android.content.Context,
        value: Int,
        text: String,
        rangeEnd: Int = 80,
        dateUtcMillis: Long = SampleDates.INITIAL_UTC_MILLIS,
        timeMinutes: Int = SampleTimes.INITIAL_MINUTES,
        time24Hour: Boolean = true,
        chronometerBaseMillis: Long = 0,
    ): String {
        val empty = context.getString(R.string.sample_state_empty)
        return when {
            isDivider ||
                this == DOT_BADGE ||
                this == TEXT ||
                this == ANALOG_CLOCK ||
                this == DIGITAL_CLOCK ||
                isScrollContainer ||
                isFrameworkLayout ||
                isContentSurface ->
                context.getString(R.string.status_preview, context.getString(labelRes))
            this == TEXT_CLOCK ->
                context.getString(
                    R.string.status_clock_format,
                    context.getString(
                        if (time24Hour) R.string.clock_format_24 else R.string.clock_format_12
                    ),
                )
            this == CHRONOMETER ->
                if (value == 1) context.getString(R.string.status_chronometer_running)
                else
                    context.getString(
                        R.string.status_chronometer_stopped,
                        SampleTimes.formatElapsed(chronometerBaseMillis),
                    )
            isViewSwitcher ->
                context.getString(R.string.status_switcher_child, value + 1, switcherPageCount)
            isAdapterAnimator ->
                context.getString(R.string.status_switcher_child, value + 1, adapterPageCount)
            isTransientWindow -> context.getString(R.string.status_shown_times, value)
            this == POPUP_WINDOW -> context.getString(R.string.status_shown_times, value)
            this == LIST_POPUP_WINDOW ->
                if (value == 0) context.getString(R.string.sample_state_no_selection)
                else
                    context.getString(
                        R.string.status_selected,
                        context.getString(R.string.list_item, value),
                    )
            this == VIDEO_VIEW ->
                context.getString(
                    if (value == 1) R.string.status_playing else R.string.status_paused
                )
            this == SHARE_ACTION_PROVIDER -> context.getString(R.string.status_shares, value)
            this == EDGE_EFFECT -> context.getString(R.string.status_pulls, value)
            this == SNACKBAR -> context.getString(R.string.status_shown_times, value)
            isAppBar -> context.getString(R.string.status_clicks, value)
            this == PERMANENT_NAVIGATION_DRAWER ->
                context.getString(R.string.status_preview, context.getString(labelRes))
            isTooltip -> context.getString(R.string.status_preview, context.getString(labelRes))
            this == SCAFFOLD ->
                context.getString(R.string.status_preview, context.getString(labelRes))
            isToggleableDrawer ->
                context.getString(
                    if (value == 1) R.string.drawer_opened else R.string.drawer_closed
                )
            isSheetSuite ->
                context.getString(if (value == 1) R.string.sheet_open else R.string.sheet_closed)
            this == SWIPE_TO_DISMISS ->
                context.getString(if (value == 1) R.string.dismissed else R.string.settled)
            isNavigationSuite || isTabRow ->
                context.getString(
                    R.string.status_selected,
                    context.getString(R.string.list_item, value),
                )
            isCarousel ->
                if (value == 0) context.getString(R.string.sample_state_no_selection)
                else
                    context.getString(
                        R.string.status_selected,
                        context.getString(R.string.list_item, value),
                    )
            this == PULL_TO_REFRESH -> context.getString(R.string.status_refreshes, value)
            this == MEDIA_CONTROLLER ->
                context.getString(R.string.status_preview, context.getString(labelRes))
            isMenuHost ->
                if (value == 0) context.getString(R.string.sample_state_no_selection)
                else
                    context.getString(
                        R.string.status_selected,
                        context.getString(
                            when (value) {
                                1 -> R.string.option_a
                                2 -> R.string.option_b
                                else -> R.string.toolbar_nav_action
                            }
                        ),
                    )
            this == TAB_HOST -> context.getString(R.string.status_tab, value + 1, tabCount)
            this == GALLERY ->
                context.getString(R.string.status_gallery, value + 1, galleryItemCount)
            this == SLIDING_DRAWER ->
                context.getString(
                    if (value == 1) R.string.drawer_opened else R.string.drawer_closed
                )
            this == TWO_LINE_LIST_ITEM || this == LIST_ITEM ->
                context.getString(R.string.status_preview, context.getString(labelRes))
            this == LIST_VIEW || this == GRID_VIEW ->
                if (value == 0) context.getString(R.string.sample_state_no_selection)
                else
                    context.getString(
                        R.string.status_selected,
                        context.getString(R.string.list_item, value),
                    )
            this == EXPANDABLE_LIST_VIEW ->
                context.getString(
                    R.string.status_expanded,
                    (0 until listRowCount)
                        .filter { value and (1 shl it) != 0 }
                        .joinToString(", ") { context.getString(R.string.list_group, it + 1) }
                        .ifEmpty { context.getString(R.string.sample_state_none) },
                )
            isZoomControl -> context.getString(R.string.status_zoom, value, zoomLevelMax)
            isCountedBadge -> context.getString(R.string.status_badge, value)
            isIndeterminateProgress -> context.getString(R.string.status_indeterminate_progress)
            this == RANGE_SLIDER -> context.getString(R.string.status_range, value, rangeEnd)
            isSecureInput -> context.getString(R.string.status_characters, value)
            this == SPINNER ->
                context.getString(R.string.status_selected, listOf("Alpha", "Beta", "Gamma")[value])
            this == SEARCH_VIEW ->
                context.getString(R.string.status_search, text.ifEmpty { empty }, value)
            this in
                listOf(
                    TEXT_FIELD,
                    OUTLINED_TEXT_FIELD,
                    AUTOCOMPLETE,
                    MULTI_AUTOCOMPLETE,
                    DIALER_FILTER,
                    EXPOSED_DROPDOWN,
                    SEARCH_BAR,
                    DOCKED_SEARCH_BAR,
                    TOP_SEARCH_BAR,
                    EXPANDED_DOCKED_SEARCH_BAR,
                ) -> context.getString(R.string.status_text, text.ifEmpty { empty })
            this == TRI_STATE_CHECKBOX ->
                context.getString(
                    when (value) {
                        1 -> R.string.sample_state_checked
                        2 -> R.string.sample_state_indeterminate
                        else -> R.string.sample_state_unchecked
                    }
                )
            this in listOf(FILTER_CHIP, ELEVATED_FILTER_CHIP, INPUT_CHIP) ->
                context.getString(
                    if (value == 1) R.string.sample_state_selected
                    else R.string.sample_state_not_selected
                )
            this == SINGLE_SEGMENTED ->
                if (value == 0) context.getString(R.string.sample_state_no_selection)
                else context.getString(R.string.status_selected, ('A'.code + value - 1).toChar())
            this == MULTI_SEGMENTED ->
                context.getString(
                    R.string.status_selected,
                    (0..2)
                        .filter { value and (1 shl it) != 0 }
                        .joinToString(", ") { ('A'.code + it).toChar().toString() }
                        .ifEmpty { context.getString(R.string.sample_state_none) },
                )
            isIconToggle || this == SWITCH || this == TOGGLE_BUTTON ->
                context.getString(
                    if (value == 1) R.string.sample_state_on else R.string.sample_state_off
                )
            this == CHECKBOX || this == CHECKED_TEXT_VIEW ->
                context.getString(
                    if (value == 1) R.string.sample_state_checked
                    else R.string.sample_state_unchecked
                )
            this == RADIO ->
                if (value == 0) context.getString(R.string.sample_state_no_selection)
                else
                    context.getString(
                        R.string.status_selected,
                        context.getString(if (value == 1) R.string.option_a else R.string.option_b),
                    )
            this == POPUP_MENU ->
                context.getString(
                    R.string.status_menu,
                    context.getString(
                        when (value) {
                            1 -> R.string.option_a
                            2 -> R.string.option_b
                            else -> R.string.sample_state_no_selection
                        }
                    ),
                    context.getString(
                        when (text) {
                            SampleMenuAction.OPENED.name -> R.string.action_opened
                            SampleMenuAction.SELECTED.name -> R.string.sample_state_selected
                            SampleMenuAction.DISMISSED.name -> R.string.action_dismissed
                            else -> R.string.action_not_opened
                        }
                    ),
                )
            this == RATING -> context.getString(R.string.status_rating, value)
            this == NUMBER_PICKER -> context.getString(R.string.status_number, value)
            this == SLIDER || isDeterminateProgress ->
                context.getString(R.string.status_value, value)
            this == DATE_PICKER_DIALOG ->
                context.getString(
                    R.string.status_date_action,
                    SampleDates.format(dateUtcMillis, context.resources.configuration.locales[0]),
                    context.getString(dialogAction(value)),
                )
            this == TIME_PICKER_DIALOG || isInlineTime -> {
                val locale = context.resources.configuration.locales[0]
                val pattern =
                    android.text.format.DateFormat.getBestDateTimePattern(
                        locale,
                        if (time24Hour) "Hm" else "hm",
                    )
                val formatted = SampleTimes.format(timeMinutes, locale, pattern)
                if (isInlineTime) context.getString(R.string.status_inline_time, formatted)
                else
                    context.getString(
                        R.string.status_time_action,
                        formatted,
                        context.getString(dialogAction(value)),
                    )
            }
            this == DIALOG || this == BASIC_ALERT_DIALOG ->
                context.getString(R.string.status_action, context.getString(dialogAction(value)))
            else -> context.getString(R.string.status_clicks, value)
        }
    }

    private fun dialogAction(value: Int): Int =
        when (value) {
            1 -> R.string.action_opened
            2 -> R.string.action_confirmed
            3 -> R.string.action_cancelled
            4 -> R.string.action_dismissed
            else -> R.string.action_not_opened
        }
}
