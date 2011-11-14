//DomUI customizations

FCKConfig.DefaultLanguage		= 'nl' ;

FCKConfig.Plugins.Add('domuiimage', 'nl,en');
FCKConfig.Plugins.Add('domuioddchars', 'nl,en');

FCKConfig.ToolbarSets["DomUI"] = [
                              	['Source','Preview', 'Templates'],
                              	['Cut','Copy','Paste','PasteText','PasteWord','-','Print','SpellCheck'],
                              	['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
                              	'/',
                              	['Bold','Italic','Underline','StrikeThrough','-','Subscript','Superscript'],
                              	['OrderedList','UnorderedList','-','Outdent','Indent','Blockquote'],
                              	['JustifyLeft','JustifyCenter','JustifyRight','JustifyFull'],
                              	['Link','Unlink','Anchor'],
                              	['Image','Table','Rule','Smiley','SpecialChar','PageBreak'],
                              	'/',
                              	['Style','FontFormat','FontName','FontSize'],
                              	['TextColor','BGColor'],
                              	['FitWindow','ShowBlocks']		// No comma for the last row.
                              ] ;

FCKConfig.ToolbarSets["NewMessage"] = [
 	['Bold','Italic','Underline','StrikeThrough','-','Cut','Copy','Paste','-','Undo','Redo','-','JustifyLeft','JustifyCenter','JustifyRight','JustifyFull','-','OrderedList','UnorderedList','Outdent','Indent','-','Link','Unlink','-','DomUI_Image','Table','Rule','Smiley','DomUI_OddChar'],
	'/',
	['TextColor','BGColor','FontFormat','FontName','FontSize']
  ] ;

