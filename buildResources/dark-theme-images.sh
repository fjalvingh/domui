#!/bin/bash
#
# Makes the dark variant's copies of the winter theme's images.
#
# A file in themes/scss/winter/dark/ shadows the file of the same name in themes/scss/winter/
# for the dark variant - both for a url() in the stylesheet and for a "THEME/x.png" in Java -
# so an image that does not read on a dark page gets a dark copy here, and the light theme
# keeps its own. See darktheme.md, Phase 5.
#
# The transformation turns the lightness around and squeezes it into the dark palette's range,
# leaving hue and chroma alone: it works in Lab, where a near-white has no chroma and so
# becomes a neutral dark grey (in HSL a near-white like #FFF5F5 is fully saturated, and would
# turn deep red). White lands just above the page ground (#2B2B2B), black on light text.
#
#	normal	L' = 0.20 + (1 - L) * 0.72		icons, tree lines, washes, animations
#	glyph	L' = 0.45 + (1 - L) * 0.50		faint one-colour glyphs, which would otherwise
#						become just as faint on the dark tab and button grounds
#
# The images that read on a dark page as they are (most colour icons, the message icons) have
# no copy. Run from anywhere; needs ImageMagick 7 (magick). Re-run after changing a light image
# or the mapping, and review the result on the demo in the dark variant.

set -euo pipefail
W="$(cd "$(dirname "$0")/.." && pwd)/to.etc.domui/src/main/resources/resources/themes/scss/winter"
D="$W/dark"

NORMAL=(
	# surfaces drawn as images
	bg-errors.png bg-progress.png bg-rounded-left.png bg-rounded-middle.png bg-rounded-right.png
	dnd-separator.png data-pager-icons.png
	# tree lines and expanders
	tree-branch.png tree-closed-last.png tree-closed.png tree-leaf-last.png tree-leaf.png
	tree-opened-last.png tree-opened.png
	bg-tree-closed.png bg-tree-leaf.png bg-tree-node.png bg-tree-open-cont.png bg-tree-opened.png
	xdt-collapsed.png xdt-expanded.png
	# one-colour glyphs
	resize.png menuarrow2.gif paw.png secret.png btnShowDetails.png mandatoryField.png
	48x16_isct_erase.png
	# colour icons with a white body or dark detail
	btn-datein.png btnToday.png btn-popuplookup.png btn-hover-popuplookup.png btn-hover-ClearLookup.png
	btnClearLookup.png btnSave.png btnFind.png btnHideDetails.png ttlFind.png isct_empty.png
	iptPage.png dataExpired.png dpr-select-all.png dpr-select-none.png dpr-select-on.png
	dspcb-on.png dspcb-off.png btnEdit.png secured.png
	btnHeaderCollapsedNORMAL.png btnHeaderCollapsedSMALL.png btnHeaderExpandedNORMAL.png
	btnHeaderExpandedSMALL.png btnHeaderHamburger.png
	# animations with a white matte
	io-blk-wait.gif progressbar.gif lui-keyword-wait.gif asy-container-busy.gif
)

GLYPH=(
	tab-pnl-close.png tab-pnl-close-hover.png 22x11_tab-pnl-close.png
)

# Not here on purpose: tab-scrl-icon.png, ScrollableTabPanel's scroll arrows. They are white on
# the grey of the scroll buttons ($tab-sep-bg), which is a mid grey in both variants.

darken() {
	local name=$1 fx=$2
	if [[ $name == *.gif ]]; then
		magick "$W/$name" -coalesce -colorspace Lab -channel R -fx "$fx" +channel -colorspace sRGB -layers Optimize -strip "$D/$name"
	else
		magick "$W/$name" -colorspace Lab -channel R -fx "$fx" +channel -colorspace sRGB -strip "$D/$name"
	fi
}

for f in "${NORMAL[@]}"; do darken "$f" '0.20+(1-u)*0.72'; done
for f in "${GLYPH[@]}"; do darken "$f" '0.45+(1-u)*0.50'; done
echo "Made $(( ${#NORMAL[@]} + ${#GLYPH[@]} )) dark images in $D"
