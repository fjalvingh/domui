package to.etc.domuidemo.pages.overview.tree2;

import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.derbydata.db.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 22-8-17.
 */
public class DemoNode {
	final private DemoNode m_parent;

	private String m_letter;

	private Artist m_artist;

	private Track m_track;

	private Album m_album;

	private List<DemoNode> m_children;

	public DemoNode(DemoNode parent, Artist artist) {
		m_parent = parent;
		m_artist = artist;
	}

	public DemoNode(DemoNode parent, Track track) {
		m_parent = parent;
		m_track = track;
	}

	public DemoNode(DemoNode parent, Album album) {
		m_parent = parent;
		m_album = album;
	}

	public DemoNode(List<Artist> artists) {
		m_parent = null;

		Map<String, List<Artist>> collect = artists.stream()
			.sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
			.collect(Collectors.groupingBy(a -> a.getName().substring(0, 1).toUpperCase()
			));
		List<DemoNode> rootList = new ArrayList<>();
		collect.forEach((letter, list) -> {
			List<DemoNode> nodeList = new ArrayList<>();
			DemoNode letterNode = new DemoNode(this, letter, nodeList);
			list.stream().map(a -> new DemoNode(letterNode, a)).forEach(n -> nodeList.add(n));
			rootList.add(letterNode);
		});
		m_children = rootList;
	}

	public DemoNode(DemoNode parent, String letter, List<DemoNode> list) {
		m_parent = parent;
		m_letter = letter;
		m_children = list;
	}

	List<DemoNode> getChildren() throws Exception {
		List<DemoNode> children = m_children;
		if(children == null) {
			children = m_children = loadChildren();
		}
		return children;
	}

	private List<DemoNode> loadChildren() throws Exception {
		Album album = m_album;
		Artist artist = m_artist;
		Track track = m_track;
		List<DemoNode> res = new ArrayList<>();
		if(artist != null) {
			for(Album a : artist.getAlbumList()) {
				res.add(new DemoNode(this, a));
			}
			Collections.sort(res, (a, b) -> a.m_album.getTitle().compareToIgnoreCase(b.m_album.getTitle()));
		} else if(album != null) {
			for(Track tr : album.getTrackList()) {
				res.add(new DemoNode(this, tr));
			}
		} else if(track != null) {
			// as is - no kids here
		} else if(m_letter != null) {
			// -- must be there
		} else {
			throw new IllegalStateException("??");
		}

		return res;
	}

	public DemoNode getParent() {
		return m_parent;
	}

	public String getIcon() {
		if(m_album != null) {
			return "fa-th-list";
		} else if(m_artist != null) {
			return "fa-group";
		} else if(m_track != null) {
			return "fa-music";
		} else if(m_letter != null) {
			return "fa-folder";
		} else {
			return "fa-question-circle-o";
		}
	}

	public String getText() {
		Album album = m_album;
		Artist artist = m_artist;
		Track track = m_track;
		String letter = m_letter;
		if(null != album)
			return album.getTitle();
		if(null != artist)
			return artist.getName();
		if(null != track)
			return track.getName();
		if(letter != null)
			return m_letter;

		return "??";
	}

	public boolean hasChildren() {
		if(m_children != null)
			return m_children.size() > 0;
		Album album = m_album;
		Artist artist = m_artist;
		Track track = m_track;
		if(artist != null)
			return artist.getAlbumList().size() != 0;
		if(album != null)
			return album.getTrackList().size() != 0;
		return false;
	}
}
