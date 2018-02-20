package to.etc.domuidemo.pages.overview.input;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.SearchAsYouTypeBase;
import to.etc.domui.component.input.SearchAsYouTypeBase.IQuery;
import to.etc.domui.derbydata.db.Genre;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 18-2-18.
 */
public class DemoSearchAsYouTypeBase1 extends UrlPage {
	@Override public void createContent() throws Exception {
		//-- Make a set of cities.
		List<Genre> genreList = getSharedContext().query(QCriteria.create(Genre.class));

		IQuery<Genre> iq = new IQuery<Genre>() {
			@Nonnull @Override public List<Genre> queryFromString(@Nonnull String input, int max) throws Exception {
				List<Genre> list = new ArrayList<>();
				for(Genre genre : genreList) {
					if(genre.getName().toLowerCase().contains(input)) {
						list.add(genre);
						if(list.size() >= max)
							break;
					}
				}

				return list;
			}

			@Override public void onSelect(@Nonnull Genre instance) throws Exception {

			}

			@Override public void onEnter(@Nonnull String value) throws Exception {
			}
		};

		SearchAsYouTypeBase<Genre> searchInput = new SearchAsYouTypeBase<>(iq, Genre.class, "name");
		add(searchInput);

		Div d = new Div();
		add(d);

		DefaultButton b = new DefaultButton("validate", a -> {
			Div res = new Div();
			add(res);
			//res.add("Result is " + searchInput.getValue());
		});
		add(b);


		//LabelSelector<Genre> ls = new LabelSelector<>(Genre.class, new ISearch<Genre>() {
		//	@Nullable @Override public Genre find(@Nonnull String name) throws Exception {
		//		for(Genre genre : genreList) {
		//			if(genre.getName().equalsIgnoreCase(name))
		//				return genre;
		//		}
		//		return null;
		//	}
		//
		//	@Nonnull @Override public List<Genre> findLike(@Nonnull String input, int maxHits) throws Exception {
		//		List<Genre> res = new ArrayList<>();
		//		input = input.toLowerCase();
		//		for(Genre genre : genreList) {
		//			if(genre.getName().toLowerCase().contains(input)) {
		//				res.add(genre);
		//				if(res.size() >= maxHits)
		//					return res;
		//			}
		//		}
		//		return res;
		//	}
		//});
		//
		//add(ls);




	}
}
