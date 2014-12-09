main_color="#54b4f8"; //Main blue
header_bg="#7c7c7c";

bg_color="#ffffff";
bg2_color=util.color(bg_color).lume(0.3);
body_image='bg-body-domui.png';

//-- Main page title gradient
bg_ttl_gradient = "bg-ttl-domui.png";
bg_ttl_end = '#7c7c7c';
bg_ttl_text = '#ffffff';

button_bg="#6ba1eb";
button_fg="#ffffff";

link_color="#2200cc";	// dark blue

highlight_bg="#ffbb43";	// row select: hard orange
highlight2_bg = util.color(highlight_bg).lighter(0.5);

//-- Header colors
data_tbl_header_bg="#7c7c7c";
data_tbl_header_text_color="#ffffff";
data_tbl_header_bg_img=""; //-- Table header doesn't have background image
data_tbl_header_btm_border=undefined;

//-- Selects and highlights ---
data_tbl_selected_bg="#bed6f8";	// blue
data_tbl_even_row_bg="#E5E5E5"; //greyish
data_tbl_cell_highlight_bg="#67267f";	// cell select: purple
data_tbl_cell_highlight_link_color_bg="#ffffff";	// link color of cell select: white
data_tbl_font_size = "12px";
data_tbl_expanding_row_bg = util.color(main_color).lighter(0.7);

readonly_bg="transparent";
readonly_border="#EEEEEF"; // bit darker grey

errors_background="#a9c5f1";	// red/pink light
errors_border="#ff0000"; 		// red
errors_foreground="#ff0000";	// red
errors_input_background="#ffe5e5"; // error bg for input component

warnings_background="#fffeee"; // light yellow
warnings_foreground="black";
warnings_border="yellow";

info_bg="#e4eeff";		// blueish light
info_fg="#4075db";
info_border="#4075db";

// row odd/even
dt_rowhdr_bg="#98bbf3";
dt_rowhdr_fg="#ffffff";

//-- input bevel: darker grey / lighter grey
bevel_up="#ABADB3";
bevel_down="#BFCDD9";

//-- hovel input bevel for a component: dark blue/light blue
bevel_hover_up="#5794BF";
bevel_hover_down="#D7E8F8";

//-- button color
button_bg="#6290E1";
button_text_color="#ffffff";
button_img= "defaultButton.png";

//-- Caption
caption_separator="hr-caption.png";

//-- selected items background
selected_bg="#ff9436";

//-- Tab panel
tab_pnl_inactive_col = "#ffffff";
tab_pnl_hover_inactive_col = "#ffffff";
tab_pnl_active_col = "#67267f";
tab_pnl_btm_border = "0px";

tab_pnl_sep_bg = "#7c7c7c";
tab_pnl_arrows = "tab-scrl-icon.png";

//-- Caption header
caption_bg=main_color;

//-- Bulk upload
upl_runing=highlight_bg;
upl_loading=data_tbl_cell_highlight_bg;

//loading bar
io_blk_wait = "io-blk-wait.gif";/* Icon definition for DomUI icons */

/*** Data Pager buttons ***/
data_pager_icons= "data-pager-icons.png";
data_pager_icon_size_x = 32;
data_pager_icon_size_y = 19;

//-- Tab panel image
tab_pnl_img = "tab-all-domui.png";
tab_pnl_close = "tab-pnl-close.png";
tab_pnl_close_hover = "tab-pnl-close-hover.png";
/* Icon definition for DomUI icons */

//label selector color
label_selector_item = '#bfd1f3';

//multiple lookup labels
multiple_lookup_label_color = '#ffa21e';
multiple_lookup_label_border = '#fff2ab';
