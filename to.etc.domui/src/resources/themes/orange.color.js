//------------------------------------------------------
//-- Tab panel colors for the primary body color schemes
//------------------------------------------------------
// tab panels color together with the primary bg color.
tabPanelDomui = {
	sep: "#7c7c7c",
	inactive_col: '#ffffff',
	active_col: '#4272C8',
	hover_inactive_col: '#ffffff',
	hover_active_col: '#4272C8',
	border: undefined,						// jal wtf is this? Without this the tabs render bad... Vladimir?
	border_active: 'transparent',
	img: 'tab-all-domui.png',
	topImg: 'tab-all-top-grey.png',
	arrows: 'tab-scrl-icon.png',
	btm_border: '0px'
};

//---------------------------------------------------------
//-- Data table's colors for the primary body color schemes
//---------------------------------------------------------
dataTableDomui = {
		even_row_bg: "#E5E5E5", 
		header_bg: "#7c7c7c",
		header_bg_img: "",
		header_text_color: "#ffffff",
		header_btm_border: "undefined",
		cell_highlight_bg: "#67267f",
		cell_highlight_link_color_bg: "#ffffff",
		font_size: "11px",
		expanding_row_bg: "#efb87c"
};

//-------------------------------- 
//-- Data pager
//--------------------------------
dataPagerDomui = {
		icons: "data-pager-icons.png",
		icon_width: "32",
		icon_height: "19"
};

//-------------------------------- 
//-- Primary background selections
//-------------------------------- 
backgroundMap = new Object();		// REQUIRED NAME - used in VisualThemeEditPage
backgroundMap["domui"] = {
	name: 'Lichtgrijze achtergrond met verloop',
	image: 'bg-body-gray-dithered.png',
	bgcolor: '#ffffff',
	tabPanel: tabPanelDomui,
	dataTable: dataTableDomui,
	dataPager: dataPagerDomui
};

// "theme_body_set" is set in VpThemeFactory from VisualTheme.getBodyColor().
theme_body_set = 'domui';
currentBackground = backgroundMap[theme_body_set];
bg_image = currentBackground.image;
bg_body_color = currentBackground.bgcolor;

//------------------------ 
//-- Button set selections
//------------------------
buttonMap = new Object();			// REQUIRED NAME - used in VisualThemeEditPage
buttonMap["domui"] = {
	name: 'Blauwe knoppen',
	img: "btn-blue-all.png"
};

button_img= "defaultButton.orange.png";
button_height=24;
highlight_bg= "#ffbb43";

//------------------------------------------------------------
//-- The tab panel's colors color with the selected background
//------------------------------------------------------------
currentTab = currentBackground.tabPanel;
tab_pnl_img = currentTab.img;
tab_pnl_top_img = currentTab.topImg;
tab_pnl_sep_bg = currentTab.sep;
tab_pnl_inactive_col = currentTab.inactive_col;
tab_pnl_active_col = currentTab.active_col;
tab_pnl_hover_inactive_col = currentTab.hover_inactive_col;
tab_pnl_hover_active_col = currentTab.hover_active_col;
tab_pnl_border_col = currentTab.border;
tab_pnl_active_border_col = currentTab.border_active;
tab_pnl_arrows = currentTab.arrows;
tab_pnl_btm_border = currentTab.btm_border;

//------------------------------------------------------------
//-- The data table's colors. 
//------------------------------------------------------------
currentDataTable = currentBackground.dataTable;
data_tbl_even_row_bg = currentDataTable.even_row_bg;
data_tbl_header_bg = currentDataTable.header_bg;
data_tbl_header_bg_img = currentDataTable.header_bg_img;
data_tbl_header_text_color= currentDataTable.header_text_color;
data_tbl_header_btm_border = currentDataTable.header_btm_border;
data_tbl_cell_highlight_bg = currentDataTable.cell_highlight_bg;
data_tbl_cell_highlight_link_color_bg = currentDataTable.cell_highlight_link_color_bg;
data_tbl_font_size = currentDataTable.font_size;
data_tbl_expanding_row_bg = currentDataTable.expanding_row_bg;

//----------------------------
//-- The data pager icons.
//----------------------------
currentDataPager = currentBackground.dataPager;
data_pager_icons = currentDataPager.img;
data_pager_icon_size_x = currentDataPager.icon_width;
data_pager_icon_size_y = currentDataPager.icon_height;

