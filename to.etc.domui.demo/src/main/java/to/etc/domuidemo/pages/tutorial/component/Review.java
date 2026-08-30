package to.etc.domuidemo.pages.tutorial.component;

import to.etc.annotations.GenerateProperties;
import to.etc.domui.derbydata.db.Album;

/**
 * Tutorial, "writing a component": the model the bound page edits.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
@GenerateProperties
public class Review {
	private String m_reviewer;

	private Integer m_rating;

	private Album m_album;

	public String getReviewer() {
		return m_reviewer;
	}

	public void setReviewer(String reviewer) {
		m_reviewer = reviewer;
	}

	public Album getAlbum() {
		return m_album;
	}

	public void setAlbum(Album album) {
		m_album = album;
	}

	public Integer getRating() {
		return m_rating;
	}

	public void setRating(Integer rating) {
		m_rating = rating;
	}
}
