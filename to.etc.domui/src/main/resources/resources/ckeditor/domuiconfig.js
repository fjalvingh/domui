/**
 * @license Copyright (c) 2003-2013, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.html or http://ckeditor.com/license
 */

CKEDITOR.editorConfig = function( config ) {
	// Define changes to default configuration here.
	// For the complete reference:
	// http://docs.ckeditor.com/#!/api/CKEDITOR.config

	// The toolbar groups arrangement, optimized for two toolbar rows.
	config.toolbarGroups = [
		{ name: 'clipboard',   groups: [ 'clipboard', 'undo' ] },
		{ name: 'editing',     groups: [ 'find', 'selection', 'spellchecker' ] },
		{ name: 'links' },
		{ name: 'insert' },
		{ name: 'forms' },
		{ name: 'tools' },
		{ name: 'document',	   groups: [ 'mode', 'document', 'doctools' ] },
		{ name: 'others' },
		'/',
		{ name: 'basicstyles', groups: [ 'basicstyles', 'cleanup' ] },
		{ name: 'paragraph',   groups: [ 'list', 'indent', 'blocks', 'align', 'bidi' ] },
		{ name: 'styles' },
		{ name: 'about' }
	];

	config.toolbar_TXTONLY = [
   	   	['Bold','Italic','Underline','Strike','-','Cut','Copy','Paste','-','Undo','Redo']
    ] ;

	config.toolbar_BASIC = [
   	   	['Bold','Italic','Underline','Strike','-','Cut','Copy','Paste','-','Undo','Redo'],
   	   	['Styles','Format','Font','FontSize']
    ] ;
	
	config.toolbar_FULL = [
   	   	['Bold','Italic','Underline','Strike','-','Cut','Copy','Paste','-','Undo','Redo','-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock','-','NumberedList','BulletedList','Outdent','Indent'],
   	   	['Link','Unlink','-','DomUI_Image','Table','HorizontalRule','Smiley','DomUI_OddChar'],
   	   	['TextColor','BGColor','Styles','Format','Font','FontSize'],['Maximize']
    ] ;
	
	// The toolbar for DomUI set.
	config.toolbar_DOMUI = [
	   	['Bold','Italic','Underline','Strike','-','Cut','Copy','Paste','-','Undo','Redo','-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock','-','NumberedList','BulletedList','Outdent','Indent'],
	   	['Link','Unlink','-','DomUI_Image','Table','HorizontalRule','Smiley','DomUI_OddChar'],
	   	['TextColor','BGColor','Styles','Format','Font','FontSize'],['Maximize']
    ] ;
	
	
	// Remove some buttons, provided by the standard plugins, which we don't
	// need to have in the Standard(s) toolbar.
	//config.removeButtons = 'Underline,Subscript,Superscript';

	// Se the most common block elements.
	config.format_tags = 'p;h1;h2;h3;pre';

	// Make dialogs simpler.
	//config.removeDialogTabs = 'image:advanced;link:advanced';
	
	config.resize_enabled = true;
	
	config.toolbarCanCollapse = true;
	
	// Using a color code.
	//config.uiColor = '#AADC6E';
	
	// Using an HTML color name.
	//config.uiColor = 'Gold';	
};