//-- Main page title gradient
bg_ttl_gradient = "bg-ttl-domui.png";
bg_ttl_end = '#7c7c7c';
bg_ttl_text = '#ffffff';

// vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv Fix below here and move up.

link_color = "#2200cc";
highlight_bg = "#ffbb43";
highlight2_bg = util.color(highlight_bg).lighter(0.5);

//-- Data table header: make it color "with" the button color set.
dataColorMap = new Object();
dataColorMap["blue"] = "#98bbf3";
dataColorMap["fg-blue"] = "#ffffff";

dataColorMap["fg-sec-ttl-blue"] = "#7FA6EB"; //used for titled section caption 

dataColorMap["bg-hdr-ntbl-blue"] = "#6290E2"; //used for nested tables header and combo buttons menu
dataColorMap["fg-sec-ttl-grey"] = "#7FA6EB"; //used for titled section caption 

dataColorMap["red"] = "#fba9a2";
dataColorMap["fg-red"] = "#000000";

dataColorMap["bluemetal"] = "#646c83";
dataColorMap["fg-bluemetal"] = "#ffffff";

dataColorMap["grey"] = "#8c8d8f";
dataColorMap["fg-grey"] = "#ffffff";

dataColorMap["mntbl-bgcol-blue"] = "#eeeeee";
dataColorMap["mntbl-bgcol-grey"] = "#dddddd";

bset = "domui";

resourceNameMap = new Object();
resourceNameMap["bg-body-blue"] = "bg-body.png";
resourceNameMap["bg-body-grey"] = "bg-body-grey.png";

resourceNameMap["button_img-blue"] = "btn-blue-all.png";
resourceNameMap["button_img-grey"] = "btn-blue-all.png";

resourceNameMap["sec-sep-img-blue"] = "hr-blue.png";
resourceNameMap["sec-sep-img-grey"] = "hr-blue.png";

resourceNameMap["bg-info-panel-blue"] = "bg-info-pnl.png";

//popup menu table
mntbl_bgcol = dataColorMap["mntbl-bgcol-" + bset];

bg_info_panel = resourceNameMap["bg-info-panel-" + bset];

fg_sec_ttl = dataColorMap["fg-sec-ttl-" + bset]; 
sec_sep_img = resourceNameMap["sec-sep-img-" + bset];

bg_hdr_ntbl = dataColorMap["bg-hdr-ntbl-" + bset];

//tree menu icons
icon_tree_folder = "btnTreeFold.png"; 
icon_tree_open_folder = "btnTreeOpen.png"; 
icon_tree_item = "btnTreeItem.gif"; 
icon_tree_line_branch = "btnTreeLinelastnode.gif"; 
icon_tree_line_vertical = "btnTreeLinevertical.gif";

//loading bar. You can use http://ajaxload.info/ for generating them in different colors
resourceNameMap["io-blk-wait-blue"] = "io-blk-wait-blue.gif"; 
resourceNameMap["io-blk-wait-grey"] = "io-blk-wait.gif"; 
io_blk_wait = resourceNameMap["io-blk-wait-" + bset];

//vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv Missing in icon.props.js file
body_image='bg-body-domui.png';
bg_color="#ffffff";
bg2_color="#ffffff";

//-- input bevel: darker grey / lighter grey
bevel_up="#ABADB3";
bevel_down="#BFCDD9";

//-- hovel input bevel for a component: dark blue/light blue
bevel_hover_up="#5794BF";
bevel_hover_down="#D7E8F8";

//-- selected items background	
selected_bg="#ff9436";
caption_bg="#f0810a";
readonly_bg="transparent";

error_bg="#a9c5f1";	// red/pink light
error_border="#ff0000"; // red
error_fg="#ff0000"; // red
error_input_bg="#ffe5e5"; // error bg for input component

info_bg="#a9c5f1";		// blueish light
info_fg="blue";
info_border="blue";

header_bg="#7c7c7c";
link_font_size="12px";
link_color="#2200cc";	// dark blue

readonly_border="#EEEEEF"; // bit darker grey

warning_bg="#fffeee"; // light yellow
warning_border="yellow";
warning_fg="black";

//-- Bulk upload
upl_runing=highlight_bg;
upl_loading=data_tbl_cell_highlight_bg;
