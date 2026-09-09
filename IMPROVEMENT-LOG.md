# DomUI documentation & demo improvement log

The finished work of the running project whose remaining work is in
`IMPROVEMENT-PLAN.md`. Everything recorded here is done; the plan holds only what
is still open. The two were split on 2026-09-06, when the plan had grown to 3100
lines of which the greater part was history.

Nothing here is to be re-litigated. Read it to find out *why* something is the way
it is - the decisions log at the end is the reasoning behind the ticked boxes above
it.

## Completed work

### Phase 0 - Fix the basics first

The whole phase is done. It was walked through step by step, one item at a time, at
the user's direction: fix the basics before rewriting anything on top of them.

- [x] **Fix `CLAUDE.md`.** The main development branch is `skarp-master`, not
      `skarp-jakarta` (`origin/HEAD -> origin/skarp-master`; no local
      `skarp-jakarta` branch exists). While fixing that, two adjacent statements
      in the same file were verified and found wrong, and corrected:
      - The Jakarta migration is **complete**, not "currently undergoing". Every
        remaining `javax.*` import is a genuine JDK package (`javax.xml`,
        `javax.sql`, `javax.crypto`, `javax.imageio`, `javax.naming`,
        `javax.script`, `javax.tools`, `javax.lang.model`, `javax.net`,
        `javax.annotation.processing`) - none of them are Jakarta EE renames.
        The code uses `jakarta.persistence`, `jakarta.servlet` and `jakarta.el`.
      - Jetty is on **11.0.26**, not "9.4.x". The obsolete "Current Migration
        Notes" section was removed and the current fact recorded under Key
        Technical Details.

- [x] **Get the demo application into a better state (structural pass).** Three
      mechanical rules applied across `to.etc.domui.demo`; 118 files changed,
      module compiles and its 9 unit tests pass.
      - *Header-like components replaced by `HTag`.* `Caption` and
        `CaptionedHeader` are gone from every page that used them as an ordinary
        header (12 files). Convention applied: `HTag(1)` for a page's own title,
        `HTag(2)` for a section inside a page or fragment. `MenuPage.addCaption2()`
        was dead code (no callers) and was deleted with it.
        **Left alone deliberately** - these exist to show the raw component:
        `pages/overview/layout/DemoCaption`, `DemoCaptionedHeader`,
        `DemoCaptionedPanel`, `pages/basic/CaptionsDemoPage` and the panels/headers
        tab of `pages/BasicOverviewPage`.
      - *form4 `FormBuilder`.* Nothing to convert: `component2.form4.FormBuilder`
        is already the only form builder in the framework and all 25 demo imports
        were it. The real defect was elsewhere - 11 pages built a form with
        `new FormBuilder(this)`, which puts the form on the page and outside the
        content panel; those now pass the panel. `Text2LayoutTestPage`'s
        "Without form4 builder" section is a deliberate raw comparison and stays.
      - *Every page's content inside a `ContentPanel`* (`.ui-cpnl` is what supplies
        the page padding). 114 of 129 direct `UrlPage` subclasses now have one.
        Where a helper method or a click handler adds to the page, the panel is
        held in an `m_cp` field, matching the existing style in `MenuPage`.
        `AbstractSearchPage` and `BasicPage` gained a `contentPanel()` accessor so
        their subclasses share one panel rather than each making their own.
        **Deliberately without a panel:** the six `MenuPage` subclasses (their
        `dm-content-links` / `dm-expl-panel` CSS already supplies margins, so a
        panel would double-inset them); the eight `pages/test/msgbox/*` pages
        (they only open a MsgBox and have no page content); `Application`,
        `MenuPage`, `WikiExplanationPage` and `TrackDetails` (not content pages);
        and `OldHome` / `BasicPage`, see below. Overlays stay attached to the page
        itself, not the panel: the `FloatingWindow` in `BasicOverviewPage` and the
        title bar in `BasicPage`.

- [x] **Syntax highlighting for the demo's source viewer.** `JavaHighlighter` was a
      stub that rendered every line as one plain `text` token, and - more to the
      point - `HighlighterFactory` only ever registered `sql`, so `.java` files got
      a `NullHighlighter` and the class could never have run. Finished it following
      `SqlHighlighter`: `HiParser` already implements Java's lexical structure
      (both comment forms, string escapes including `\uXXXX`, and the hex/binary/
      octal/underscore numeric formats), so the highlighter only adds keyword sets -
      case *dependent*, unlike SQL. Four groups: `type` (primitives plus the
      java.lang types visible without an import), `keyword1` (reserved words),
      `keyword2` (contextual: `var`, `record`, `sealed`, `permits`, `yield`) and
      `keyword3` (`true`, `false`, `null`). Registered `java` in the factory, and
      added the `s-*` token CSS to the demo's `_syntax.scss`, which only styled
      `s-keyword1` and `s-keyword2` and would have left everything else black.
      Covered by a round-trip + golden-file test alongside the SQL ones.
      - Found and fixed a real defect in the shared `HiParser` while verifying:
        base-10 literals only consumed a `d`/`D` suffix, so `123456789L` lexed as
        a number followed by an identifier `L`. It now accepts `l/L/f/F/d/D`. The
        SQL golden files are unaffected.
      - Known limitation, left alone: `HiParser` hardcodes both quote characters to
        the `string` token type, so Java char literals are `string` rather than the
        `character` type the enum defines. Distinguishing them needs a new hook in
        `HiParser`; both are styled identically, so it is invisible. Java text
        blocks (`"""`) are likewise not understood.

- [x] **CD shop application pages.** The demo database (the Chinook model: artist -
      album - track, customer - invoice - invoice line, employee) only had screens
      for albums and track search. Added the missing ones as an application rather
      than as one list+edit pair per table, under `pages/cddb`, reachable from a
      new `CdShopMenuPage` linked from the demo home page:
      - `ArtistListPage` -> `ArtistDetailPage`: the artist with **its albums** as
        the bound detail part, each album linking on to the existing album screen.
      - `CustomerListPage` -> `CustomerDetailPage`: contact and address details
        with the customer's **purchase history** below, linking to the invoice.
      - `InvoiceListPage` -> `InvoiceDetailPage`: invoice header with the **sold
        lines** as the bound detail part, each line linking to its track.
      - `EmployeeListPage` -> `EmployeeDetailPage`: an employee with two detail
        tabs - **who reports to them** (bound) and **which customers they support**
        (queried).
      - `ReferenceDataPage`: the genre and media-type lookup tables maintained as
        **editable tables**, each row's control bound straight to its record.
      - `TrackDetails` was an empty stub that `CdCollection` already navigated to,
        so clicking a track search result led to a blank page. It is now the track
        screen. Its navigation was broken too: `CdCollection` passed the entity
        itself to `UIGoto.moveSub`, which makes `PageParameters` derive a parameter
        named after the class; it now uses the `"id"` convention the other pages use.
      - `AbstractCdShopListPage` holds the shared search-plus-result behaviour, so
        each list page only declares its columns and where a row click goes.
      Verified by running the demo under jetty and fetching every page: all 14
      render without exceptions, and the detail tables contain real data (artist 1
      has its 2 albums, invoice 1 its lines, employee 2 its 3 direct reports).
      Interactive flows (pressing Search, pressing Save) were not clicked through.
      - Fixed a framework defect this depended on: `ChildFragment`, the master/detail
        component, declared its constructor as `QField<P, C>` while the class treats
        `C` as the collection's *element* type. `C` was therefore inferred as
        `List<Album>`, making the documented `ChildFragment<Artist, Album>`
        impossible to declare. It is now `QField<P, List<C>>`. The one existing
        caller (`AlbumEditPage`) is unaffected.
      - Found that entity label bundles **never loaded**: `Artist.properties` and
        `Invoice.properties` sat in `src/main/java` with no `<resources>` entry to
        copy them, so they never reached the classpath and every label in the demo
        showed the raw property name (`mediaType`, `supportRepresentative`). Moved
        them to `src/main/resources` and added bundles for the other entities, so
        labels now read properly everywhere - including on the pre-existing screens.

- [x] **Documentation: "Building your first page".** The `building-pages` section
      opened with technical leaf pages and had nothing that walks someone through
      writing a page, so it now starts with
      `site/content/building-pages/first-page/index.md`: a page is a class
      extending `UrlPage`, the fully qualified class name plus `.ui` is its URL,
      what `createContent()` is and when it runs (constructor -> `@UIUrlParameter`
      injection -> `createContent()`, once per build, `forceRebuild()` to build
      again), tags nesting into a tree, and a click handler. Tags only - no
      components, no binding - both of which it points at for the next step.
      Three demo pages carry it, in `pages/tutorial/first`: `HelloPage` (a div
      with text), `HelloTreePage` (a tree of tags) and `HelloClickPage` (a click
      handler toggling the div's colour), linked from `TutorialListPage` under a
      new "Building your first page" caption, with a `.dm-tut` class added to the
      demo's `_panels.scss`. Verified by running the demo under jetty: all three
      render, and the click round trip returns exactly the one-attribute delta
      the page describes.
      - The three `!demo()` iframes point at https://demo.domui.org/ and stay
        empty until the demo is redeployed (`scripts/deploy-demo`).
      - Corrected while writing it: `setPageTitle()` sets the *page name* used by
        the title bar and breadcrumb, not the browser tab title - that is the
        `title` property, falling back to `DomApplication.getDefaultPageTitle()`.
      - The rest of `building-pages` is untouched on purpose; moving or deleting
        those pages comes later.

- [x] **Documentation: "Using components".** The second page of the walkthrough,
      `site/content/building-pages/using-components/index.md`: a component is a
      node that builds itself (`Text2<T> extends Div`, `DefaultButton extends
      Button`, the same `createContent()` mechanism a page uses, layer 0 versus
      layer 1, `forceRebuild()` when a property changes the presentation); a form
      built with `component2.form4.FormBuilder` without binding; the state every
      `IControl<T>` has (value, readOnly, disabled, disabledBecause, mandatory)
      and the fact that each control shows readOnly its own way; `getValue()` as
      the place where mandatory, conversion and validators are checked, posting a
      `UIMessage` to the error fence and throwing a `ValidationException` that the
      framework - not the handler - catches; and `setOnValueChanged` with the
      request order (raw values in, change handlers, then the action). No data
      binding and no database, on purpose - binding is the next step.
      Three demo pages carry it, in `pages/tutorial/components`:
      `ComponentFormPage` (Text2<String/Integer/BigDecimal>, DateInput2,
      ComboFixed2, two buttons reading the values with `getValue()` and with
      `hasError()`), `ComponentStatePage` (four buttons switching all three
      controls between editable, read only, disabled and disabledBecause) and
      `ComponentChangePage` (two change handlers recomputing a total). Linked from
      `TutorialListPage` under a new "Using components" caption.
      Verified by running the demo under jetty and driving it in Chrome: the
      mandatory error renders as "**Album title:** Mandatory field" in the
      auto-inserted `ErrorPanel` with the field in red, an Integer field holding
      "abc" gives "The field content "abc" is invalid", readOnly turns the combo
      into plain text while disabledBecause leaves it a greyed select, and
      changing a field and leaving it updates the total without a button.
      - Found while writing it: `DateInput2` reacts to an unparseable date with a
        client-side `alert()` (`WebUI.dateInputCheckInput` -> `dateInputRepairValueIn`
        in `domui.dateinput.ts`), which blocks the browser rather than using the
        framework's own error reporting. Left alone - not this step's scope, but a
        candidate.
      - `Text2<BigDecimal>` accepts "1.2.3" and silently converts it to 1.2; the
        conversion example in the documentation therefore uses an Integer field.
      - Reworked directly afterwards to the style decided on 2026-08-30 (see the
        decisions log): each section now opens with its example and explains afterwards,
        the forward references to data binding are gone, three plantuml diagrams carry
        what prose was carrying (the tree a `Text2` builds, the `getValue()` decision
        path, the round trip of a changed field), and all three demo pages were rewritten
        to keep their controls in local variables. `ComponentStatePage` now keeps only
        readOnly/disabled/disabledBecause in fields and calls `forceRebuild()`, which is
        the pattern the documentation points at as the reason components are not fields.

- [x] **Documentation: "Using databases".** The third page of the walkthrough,
      `site/content/building-pages/30-using-databases/index.md`, started from
      `data/qcriteria` and rewritten in the agreed style. It opens with the two
      classes the rest depends on - `QCriteria<T>` as the question and
      `QDataContext` as the thing that runs it - and `getSharedContext()` as where
      a page gets the latter; then a first query (restrictions, ordering, limit,
      typed results, property paths rather than column names, the value travelling
      as a JDBC parameter); then what a `QDataContext` is and how the shared one
      lives (opened during a request, closed when the conversation detaches, its
      `close()` ignored) with the useful calls tabled; then restrictions and
      combinators (and by default, `or()`/`and()`/`not()` returning a restrictor,
      levels of the same kind merging) and finally querying over a relation - a
      dotted path upwards, `exists()` downwards, and why the child condition is a
      subselect rather than a join (`limit()` counts entities). Two plantuml
      diagrams: query-versus-context, and the and/or tree. No data binding and no
      `DataTable`, on purpose; a single pointer at the end to the in-depth page
      (now `70-implementation-details/qcriteria`) for the reference-level material.
      Three demo pages carry it, in `pages/tutorial/database`: `QueryFirstPage`
      (ilike/ascending/limit on `Album`), `QueryRestrictionsPage` (an `or()` over
      name and composer, anded with a duration, showing the query's own
      `toString()` in a new `.dm-tut-q` box) and `QueryJoinPage` (albums by
      `artist.name`, and artists by `exists(Album.class, "albumList")`). Linked
      from `TutorialListPage` under a new "Using databases" caption.
      Verified by running the demo under jetty and fetching all three: "rock"
      finds 7 albums, the restrictions page renders
      `WHERE (name ilike '%brown%' or composer ilike '%brown%') and milliseconds>=240000L`
      with 5 matching tracks, and the exists query returns 11 artists once each.
      The site builds clean, both diagrams render and all three `!demo()` iframes
      resolve.
      - Found while writing it: the demo database itself holds truncated artist
        and track names - `CreateDB.sql` has both `Led Zeppeli` and
        `Led Zeppelin` as separate artists, plus `Iron Maide`,
        `Yamma Brow`, `Talkin Loud and Saying Nothi`. Not a rendering bug; the
        insert statements are like that. Left alone - a candidate, below.
      - ~~`data/qcriteria` now overlaps this page and still carries stale material
        (an `examples/tutorial` module that no longer exists, an `Album.year`
        property the entity does not have, Confluence-hosted images). It wants
        triage in phase 2 as the reference page this one points at.~~ Done
        2026-08-31: it became `70-implementation-details/qcriteria` and was
        rewritten as the layer's own page (see the decisions log).

- [x] **Documentation: "Typed properties".** The fourth page of the walkthrough,
      `site/content/building-pages/40-typed-properties/index.md`, taking its
      material from `data/data-binding/typed-properties` and rewritten in the
      agreed style, without any data binding (that is the next page). It opens
      where the previous page left off - the four things that can be wrong with
      `q.ilike("artist.name", part)` that the compiler cannot see (typo, rename,
      wrong value type, wrong entity) - then shows the same query typed, and the
      three compile errors those mistakes now produce, quoted from an actual
      javac run. Then what `Album_` is (`QField<R, P>`, and the `Artist_Link<R>`
      that is itself a `QField<Album, Artist>`, which is what makes chaining
      work) with a plantuml class diagram; paths of any depth and `exists()`
      still needing the child class; a typed property as an ordinary value; and
      finally turning it on - `@Entity` (jakarta) or `@GenerateProperties`, the
      maven-compiler-plugin block, the `to.etc:annotations` dependency, the ecj
      caveat, where the generated sources land, which property types are
      generated and `@IgnoreGeneration` on the getter.
      Three demo pages carry it, in `pages/tutorial/typed`: `TypedQueryPage`
      (the previous page's album search with `Album_.title()` and
      `Album_.artist().name()`), `TypedPathPage`
      (`Track_.album().artist().name()`, and
      `exists(Album.class, Artist_.albumList())`) and `TypedGenericPage` (one
      generic `listOf(List<T>, QField<T, String>)` helper rendering artists,
      albums and tracks with no cast and no `Class<T>` parameter, via
      `MetaManager.getPropertyMeta(clz, QField)`). Linked from
      `TutorialListPage` under a new "Typed properties" caption.
      Verified by running the demo under jetty and driving it in Chrome: all
      three render, the query box shows the typed path arriving as the plain
      string it always was (`artist.name`, `album.artist.name`), and searching
      for artist "AC/DC" on top of title "rock" produces
      `title ilike '%rock%' and artist.name ilike '%AC/DC%'` with the two
      expected albums. The three compile errors quoted in the page were produced
      by compiling them, not assumed. The site builds and the class diagram
      renders.
      - Corrected against the old page while writing: the processor triggers on
        **`jakarta.persistence.Entity`** (the old page says `javax.*`); the
        version floor for `plexus-compiler-eclipse` is 2.8.4 and the repo is on
        2.8.5, so the old page's "to be released" note is gone; the generated
        sources land in `target/generated-sources/annotations`, not
        `target/annotations`; and a getter carrying `@Column` is generated
        whatever its type, which the old "what is generated" rules did not say.
      - **`data/data-binding/typed-properties/index.md` was superseded by this
        page** and was stale in the ways just listed, plus a "recent addition,
        work in progress" banner and 2018 IntelliJ screenshots. Rather than being
        deleted it became `70-implementation-details/typed-properties` on
        2026-08-31, rewritten as the processor's own page (see the decisions log);
        the five links to it were repointed to this page.

- [x] **Documentation: "Data binding".** The fifth page of the walkthrough,
      `site/content/building-pages/50-data-binding/index.md`, consolidating
      `data/data-binding` and `data/data-binding/how-does-it-work` and rewritten
      in the agreed style, with typed properties used throughout. It opens with
      the screen written without binding - ten lines of carrying values, in two
      places that must stay in step - then the same screen bound, where a handler
      that only calls `m_order.setPrice(ZERO)` changes what is on screen; then
      `FormBuilder.property().control()` as one line per field; then when the
      moving happens (soft binding, the two moments in a request, bindings living
      inside the control so they die with it); then bidirectional value binding
      versus unidirectional binding to any other control property, with the
      `IControl.DISABLED` / `READONLY` and `CssBase.VISIBILITY` / `DISPLAY`
      constants tabled; then `bindErrors()` and why bindings read `bindValue`
      rather than `value`; and finally `StyleBinder` / `StyleBinding`. Three
      plantuml diagrams: the request round trip, which way each kind of binding
      moves, and what a binding does with a value it cannot deliver. A single
      closing pointer to `how-does-it-work` for binding order and the
      changed-value pitfalls.
      Five demo pages carry it, in `pages/tutorial/binding`, plus two model
      classes: `AlbumOrder` (a plain `@GenerateProperties` class - no entity is
      edited, so an embedded demo cannot write to the demo database) with an
      `AlbumOrder.properties` label bundle, and `SendInfoModel`.
      `BindByHandPage`, `BindValuePage`, `BindPropertyPage`, `BindErrorsPage`
      and `BindStylePage`, linked from `TutorialListPage` under a new "Data
      binding" caption. Four order-state colours added to the demo's
      `_panels.scss`.
      Verified by running the demo under jetty and driving all five in Chrome:
      typing in a bound field updates the read-only mirror bound to the same
      property; "Clear the price" sets both price fields to 0 from a handler that
      touches only the model; the Send button is disabled until both combos have
      a value and then enables itself; Save with an empty mandatory Customer and
      "abc" in Copies shows **Customer:** Mandatory field and **Copies:** The
      field content "abc" is invalid, saves nothing, and saves correctly once
      fixed; and the style-bound box changes colour with the order state. The
      site builds and all three diagrams render.
      - `BindPropertyPage` was first written with `LookupInput2`, as the old page
        has it. Its lookup popup is nearly full-page, so inside a `!demo()` iframe
        it is unusable; the page now uses two `ComboLookup2` with `limit(20)`.
        Worth remembering for any tutorial page that will be embedded.
      - **Fixed a defect in the demo data model that this page hit.**
        `FormBuilder.property(m_order, AlbumOrder_.genre()).readOnly().control()`
        picks a lookup for an entity-typed property, and rendering a *selected*
        value in one needs presentation metadata - so choosing a genre threw
        `The class ClassMetaModel[...Genre] has no presentation metadata
        (@MetaObject or @MetaCombo)` from `SimpleLookupInputRenderer`. `Genre` and
        `MediaType` were the only two of the nine derbydata entities without
        `@MetaObject`; both now carry
        `@MetaObject(defaultColumns = {@MetaDisplayProperty(name = "name")}, defaultSortColumn = "name")`
        like `Artist` does. It only surfaces once a value is selected, which is
        why the first pass through the screen missed it. Verified: the read-only
        genre now renders "Jazz", `ReferenceDataPage` and `TrackDetails?id=1`
        still render, and the module's 9 unit tests pass.
      - The old `pages/binding/tut1/**` pages stay for now: they are what
        `TutorialListPage`'s "Binding tutorial" caption still points at, they use
        `Text` and `TextStr` (pre-`component2`) and `InvoiceListPage` extends the
        `@Deprecated` `BasicPage`. Deleting them is a phase-3 item.
      - **`data/data-binding/index.md` was superseded by this page** and was
        deleted on 2026-08-31, together with the rest of `data/data-binding` (see
        the decisions log). `how-does-it-work` was not superseded - it keeps the
        binding-order and changed-value material, and this page links to it; it
        moved to `70-implementation-details/data-binding-details`, and
        `typed-properties` to `70-implementation-details/typed-properties`.

- [x] **Documentation: "Page navigation".** The sixth page of the walkthrough,
      `site/content/building-pages/60-page-navigation/index.md`. It opens with a
      page that keeps two fields, walks away from it and comes back to find them
      unchanged; then that a `UIGoto` is a real page change (redirect, new
      document, new page object) and that it only *records* the target, so the
      rest of the handler still runs; then the six moves tabled, with page
      parameters and `@UIUrlParameter` on the receiving side; then `moveSub` and
      the shelf in detail - the `WindowSession` stack, `getShelvedPageStack()`,
      `BreadCrumb2.createPageCrumb()` being nothing but that stack drawn, how an
      entry gets its name (`IBreadCrumbTitler`, `getPageTitle()`, class name), and
      `addBackButton()` turning into a Close button at the bottom of the shelf;
      then what each move does to the stack, including the two rules that cut
      across it (a target already on the shelf turns any move into a move *back*
      to that instance, destroying everything above it; moving to the root page
      always empties the shelf); then conversations travelling with a shelved page
      and the `moveSub(clz, conversation, pp)` join; then
      `addActionMessage()`/`addAction()` for saying something on the page you land
      on. Two plantuml diagrams: the request in which a `UIGoto` happens, and the
      shelf through moveSub/moveSub/back. Closing pointer to `state-management`.
      Two demo pages carry it, in `pages/tutorial/navigation`: `NavStatePage`
      (state in fields, and buttons for moveSub / moveSub with a message /
      replace / reload) and `NavDetailPage` (prints the live shelve stack and
      walks it with Back, Deeper, Sideways, Start over here, and a moveSub to the
      page it came from), linked from `TutorialListPage` under a new "Page
      navigation" caption.
      Verified by driving both pages under jetty in Chrome, watching the printed
      stack and the breadcrumb: `moveSub` pushes and starts a new conversation
      (`$cid` .x -> .c1 -> .c2); `back()` returns to the page instance with its
      counter intact; `replace` swaps the top entry and leaves the depth alone;
      `moveNew` empties the shelf, leaving one entry and a Close button;
      `reload()` keeps the shelf but gives a new conversation and a zeroed page;
      `addActionMessage` shows its flare on the page arrived at; and a `moveSub`
      to a page already on the shelf returned to *that* instance (state intact)
      and dropped the two pages above it - which is why that rule is in the page
      as a warning callout. The site builds and both diagrams render.
      - **Not deployed**: the two `!demo()` frames on this page are 404 until the
        demo is redeployed with `scripts/deploy-demo`.

- [x] **Documentation: "Showing rows".** The seventh page of the walkthrough,
      `site/content/building-pages/70-showing-rows/index.md`, filling the biggest
      hole in it: after "using databases" a reader could run a query but had no
      way to show more than one record. Three parts, as agreed. First the table:
      the model / `RowRenderer` / `DataTable` split and the `DataPager`, the
      query running on first render rather than at construction, page size and
      the 1000-row model maximum with its truncation marker. Then the
      columns: the four `column()` forms tabled, everything a `ColumnDef` can be
      told tabled (label, width in characters or css, maxWidth, align, css,
      wrap, hint, converter, renderer, the sort methods, cellClicked, editable /
      factory), what a cell actually is - a `DisplaySpan` bound to the property,
      so a changed row value updates the cell - and the rules around custom
      renderers (`IRenderInto`, not called for null unless you implement
      `renderOpt`, and a warning callout that editable/factory cannot be combined
      with renderer/converter, which throws), and a **sorting** section: how a
      column becomes sortable, what `sortdefault()` decides, and that a header
      click tells the *model* to sort rather than reordering the screen - which
      for `SimpleSearchModel` means an `order by` in the database, applied
      **before** the row limit. Sort -> limit -> show, not limit -> sort: sorting
      20,000 tracks in a model that fetches 1000 gives the first 1000 names of
      all of them, where the other order would silently sort an arbitrary
      thousand. A diagram carries that pipeline. Only `RowRenderer` is used; the
      other row renderer classes are not mentioned. Then `SearchPanel`: the whole
      list screen written in the search handler with no components in fields,
      `getCriteria()` returning null on bad input, where the fields come from
      (metadata, a property-name list, a base `QCriteria`, `add().property()`),
      and two tables of what a user may type in a text or number search box
      (`*` wildcards, a trailing point for exact, `> 12 < 100`, `*`/`!`). Finally
      the shelve mechanism: `TableModelTableBase.onShelve/onUnshelve` handing the
      event to a model that implements `IShelvedListener`,
      `SimpleSearchModel.onShelve()` dropping the result so the next render
      re-queries, and `setRefreshAfterShelve(true)` for the second staleness -
      the page's own persistence session handing back the entity objects it
      already had. And finally the alternative for data that is not a query
      result: `SortableListModel<T>` over a list of your own, which sorts in
      memory instead of in the database, plus the rule that every change goes
      through the model (`add()`, `delete()`, `modified()`, `move()`) because
      that is what tells the table which rows to change - a change made behind
      the model's back leaves the screen showing the old rows. Three plantuml
      diagrams: how a cell gets filled, where sorting happens, and what shelving
      does to the model.
      Six demo pages carry it, in `pages/tutorial/tables`: `TableFirstPage`
      (the model, the columns, the table and a pager),
      `TableColumnsPage` (the column options, cell and row click handlers, a
      renderer that highlights a price, a `column()` column sorted on the
      artist), `TableSearchPage` (SearchPanel + result table),
      `TableShelvePage` (a query counter incremented inside the query itself) and
      `TableDetailPage` (what a row click opens, so the list gets shelved) and
      `TableListPage` (a `SortableListModel` over a basket of plain
      `BasketLine` objects, with buttons that add, change and delete rows through
      the model and no `forceRebuild()` anywhere). They are linked from
      `TutorialListPage` under a new "Showing rows" caption.
      Verified by driving all of them under jetty in Chrome: sorting on a
      property-less column orders by artist; the cell handler wins over the row
      handler on its own column; `maxWidth` truncates with a hover title; a
      search for `love` returns 27 records; and on the shelve page the counter
      goes 1 -> 2 after visiting a track and pressing Back, stays put when paging
      (the model reads the result once and the pager slices it) and goes up again
      when a column header is sorted. On the list page, adding a line drops it in
      at its sorted position and "one more copy" changes that one cell, both
      without a rebuild. The site builds and both diagrams render.
      - `SearchPanel` was given an explicit property list (`name`, `album.title`,
        `album.artist.name`) rather than the metadata default, because the
        metadata search fields for `Track` include two entity lookups whose popup
        is unusable inside a `!demo()` iframe - the same trap as the
        `LookupInput2` one noted under the data-binding page. The metadata form is
        documented, just not embedded.
      - `AbstractCdShopListPage` (the cddb list screens) keeps its `ContentPanel`
        and `DataTable` in fields, which is what the conventions forbid; the
        tutorial page shows the closure form instead. Reworking that base class is
        a phase-3 item.
      - **The page says nothing about metadata**, deliberately: the concept is
        not explained yet, and the page after this one is about it. An earlier
        draft had a "Columns you did not define" section (a `RowRenderer` with no
        columns, taking them from `@MetaObject`) and explained why a price shows
        as money; both were removed, together with the second table on
        `TableFirstPage` that the section demonstrated. That material, and the
        rest of what metadata decides (labels, converters, default columns,
        default sort, search fields), belongs on the metadata page - which should
        pick it up there.
      - **Not deployed**: the four `!demo()` frames on this page are 404 until the
        demo is redeployed with `scripts/deploy-demo`.

- [x] **Fixed the metadata of the Track entity and the entities around it.**
      Writing the "showing rows" page made the gaps visible: the tutorial had to
      hand a converter and an alignment to a column that metadata should have
      supplied, and `TrackDetails` formatted a duration by hand. In
      `to.etc.domui.derbydata`:
      - `Track` had **no `defaultSortColumn`**, so any metadata-driven table of
        tracks came out in database order; it is now `name`.
      - The duration converter sat on the *column*
        (`@MetaDisplayProperty(converterClass = MsDurationConverter.class)`), so
        it worked in a default table and nowhere else. It moved to the property
        as `@MetaProperty(converterClass = MsDurationConverter.class)`, which is
        what makes it work in forms and in explicitly-defined columns too.
      - `Track.unitPrice` had **no numeric presentation**, so prices rendered as
        a bare `0.99`, left aligned. It now carries
        `@MetaProperty(numericPresentation = NumericPresentation.MONEY_FULL)` and
        renders as `$ 0.99`, right aligned, everywhere.
      - `unitPrice` was added to the default columns; a track's price belongs in
        the list of a CD shop, and it makes the money presentation visible in the
        metadata-driven table.
      - `Track.name` was mapped `@Column(length = 128)` while the schema says
        `VARCHAR(200)`; the annotation now matches the column.
      - `Album.defaultSortColumn` was **`"name"`, a property Album does not
        have** (it has `title`), so it silently did nothing -
        `ColumnList.setDefaultSortColumn()` just finds no matching column. Fixed
        to `title`.
      - `MediaType` rendered as `to.etc.domui.derbydata.db.MediaType#1 @1491...`
        in the combo on `TrackDetails`: a combo renders through
        `@MetaCombo`/`getComboDisplayProperties()`, falling back to `toString()`,
        and `MediaType` had neither (`Genre` only worked because it happens to
        have a `toString()`). Both now carry
        `@MetaCombo(properties = @MetaComboProperty(name = "name"))`, so the
        rendering comes from metadata rather than from an accident.
      Two consumers were simplified to use what metadata now provides:
      `TrackDetails` shows the duration with
      `fb.property(m_track, Track_.milliseconds()).readOnly().control()` instead
      of calling `MsDurationConverter` itself, and the tutorial's
      `TableColumnsPage` no longer passes a converter for its Duration column.
      The "showing rows" page does not mention any of this - metadata is not a
      concept it may use yet - so its Duration columns state their converter on
      the column, which overrides what metadata now gives anyway.
      Verified in the demo: the CD shop's track search (`CdCollection`) now shows
      Title / Duration / Price / Album / Artist sorted by title with `$ 0.99`
      prices; the media type combo reads "MPEG audio file"; `TrackDetails` shows
      `5m 43s 719ms` from metadata; `AlbumListPage` is unaffected because its own
      renderer sets a sort column, which beats the metadata default. The demo
      module's 9 unit tests pass and the HibernateChecker reports no issues.
      - **Not checked**: the Selenium `IT*` fixtures were not run (they need a
        browser driver). `LookupForm1TestPage`/`LookupForm2TestPage` use Track's
        *search* metadata, which was not touched, but a test that counts the
        default columns of a Track table would now see five instead of four.
      - **Left alone, worth a decision**: every entity except `Artist` names a
        sequence that `CreateDB.sql` does not define (`track_sq`, `album_sq`,
        `genre_sq`), and `MediaType` names `track_sq` - a copy-paste. Nothing
        inserts these entities in the demo, so it never surfaces; fixing it means
        deciding whether the sequences belong in the schema script.

- [x] **The embedded demo pages (`!demo()`) did not work at all.** Every iframe on
      the documentation site showed "Can't create session, session cookie is
      blocked by the browser!" - not only the new page, the data-binding ones too.
      Cause: domui.org frames demo.domui.org, which is a **cross-site** frame, and
      the demo's session cookie was plain `JSESSIONID=...; Path=/; HttpOnly`.
      Without `SameSite=None` a browser defaults it to Lax and never returns it in
      that frame, so DomUI redirects once with `$cid=...r`, finds no session
      again, and gives up with that message (`PageRequestHandler`, the
      `conversationId.equals("r")` branch).
      Fixed in the demo webapp:
      - `src/main/webapp/META-INF/context.xml` (new): a Tomcat `CookieProcessor`
        with `sameSiteCookies="none"` and `partitioned="true"`. Partitioned (CHIPS)
        keeps it working for people whose browser blocks third party cookies
        outright; it gives the embedded application a session per embedding site.
        It needs Tomcat 10.1.20 / 11.0.0-M18 or newer - on an older Tomcat the
        attribute is unknown and the application does not deploy.
      - `SessionCookieSetup`, a `ServletContextListener`, sets the Secure flag,
        which `SameSite=None` requires. It is done in code because **Tomcat
        ignores `session-config/cookie-config/secure`** - verified: changing
        `<name>` in that block does change the cookie name, while `<secure>` and
        `<http-only>` do nothing; Tomcat takes those from the Context defaults and
        from `request.isSecure()`, which is false because Apache terminates TLS in
        front of it. The listener skips a developer workstation
        (`DeveloperOptions`), so local http development is unaffected.
      - The demo's `web.xml` was on the pre-Jakarta schema (`java.sun.com`
        namespace, version 3.0); it is now `jakartaee` version 6.0.
      Verified on a real Tomcat 11.0.18 with the war built from this tree: the app
      deploys clean and answers
      `Set-Cookie: JSESSIONID=...; Path=/; Secure; HttpOnly; SameSite=None; Partitioned`,
      and with the documentation built against that instance the demo renders
      inside the iframe in Chrome, cross-site. (Jetty 11, which the local
      `jetty:run` uses, ignores `session-config` altogether - it cannot show this.)
      **Not deployed**: the live site keeps showing the message until the demo is
      redeployed with `scripts/deploy-demo`.

- [x] **Inline code was rendered as a block on the whole documentation site.**
      `site/templates/css/site.css` styled the bare `code` element as the code box
      (`display: block`, padding, border-left), which is meant for a fenced block -
      `<pre><code>` - but also hit every inline `code` span in running text,
      cutting each sentence containing one into pieces. The box now belongs to
      `pre`, `pre code` is neutralised, and inline `code` has its own compact
      style; the `@media print` block had the same `code, pre` conflation and was
      split the same way.

- [x] **Documentation: "Metadata".** The eighth page of the walkthrough,
      `site/content/building-pages/80-metadata/index.md`, and the page the
      previous one deliberately left material for. Two subjects in one page,
      because they are one mechanism. First metadata: a form and a table built
      over `Track` that state no label, size, converter, mandatory marker,
      column or sort order at all, then where each of those came from - the four
      sources read in order (the properties themselves, JPA annotations, DomUI
      property annotations, DomUI class annotations, the class's `.properties`
      file), tabled per source, with the rule that `@MetaProperty` **adds** to
      what JPA already said rather than repeating it. Then asking for the model
      yourself (`MetaManager.findClassMeta` / `getPropertyMeta`, dotted paths)
      with `PropertyMetaModel` and `ClassMetaModel` tabled. Then what metadata
      decides, one section each: the label (and that a missing key shows the
      property name, which is how you find it); which control (the
      `ControlCreatorRegistry` scoring, the >5-values-becomes-a-combo rule, a
      relation becoming a lookup unless the metadata prefers a combo); the
      limits of the field (`length` -> maxLength, `precision`/`scale` ->
      validator plus a calculated width, and the JPA-255 trap); how the value is
      shown (`numericPresentation` and `temporal` steering the converter factory
      scoring, `converterClass` beating everything, and why that belongs on the
      property rather than on one column); and the table, sort order and search
      fields from `@MetaObject`, including that defining one column drops all
      the default ones. Then enum labels: `<VALUE>.label` in the enum's bundle,
      `<property>.<VALUE>.label` overriding it in the owning class's bundle, and
      booleans getting Yes/No from DomUI's own bundle.
      Then internationalization, taken from `99-todo/internationalization` and
      its `locale-handling` sub-page and rewritten: one page in two languages
      first, then where the locale of a request comes from (`___locale` -
      **three** underscores, kept in the session - then the session, then
      `DomApplication.getRequestLocale()`), `NlsContext.getLocale()` as a
      ThreadLocal set per request, and the currency locale as a separate
      question. Then **why the JDK's localization is not used**, with the four
      concrete reasons rather than the old page's rant: a `ResourceBundle` is
      one language so it cannot be a constant; a missing translation falls back
      to `Locale.getDefault()` - the locale of the *server* - before it falls
      back to the base file, so it silently becomes the wrong language
      (`ResourceBundle.Control.getFallbackLocale()`); the search order cannot be
      extended with DomUI's dialect level; and the encoding trap. Then
      `BundleRef` (a place, not a language; one instance per place; the
      resolution order and key-level fallback), `IBundleCode` enums as the way
      to name a message, with `CodeException`, `UIMessage.error()` and `Msgs`;
      the bundle stack behind `$()` and how a subclass overrides a superclass's
      text; and finally **the keys metadata looks for**, tabled - which is where
      the two halves meet, since the class bundle is an ordinary `BundleRef`.
      Three plantuml diagrams: how a class metadata model is built, which
      control a property gets, and where the locale of a request comes from.
      Three demo pages carry it, in `pages/tutorial/meta`, plus a small model:
      `ShippingMethod` and `ShipmentState` (enums with their own bundles, three
      and six values, so one becomes a radio group and the other a combo) and
      `Shipment` (a `@GenerateProperties` class with a bundle that overrides one
      enum value label for one property). `MetaFormPage` (the `Track` form and
      the column-less `RowRenderer`, with a box printing the metadata that
      produced them), `MetaEnumPage` (the four controls plus
      `MetaManager.getEnumLabel` asked directly, per enum and per property) and
      `MetaNlsPage` (language links, `$()` texts, an `Invoice` form and the
      framework's own `Msgs`). Linked from `TutorialListPage` under a new
      "Metadata and internationalization" caption.
      Verified by running the demo under jetty and driving all three in Chrome:
      the form shows Title/Composer/Duration/Price/Album/Media type with three
      mandatory markers, `5m 43s 719ms` and `$ 0.99`, and the table comes out
      with the five `@MetaObject` columns sorted on title; the enum page shows
      "Pick up at the shop" on one field and "Customer brings it back" on the
      other, Yes/No on the boolean and a combo for the six-valued enum; and the
      NLS page switches wholesale to Dutch on `___locale=nl_NL` - labels, enum
      values, page title, breadcrumb, `02-01-2007` and `3,96` - while the two
      `Invoice` properties that `Invoice_nl.properties` has no key for stay
      English, which is the per-key fallback made visible. The site builds, all
      three diagrams render and all three `!demo()` frames resolve. The demo
      module's 9 unit tests pass.
      - **The framework's own bundles have Dutch as their default language**
        (`to/etc/domui/util/messages.properties` is Dutch, `_en` is English; the
        same for `YesNoType` and the rest). The `de_DE` link on the demo page
        shows what that means: the application's texts fall back to their default
        file - English there - while `Msgs.mandatory` comes out as "Dit veld is
        verplicht". Documented as a warning callout, and listed as a candidate
        below.
      - Corrected against the old page while writing: the locale parameter is
        `___locale` with **three** underscores, not `__locale`, and it is stored
        in the session rather than applying to one request only; parameters
        starting with `_` are filtered out of `PageParameters`, so it never
        reaches the page.
      - **`99-todo/internationalization` and its `locale-handling` sub-page are
        superseded by this page and were deleted**, everything worth keeping
        having been carried over. The two links to them were repointed:
        `building-pages/index.md` now lists the metadata page, and
        `release-notes/domui-2-0` (itself due for deletion in phase 4) points at
        the new page.
      - **Not deployed**: the three `!demo()` frames on this page are 404 until
        the demo is redeployed with `scripts/deploy-demo`.


- [x] **Documentation: "Telling something to a user".** The ninth page of the
      walkthrough, `site/content/building-pages/90-telling-the-user/index.md`:
      everything an application uses to say something out of band, ordered by how
      much it interrupts. `MsgBox2` first - `on(node)` hanging the box on the
      page, the four types, `text()`/`content()`/`title()`/`size()`, and the
      warning that the box does **not** block the code that made it. Then asking:
      which handler an answer arrives in, tabled (`onAnswer` for a `MsgBoxButton`,
      `onAnswer2` for a value of your own, a click handler for a button that does
      not answer, the input handler for a value), the default `CONTINUE`/`CANCEL`
      buttons, priority-based button order, and `onValidate` refusing to let the
      box close. `MsgBox` gets one short subsection: the same window with a fixed
      set of static methods, worth recognising in existing code, not worth writing
      - deliberately the only place in the section where an older API is named
      (the user asked for it explicitly).
      Then `UIMessage`: its five parts tabled (bundle code + parameters, type,
      error location, node, group), the static constructors, and the two ways to
      post one - `setMessage()` on a control (one at a time; a less severe message
      does not replace a more severe one) and `addGlobalMessage()` on any node.
      Then `ExceptionDialog`: `create(container, message, throwable)`, a plantuml
      of what it decides (ValidationException shown as nothing, unwrapping,
      translators, else log + stack trace), the built-in translators tabled
      (`CodeException`, concurrent update, SQL state 23505, Hibernate constraint
      violations), registering one of your own in the application's
      `initialize()`, and `executeWithDialog()`. Its sub-section covers the
      exceptions nobody catches: `DomApplication.addExceptionListener()`, most
      specific class first, `true` meaning handled, with DomUI's own
      `QNotFoundException` -> `ExpiredDataPage` as the example.
      Then the **error fence**, with a nested-rectangle plantuml: a message
      travels up until it meets a fence; the page body always is one; a fence is
      an `IErrorFence` on a `NodeContainer` holding listeners; `ErrorPanel` and
      `ErrorMessageDiv` are listeners; `new ErrorMessageDiv(panel)` makes a panel
      a fence and its own display in one call; a fence with no listeners asks
      `DomApplication.addDefaultErrorComponent()` (which the demo application
      overrides); `PropagatingErrorFenceHandler` passes messages on upwards. The
      page closes on `bindErrors()`, pointing back at data binding for the
      mechanism and repeating only the rule.
      Five demo pages carry it, in `pages/tutorial/messages`, plus a `TutorialMsg`
      bundle-code enum with its `.properties` and an `OutOfStockException`:
      `MsgBoxPage` (info/warning/error/translated text/own content),
      `MsgAskPage` (yes-no, buttons carrying values, an input box, an input box
      with `onValidate`), `MsgMessagePage` (global messages per severity, a
      message with an error location, messages on one control),
      `MsgExceptionPage` (an unrecognised exception, a `CodeException`, an
      exception with a registered translator, `executeWithDialog`) and
      `MsgFencePage` (two panels each with their own `ErrorMessageDiv` fence, and
      the page around them). The translator for `OutOfStockException` is
      registered in `Application.initialize()`, which is where such a registration
      belongs. Linked from `TutorialListPage` under a new "Telling something to a
      user" caption.
      Verified by running the demo under jetty and driving all five in Chrome, and
      the site builds with both diagrams rendered and all five `!demo()` frames
      resolved. The demo module's unit tests pass.
      - **Two theme defects found and fixed while verifying** (they made the page
        impossible to illustrate honestly):
        `themes/scss/winter/_errorMessageDiv.scss` set the info and warning
        message backgrounds with the `background` *shorthand*
        (`background: $info_bg url(mini-info.png)`), which resets the
        `no-repeat`, position and `background-size` that `.ui-emd-msg` had just
        set - so every info and warning message rendered as a page-filling tiled
        carpet of icons with the text nowhere in sight, while errors (which use
        `background-image`) were fine. Both are longhand now.
        And `$warnings_foreground` was `yellow` on the `#fffeee` warning
        background - invisible; it is a dark amber (`#8a6100`) now. Both
        variables are used only for text on that background (here and in
        `_flare.scss`).
      - **Observed, not fixed:** a control error inside a `MsgBox2` is reported
        **twice** - once inside the box and once in the page's own error display.
        `MsgBox2`'s constructor does `setErrorFence(null)` ("do not accept
        handling errors") while `Window.init()`/`createFrame()` set fences on the
        window and on its content, and the message ends up in two of them. It
        happens for a plain mandatory field just as much as for a message set by
        hand, so it is not something the tutorial code causes. Candidate below.
      - **Not deployed**: the five `!demo()` frames on this page are 404 until the
        demo is redeployed with `scripts/deploy-demo`.

- [x] **Documentation: "Layout".** The tenth page of the walkthrough,
      `site/content/building-pages/100-layout/index.md`, in two halves. First the
      three things a screen is framed with: `ContentPanel` (a `Div` with
      `ui-cpnl`, the padding a page's content needs - with the demo page adding
      one line to the page itself so the difference is visible, and the rule that
      overlays go on the page rather than in the panel); `ButtonBar2` (left and
      right groups, horizontal or vertical, a table of the *kinds* of button it
      adds rather than a list of overloads, the `order` argument, and that
      `addBackButton()` reads the page shelf and silently becomes a **Close**
      button when there is nothing to go back to); and `TabPanel` (the `tab()`
      builder tabled - label/image/content/lazy/closable/position/onDisplay/
      onHide/onClose - `ITabHandle` for select/close/updateLabel/updateContent,
      why `lazy()` matters, `new TabPanel(true)` making the panel an error fence
      that marks the tab an error came from, and `ScrollableTabPanel`).
      Then the second half: writing a **fragment** - a `NodeContainer` that fills
      itself in `createContent()`, "a `UrlPage` minus the URL". `ArtistCardFragment`
      shown whole, used three times on one page; a plantuml of what
      `forceRebuild()` on one of them costs; five reasons to bother (reuse, a
      readable page build method, per-part redraw over a page-wide rebuild, state
      living with what owns it, and one place for the css class); and the rules
      inside one - components local, state in fields, parameters through the
      constructor, results through a listener.
      The page ends on `CollapsibleSection`, written out in full: a titled section
      with a chevron that folds it shut, with three things called out because
      every component has to answer them - **where the content lives** (the
      content `Div` *is* a field, deliberately: it holds what the caller put in
      it, so the rule is not "no components in fields" but "a field may hold what
      the fragment does not build"), **what opening and closing costs** (a boolean
      plus `forceRebuild()`, a delta of one div), and **how it tells anyone**
      (`INotify<CollapsibleSection>`, the shape every DomUI event has). It closes
      by naming what a fragment still lacks to be a component - a value, the
      control states, a place in the form builder, metadata - pointing at the
      components section.
      Four demo pages plus two fragment classes in `pages/tutorial/layout`:
      `LayoutPanelPage`, `LayoutTabPage`, `ArtistCardFragment` +
      `LayoutFragmentPage`, `CollapsibleSection` + `LayoutSectionPage`, with
      `dm-card`, `dm-cardrow` and `dm-cs*` styles added to the demo's
      `_panels.scss`. Linked from `TutorialListPage` under a new "Layout" caption.
      Verified by driving all four under jetty in Chrome: the tab builder's icon,
      lazy body, closable cross and `select()` from outside all behave; opening
      one card leaves the other two alone; and the album section survives a
      collapse-and-expand with its table intact, which is the point being made
      about the kept content div. Site builds, the diagram renders, all four
      `!demo()` frames resolve. The demo module's unit tests pass.
      - **`TabPanelBase.add(content, label, ...)` is marked "legacy quick add
        methods - prefer the builder instead" in the source**, and every one of
        them just calls `tab()...build()`. The page and the demo therefore use the
        builder only, and the `add()` forms are not documented.
      - **Not deployed**: the four `!demo()` frames on this page are 404 until the
        demo is redeployed with `scripts/deploy-demo`.

- [x] **Documentation: "Writing a component".** The eleventh page of the
      walkthrough, `site/content/building-pages/110-writing-a-component/index.md`,
      picking up where the layout page's fragment stopped: a component is a
      fragment that holds a value, and it is nothing more than a class
      implementing `IControl<T>`. Opens with `StarRating` (a five-star control)
      and the demo page using it exactly like a `Text2`. Then what `IControl`
      asks for, tabled per interface it inherits from (`IActionControl`,
      `IHasChangeListener`, `INodeErrorDelegate`, `IForTarget`), and the note that
      nothing in it says "node". Then `AbstractDivControl` tabled - what it
      already does (`internalGetValue`/`internalSetValue`, `setValue` with its
      equality check, `onValueSet` -> `forceRebuild`, the three states with their
      `xxxChanged()` hooks, `setBindValue`) against the four things left to write
      (`createContent()`, `getForTarget()`, `validateBindValue()`, and turning
      user actions into a value).
      Then how a control learns about a change: `acceptRequestParameter()`
      answering *whether the value differed* for input-based controls, and for a
      click-driven one the three obligations of its own handler - `setValue()`,
      `OldBindingHandler.controlToModel(this)` (because the request's
      control-to-model pass already ran before the click handler; the framework's
      own lookup input does the same), and calling the change listener. With
      `RadioGroup`'s delegation of `internalOnValueChanged()` as the alternative
      for a control built out of other controls.
      Then the core of the page: **when has a value changed?** `setValue()` shown
      whole, the consequence that setting the value a control already holds costs
      nothing (which is what makes it safe for binding to push on every request),
      and `MetaManager.areObjectsEqual` tabled in order - identity, `equals`,
      class relation, **primary key**, arrays. The two traps then get a section
      each: a mutable object changed in place is still the same value, and two
      objects with the same primary key are the same value; with the three ways
      out (treat values as immutable, `forceRebuild()`, a real `equals()`), and
      `RadioGroup`'s `m_valueIsSet` as the answer to "set to null" versus "never
      set". Then the same question inside a binding, twice: on the way in the
      control's `bindValue` against the model, on the way out the model against
      `m_lastValueFromControlAsModelValue` (the value last exchanged, which is
      what keeps a control in error showing what the user typed) - with the
      `Collection` content-hash exception named as the patch over the same hole,
      and a sequence diagram of both comparisons.
      Closes on `bindValue` versus `value`, tabled: same value, different
      audience - `getValue()` reports the error on the control and throws,
      `getBindValue()` throws silently and the binding keeps the error until
      `bindErrors()`; `setBindValue()` is the equality check plus `setValue()`;
      `bind()` binds `bindValue` when the control has one. With the two-method
      implementation pattern (`validateBindValue()` + a reporting `getValue()`)
      that `RadioGroup` and the comboboxes use word for word. One pointer at the
      end to the components section for control factories and css rules.
      Three demo pages and three classes in `pages/tutorial/component`:
      `StarRating` (the control), `AlbumBadge` (a control whose value is an
      entity), `Review` (the bound model), `ComponentStarPage`,
      `ComponentBindPage` and `ComponentEqualityPage`, with `dm-rating*` and
      `dm-badge*` styles in the demo's `_panels.scss`. Linked from
      `TutorialListPage` under a new "Writing a component" caption.
      Verified in Chrome against the running demo, including every claim the page
      makes about equality: clicking a star fires the change event and reaches the
      model in the same request; a mandatory empty control reports "Mandatory
      field" through `getValue()` (label prefix and all) and through
      `bindErrors()` on the bound page; changing an album's title in place and
      calling `setValue()` with it changes **nothing** on screen, `forceRebuild()`
      does; setting a *different* `Album` object carrying the same id changes
      nothing either; and mutating the album inside the bound model is invisible
      while replacing it moves. The site builds, the sequence diagram renders and
      all three `!demo()` frames resolve. The demo module's unit tests pass.
      - The page lives in `building-pages` rather than in `components` because it
        is the next step of the walkthrough; `components/` keeps the reference
        material (rules, the existing component catalogue) that it points at.
      - **Not deployed**: the three `!demo()` frames on this page are 404 until
        the demo is redeployed with `scripts/deploy-demo`.

- [x] **Documentation: the components reference.** All thirteen groups are
      written, 2026-09-05. The `components/` section is
      rewritten as a **reference catalogue, organised in functional groups**. The
      structure is fixed (decided 2026-09-01, see the decisions log for the full
      inventory and the group membership):
      - one **group page** per functional group, describing what the group is for
        and listing its members with one line each;
      - one **component page** per component below its group: what it is for,
        its example, then its relevant properties and methods;
      - one **demo page** per component, in
        `to.etc.domui.demo/pages/components/<group>/`, embedded with `!demo()`.
        Complex components (`LookupInput2`, `SearchPanel`, `DataTable`) get
        several demo pages, one per aspect.
      Only the **current** component of each kind is documented; the superseded
      ones (`Text`, `ComboFixed`, `LookupInput`, `Tree`/`Tree2`, `MsgBox`, ...)
      are not named at all. `ComponentListPage` in the demo is regrouped to match
      these groups exactly, so the demo's own overview and the documentation have
      the same shape.
      The thirteen groups, worked one at a time:
      - [x] **Text and value input** - `Text2`, `TextArea`, `DateInput2`,
            `ColorPicker`, `ColorPickerButton`, `ColorPickerInput`. Done
            2026-09-01; see the decisions log entry of that date for what the
            pages say, what was verified and the two defects found.
      - [x] **Choice input** - `Checkbox`, `RadioButton`/`RadioGroup`,
            `ComboFixed2`, `ComboLookup2`, `EnumSetInput`. Done 2026-09-02; see
            the decisions log entry of that date.
      - [x] **Lookup and search** - `LookupInput2`, `SearchInput2`,
            `SearchAsYouType`, `SearchPanel`. Done 2026-09-02; see the decisions
            log entry of that date.
      - [x] **Buttons and actions** - `DefaultButton`, `LinkButton`,
            `SmallImgButton`, `HoverButton`, `CheckboxButton`, `SwitchButton`,
            `ActionButton` + `IUIAction`, `ButtonBar2`. Done 2026-09-02; see the
            decisions log entry of that date.
      - [x] **Display-only components** - `DisplaySpan`, `DisplayControl`,
            `DisplayCheckbox`, `DisplayRadiobutton`, `DisplayHtml`,
            `PercentageCompleteRuler2`, `EmbeddedCode`. Done 2026-09-02; see the
            decisions log entry of that date.
      - [x] **Tables, lists and trees** - `DataTable`, `DataPager`,
            `RowRenderer` / `ColumnDef`, the `ITableModel` family,
            `ExpandingEditTable`, `DataCellTable`, `ListShuttle`, `Tree3`. Done
            2026-09-02; see the decisions log entry of that date.
      - [x] **Layout and page structure** - `ContentPanel`, `Panel`,
            `CaptionedPanel`, `Caption2`, `GenericHeader`, `ExpandHeader`,
            `TabPanel`, `ScrollableTabPanel`, `SplitterPanel`, `VerticalSpacer`,
            `ChildFragment`. Done 2026-09-02; see the decisions log entry of that
            date.
      - [x] **Windows, dialogs and messages** - `Window`, `Dialog`, `InputDialog`,
            `MsgBox2`, `ExceptionDialog`, `ErrorPanel`, `ErrorMessageDiv`,
            `MessageFlare`/`Flare`, `MessageLine`, `InfoPanel`, `Explanation`.
            Done 2026-09-04; see the decisions log entry of that date.
      - [x] **Navigation and menus** - `BreadCrumb2`, `AppPageTitleBar`,
            `PopupMenu2`, `HamburgerMenu`, `ALink`. Done 2026-09-05; see the
            decisions log entry of that date.
      - [x] **Images, icons and file upload** - `Icon`/`IIconRef`, `FontIcon`,
            `SvgIcon`, `ImgIcon`, `Img`, `DisplayImage`, `ImageSelectControl`,
            `FileUpload2`, `FileUploadMultiple`. Done 2026-09-05; see the
            decisions log entry of that date.
      - [x] **Rich content editors** - `CKEditor`, `HtmlEditor`, `AceEditor`.
            Done 2026-09-05; see the decisions log entry of that date.
      - [x] **Charts** - `PlotlyGraph`. Done 2026-09-05; see the decisions log
            entry of that date.
      - [x] **Asynchronous and long-running work** - `AsyncContainer`,
            `AsyncDiv`, `PollingDiv`. Done 2026-09-05; see the decisions log
            entry of that date.
      The existing `components/rules` page stays where it is; the three current
      subdirectories (`forms-and-input`, `lookup-and-search`,
      `tables-trees-navigation`) are dissolved into the groups above.
      **The dissolving is finished.** `lookup-and-search`,
      `tables-trees-navigation` and `forms-and-input` are all gone; the form
      builder, which is a member of no group because it is not a component but
      the thing that lays components out, got a group of its own -
      `components/15-forms` - on 2026-09-06. See the decisions log entry of that
      date.

### Candidates raised while doing phase 0, and fixed

These were offered as input while the phase 0 items were being worked, and taken up.

- FIXED: The `IUIAction` API was inconsistent about instances:
  `DefaultButton(instance, action)` and `ButtonBar2.addAction(instance, action)`
  are generic, while `LinkButton(IUIAction)` and
  `ButtonBar2.addButton(IUIAction)` accepted an action over `Void` only. The
  latter two are now shaped like the working pair: `addButton(IUIAction<?>)`
  (through `IButtonBar`, `ButtonFactory`, `ButtonBar` and `ButtonBar2`) and
  `LinkButton(IUIAction<?>)` plus `<T> LinkButton(T instance, IUIAction<T>)`.

- FIXED: `AbstractSearchPage` (the demo's shared list-screen base, now under
  `pages/components/lookup`) and `AbstractCdShopListPage` kept their
  `ContentPanel` and `DataTable` in fields, which the conventions forbid. Both
  hold no components at all now: the result container is a local `Div` of
  `createContent()` that the search handler closes over, and the base only
  offers `showResult(container, criteria)`, which builds the table and pager
  into it. The four `SearchPanel*Page` demos make their own `ContentPanel` and
  their own result `Div`.

- ~~**No label a form builder writes has a `for` attribute.**
  `ResponsiveFormLayouter` never calls `Label.setForTarget(control)`~~ -
  **done**: it now does, in both places it attaches a label (the appended-label
  branch and the label container of a new pair), so a form label focuses its
  control and a checkbox label ticks it. Fixed in `f7f9df6af`, verified
  2026-09-05.

- FIXED: `EnumSetInput.setMatcher()` stores a matcher that nothing reads, and
  `setAddSingleMatch()` is commented out while its getter remains. Wire them up
  or remove them.

- FIXED: **`AceEditor.setTabSize()` has its `isBuilt()` test inverted.** It updates when
  the editor is *not* built, where `setTheme()` and `setMode()` next to it update
  when it *is*. It works out because `renderJavascriptState()` pushes the tab
  size on every render, but the setter reads as a bug and behaves like one in
  isolation.

- FIXED: **`AceEditor.selectWord()` selects nothing.** It calls
  `selection.getWordRange(line, col)`, which is a getter: it computes a range and
  discards it. It should feed that range to `selection.setRange`, the way
  `select()` does.

- FIXED: **`FileUpload2.renderEmpty()` is half dead.** The whole method is
  `if(true) { ... } else { ...an older, nearly identical rendering... }`; the
  else branch cannot run and has drifted out of step with the branch that does.
  Delete it. (Found writing the group 10 pages.)

- FIXED: **`LoadedImage.create()` builds an object it throws away.** In the
  resize branch it constructs `LoadedImageInstance oli`, never reads it, and
  returns a `LoadedImage` built from the resized file instead. Harmless, but it
  reads as though the resized instance were being cached and it is not.

- FIXED: **`ImageSelectControl` has no focus target.** `getFocusID()` and
  `getForTarget()` both return `m_sib`, the `HoverButton` whose creation is
  commented out - so both always return null and the control can neither be
  focused nor be the target of a label. Point them at the `FileInput` instead.

- FIXED: **`Icon.of(String)`'s javadoc describes the wrong object.** It says the method
  returns "an `ImageIconRef` / an `SvgIcon` / a `FontIcon` depending on the
  extension"; it always returns an `ImageIconRef`, and it is that ref's
  `createNode()` that picks between the three. The behaviour is right, the
  sentence is not.

- FIXED: **`@UIUrlParameter` was silently ignored on a setter that has no
  getter.** Page parameter injection works from the metamodel's property list,
  and a property without a getter is not in it, so the annotated setter was
  simply never called - no warning, no error, the field kept its default. Found
  writing the group 9 breadcrumb demo page. It now fails loudly:
  `DefaultPagePropertyInjectorFactory` scans the page's setters after
  calculating the injectors and throws a `ProgrammerErrorException` naming the
  method when an `@UIUrlParameter` sits on a property that has no getter.

- FIXED: `DateInput2`'s browser-side date repair (`dateInputRepairValueIn` in
  `domui.dateinput.ts`) took its format from the calendar's NLS bundle, of which
  only a Dutch (the default) and an English one exist - so every other locale got
  the Dutch `%d-%m-%Y` while `DateConverter` read the JDK SHORT pattern for it.
  The client is now given the pattern the server will use: `DateInput2` renders
  `data-datefmt` on its input, translated from `DateConverter.getDatePattern
  (locale)` into the calendar's `%`-notation, and the repair (and the calendar
  popup's format) reads it, falling back to the bundle's default when it is
  absent. Verified in the demo: the same `13-3-13` becomes `2013-03-13` for
  `%Y-%m-%d`, `13-03-2013` for `%d-%m-%Y` and `13.03.13` for `%d.%m.%y`.

- FIXED: `component/layout/ExpandCollapsePanel` did roughly what the tutorial's
  `CollapsibleSection` does, but it put its content *next to* itself
  (`appendAfterMe`) rather than inside it, so the panel and what it opened were
  siblings. It is now shaped like the tutorial one: a `Div` holding a header and,
  while it is expanded, its content div; the expanded state is a boolean field
  and toggling calls `forceRebuild()`, so no component is kept in a field.
  Verified through its only user, `AsyncDiv`'s "Details" stack trace.
- FIXED: A control error raised inside a `MsgBox2` was shown twice: in the box and
  in the page's error display, because the box had no error listener of its own
  and the application's default component is a *propagating* `ErrorMessageDiv`.
  `MsgBox2` now adds its own non-propagating `ErrorMessageDiv` to its content
  when it builds, so a message raised inside the box stops there; the misleading
  `setErrorFence(null)` in its constructor (undone by `Window.createFrame()`
  anyway) is gone. Verified in `MsgAskPage`: an order of 99 copies shows the
  error inside the box only.
- FIXED: `pages/OldHome.java` was an unreferenced legacy page. Deleted.
- FIXED: `pages/special/BasicPage.java` and `BasicListPage.java` (both
  `@Deprecated`, both documenting themselves as "DO NOT USE - ancient and badly
  written") are deleted, and with them the `pages/special` package. Their only
  users, the two binding-tutorial `InvoiceListPage`s, are now plain `UrlPage`s
  that build a `SearchPanel` and show the result in a local `Div` - the same
  shape as the rest of the demo.
- FIXED: The demo database had corrupt names in `to.etc.domui.derbydata`'s
  `CreateDB.sql`. The corruption turned out to be one mechanical accident: every
  `n` standing directly before a quote in the script was deleted, which is every
  value ending in `n` (`Iron Maide`, `Led Zeppeli`, `Yamma Brow`, `Quee`) and
  every `n` before an escaped apostrophe (`Ca''t`, `Do''t`, `Talki''`,
  `Heave''s`). 431 distinct values (829 rows) were repaired; see the decisions
  log entry of 2026-09-05. The 42 `TestDbQCriteria` tests still pass.
- ~~`pages/overview/layout/DemoAppTitle.java` renders a full-width `AppPageTitleBar`
  and now sits inside a padded panel; worth eyeballing in the browser.~~ -
  **done**: the page no longer exists; it was deleted with the group 9/10 work
  (`8a564b553`), and `AppPageTitleBar` is demonstrated by the navigation group's
  own page instead.
- FIXED: **The source viewer's title overlapped its first source line.** The
  scroll container `.dm-srcp-scrl` was `position: absolute; top: 30px` inside a
  page that is a normal document, so it was laid over the header. It is a normal
  block with `overflow: auto` now, and the page scrolls. Verified in the
  browser.

- FIXED: the stale "we cannot upgrade to jetty 11" comment and the commented-out
  `9.4.57` version are gone from `pom.xml`.
- FIXED: `README.md` was rewritten: what DomUI is, the documentation and demo
  urls, how to build it with Java 21 and run the demo, and a pointer at
  `domui-skeleton` for starting an application. The dead Travis and Codacy badges
  and the Launchpad line are gone.

### Phase 1 - Inventory and triage

- [x] Decide the canonical "how you should write a DomUI page today" story - the
      single set of APIs the docs and demo will consistently show. **Decided:**
      current version and current usage only; see "Guiding principle" in
      `IMPROVEMENT-PLAN.md`.

### Phase 2 - Correct the factual errors in the documentation

- [x] Rewrite `getting-started/running-the-demo` for the real branches, Java 21,
      current Maven and the current IntelliJ run configuration. **Done 2026-08-31**
      (see the decisions log entry of that date).
- [x] Rewrite `introduction/developer-view-of-domui` against the source: the
      stack it never named, the entry points (`AppFilter`, `DomApplication`,
      `UrlPage`, `@UIPage`, `@UIRights`), layer 2, and the test framework.
      **Done 2026-08-31** (see the decisions log entry of that date).
- [x] Purge `javax.*`, Java 8, Eclipse/Launchpad-era and 1.1/2.0-branch
      references site-wide. **Done** - checked 2026-09-05: no page mentions
      Java 8, Launchpad, `2.0-stable`, `1.1` or `master-java11` any more, and the
      single remaining `javax.` is deliberate (`70-implementation-details/typed-properties`
      saying that the old `javax.persistence.Entity` is *not* seen by the
      processor). `development-environment/ecj-in-maven` still describes 2017-era
      plugin and compiler versions, but that is a stale-content problem for
      phase 2's "verify every code sample", not a `javax`/Java 8 one.

### Phase 3 - Rework the demo/tutorial application

- [x] Make the tutorial track the spine of the demo: a coherent sequence from
      "hello page" through layout, input, data binding, tables, lookup and
      master/detail, each page small and each showing the recommended API.
      **Done** - `TutorialListPage` now walks eleven sections in order (first
      page, using components, using databases, typed properties, data binding,
      page navigation, showing rows, metadata and internationalization, telling
      something to a user, layout, writing a component), each built alongside its
      `building-pages/*` chapter. The one remnant is the old "Binding tutorial"
      block at the end, which phase 4 deletes.
- [x] **The JUnit/Selenium fixture pages stay in the demo application.** Phase 3
      used to carry "separate the fixture pages from the tutorial/demo pages".
      That is settled the other way on 2026-09-06: the pages serve two purposes
      at once - they test the framework, and they are the worked examples of how
      a testable page is written - so they stay where they are, new fixtures go
      there too, and the testing documentation is written around them. See the
      decisions log entry of that date.
- [x] Ensure the source-viewer (`SourceIcon`) story works for every tutorial page,
      since that is how readers get from a screen to its code. **Done** -
      verified 2026-09-05 by running the demo: `Application.onNewPage()` puts a
      `PageHeader` (and with it the `SourceBreadCrumb`) on every page that has no
      breadcrumb of its own, so every tutorial page carries the `</>` link; it
      opens `SourcePage` in a 1024x768 popup, the sources are packaged into
      `WEB-INF/classes`, and the file comes out syntax-highlighted now that the
      Java highlighter of phase 0 exists. `SourceIcon` itself is dead code - the
      link is `SourceBreadCrumb`'s. One cosmetic defect is listed as a candidate
      above: the page title overlaps line 1.

- [x] **Rewrite demo pages that use superseded APIs to use the current ones.**
      **Done 2026-09-06** - the last part of phase 3: the pages that sat outside
      the component list and the tutorial, and the `pages/test/**` fixtures. No
      page in `to.etc.domui.demo` imports a superseded component any more
      (checked against the superseded-to-current table of 2026-09-01).
      - *`pages/basic` and `pages/dbtable` deleted.* All four pages were
        unreachable - linked from nothing, `!demo()`d from nothing - and each is
        shown better elsewhere: `HelloWorld` by `tutorial/first/HelloPage` and
        `HelloClickPage`, `CaptionsDemoPage` (which used `CaptionedHeader` and put
        an `AppPageTitleBar` in the middle of a page) by `components/layout/PanelsPage`,
        `components/dialog/NoticePage` and `components/navigation/PageTitleBarPage`,
        `WikiDemo` (a `CKEditor` in a field, pointed at `/home/jal/Pictures`, with a
        Dutch button) by `components/editors/CKEditorPage`, and `SimplestDbTable` by
        `tutorial/tables/TableFirstPage`. The 2026-08 entry that kept
        `CaptionsDemoPage` "to show the raw component" was written when
        `pages/overview/**` still existed; it does not.
      - *`pages/rxjava` became `pages/test/rxjava`.* `RxTimePage` is a lifecycle
        fixture - it checks that a `PageScheduler` subscription is disposed when
        the page dies - so it belongs with the other fixtures and is now linked
        from `JUnitTestMenuPage`; its `System.out.println` per tick is gone.
      - *The CD shop's last two component-holding pages are gone.* `AlbumListPage`
        kept its `DataTable` and its `ContentPanel` in fields to decide whether to
        add or replace the result table; it is now an `AbstractCdShopListPage<Album>`
        like its four siblings. `CdCollection` plus `TrackResultFragment` (a `Div`
        holding a `SearchPanel` and a `DataTable` in fields) were between them a
        second copy of that same base, so the fragment was deleted and
        `CdCollection` extends the base too. The base gained `configureSearch()`
        next to `configureColumns()` - `Album` marks only `title` searchable, and
        the page searches on artist as well - and a `getPageTitle()` returning the
        title it is already given, so all five list screens now name themselves in
        the breadcrumb.
      - *The fixtures under `pages/test/**` held to the page rules.* The
        `ContentPanel` / `Div` / control fields went out of `BindError1Page`,
        `BindError2Page`, `BindvalidationErrorPage`, `BuildOrderPage`,
        `BindingConversionTestForm`, `BindingTypeForm1`, `ProxyTestPage1` and the
        two SearchPanel pages: the handler now closes over the local node, the way
        `AbstractCdShopListPage` does. Anonymous `IClicked` classes became lambdas.
      - *Superseded components replaced in the fixtures.* `Text` -> `Text2`
        (`BindingConversionTestForm`, `BindingTypeForm1`, and the "Old Text<>
        control" section of `Text2LayoutTestPage` which is simply gone),
        `DisplayValue` -> `DisplaySpan`, `LookupInput` -> `LookupInput2`
        (`BindError2Page`, and the four LookupInput variants of
        `Form4LayoutTestPage`, which already had the four LookupInput2 ones),
        `MsgBox` -> `MsgBox2` (`BuildOrderPage`, and `tutorial/binding/BindPropertyPage`),
        `ComboFixed.READONLY` -> `IControl.READONLY` (`binding/editabletable/EditableTablePage`).
        `DoNotBindControlDottedTestPage` needs a subclass with a concrete type
        argument and `LookupInput2` is `final`, so its `MyLookup` now extends
        `LookupInputBase2<Customer, Customer>` with a `SameTypeModelFactory`.
      - *Fixtures whose whole subject was a superseded component are gone*, each
        already having a current-component twin: `LookupInputTestPage` (twin:
        `LookupInput2TestPage`), `TestMsgBox1` and `TestMsgBox2` (twins:
        `TestMsg2Box1`, `TestMsg2Box2`). Their tests went with them -
        `ITTestLookupInput`, `ITTestLookupInputLayout`, and two methods of
        `ITTestMsgBox` - but not before the two things in them that were not about
        the old component were moved: `testBindingShouldNotThrowErrorOnLookup`
        (clicking the lookup button must not put a mandatory bound control in
        error) is now in `ITTestLookupInput2`, and `testChromeExtension` (which
        only needs some screen) in `ITTestLookupInput2Layout`.
      - *Four more fixtures deleted as dead*: `IeCheckBoxInTable`,
        `IeCheckBoxInTable2` and `IeTableBugPage` reproduce Internet Explorer 7
        bugs, and `ImageEntitiesTest` pointed an `Img` at a hand-written
        `PropBtnPart` URL - a part with no live caller left, now a phase 4 item.
      - *Two fixtures kept but repaired.* `Click2HandlerPage` and
        `AddRemoveClickHandlerPage` demonstrate current framework behaviour but
        were reachable from nothing; both are now linked from `JUnitTestMenuPage`,
        as the testing documentation's own rule 1 requires.
      - *`LookupForm1TestPage` / `LookupForm2TestPage` renamed* to
        `SearchPanel1TestPage` / `SearchPanel2TestPage`, and `ITTestLookupForm1` to
        `ITTestSearchPanel`: `LookupForm` was removed from the framework in
        `b57d7d2e1` and the pages have used `SearchPanel` since, so the names were
        pointing at something that no longer exists. Their one-line class comments
        (one of them cut off mid-sentence) now say what each actually checks.
      - *`ProxyTestPage1` lost its `Text` and `ComboFixed` pair*, and with them the
        `text()`/`cf()` accessors of the committed `POProxyTestPage1Base` and the
        `testText`/`testFixed` methods of `ITTestProxiesPage1`. The page object was
        edited by hand rather than regenerated - the generator needs a live browser
        session and a keystroke.
      Verified: `mvn21 clean verify -pl to.etc.domui.demo` - 9 unit tests and 57
      integration tests green before the last round of edits and 55 after it (the
      two that went with `Form4LayoutTestPage`'s deleted LookupInput baselines),
      0 failures, 0 errors, the skips all pre-existing `@Ignore`s. Then the demo
      run under jetty and driven in Chrome: `CdShopMenuPage` -> `AlbumListPage`
      searches and lists Title/Artist sorted by artist under the breadcrumb
      "Albums"; `CdCollection` still lists Title/Duration/Price/Album/Artist
      through the shared base; `JUnitTestMenuPage` shows every link including the
      three new ones and no longer has a "Deprecated components' test" section;
      `RxTimePage` subscribes in its new package; `Form4LayoutTestPage` and
      `Text2LayoutTestPage` render without the sections that were removed; and
      `EditableTablePage` and `BindPropertyPage` still build. The documentation
      site builds (157 pages).
      - The counterpart in `domui.github.io`: the fixture-group table in
        `testing/50-test-pages-in-the-demo` names the renamed and added tests, and
        the `rxjava` group with the note that it has no test of its own because
        what it checks is what happens when you walk away from the page.

- [x] **Check the external links.** **Done 2026-09-07**: all eighteen real
      external URLs answer 200 (the rest are illustrative `localhost`,
      `example.com` and `somehost` addresses). One documented URL was wrong, and
      it was a DomUI URL, not a foreign one - see the decisions log entry of that
      date.
- [x] **The hibernate generator has no runnable jar.** **Done 2026-09-07**: the
      module now shades one, and `data/pojo-generator` documents `java -jar`
      again. See the decisions log entry of that date.

### Phase 4 - Remove old and incorrect code and information

- [x] Remove the Maven archetype (`archetypes/domui-hello`) from the framework and
      the `getting-started/maven-archetype` page from the site. **Done 2026-08-31**
      (see the decisions log entry of that date).
- [x] Delete the whole `release-notes/` section. **Done 2026-09-05**: the section
      and its one page (`release-notes/domui-2-0`, the 2017 migration note) are
      gone, along with the entry for it on the site index. The two things on it
      that describe live behaviour rather than a migration - the login
      brute-force limit and user impersonation - were rewritten as statements of
      what is true now and moved to `getting-started/example-skeleton`, next to
      the `UILogin.login()` it already described.
- [x] Strip version-history asides from the pages that carry them. **Done
      2026-09-05** for all seven: `about`, `70-implementation-details/state-management`,
      `99-todo/subpages`, `components/rules`, `getting-started/intellij-plugin`,
      `look-and-feel/animations` and `testing/junit-testing`. See the decisions
      log entry of that date for what each of them said and says now.
- [x] Delete demo pages that demonstrate removed or discouraged APIs. **Done
      2026-09-05**: the thirteen component groups deleted the pages they
      superseded as they went, and the last two remnants - the old binding
      tutorial (`pages/binding/tut1/**`) and the whole pre-`component2`
      `pages/overview/**` tree, with `BasicOverviewPage`, `TableMenuPage` and
      `DataTable1Page` - went with the entry of that date. The two things that
      tree still demonstrated, the week agenda and drag and drop, were rewritten
      as component group demo pages and documented.

- [x] Remove the node-carrying handler interfaces `IClicked`, `IClicked2`,
      `IClickBase` and `IValueChanged`, and every method that took one. **Done
      2026-09-06**: `IExecute` is now the only click and change handler, and
      `setClicked2()` takes the new `IClickedInfo` (the `ClickInfo`, no node).
      See the decisions log entry of that date.

- [x] **Framework: `PropBtnPart` and `ButtonPartKey` are dead.** **Done
      2026-09-07**: `PropBtnPart`, `ButtonPartKey` and `PropButtonRenderer` are
      gone, and with them `ThemeCssUtils.buttonURL()`, the only code that still
      built a URL for that part. A client stylesheet could name the part by URL,
      so this was the user's call, not a grep's: the properties-file button
      painter is obsolete and goes.
- [x] **Framework: a dead `setDefaultThemeName` call.** **Done 2026-09-07**: the
      `setDefaultThemeName("blue/domui/blue")` line in `DomApplication` is gone;
      the `setDefaultThemeFactory(SassThemeFactory.INSTANCE)` on the next line
      sets the same field to `scss-winter-default-default`.
- [x] **Framework: `TableFormLayouter` is dead.** **Wrong, and closed 2026-09-07**:
      it is not dead. The class is usable and used through the public
      `FormBuilder(IFormLayouter)` constructor by application code outside this
      workspace. It was removed and restored the same day, with `_form4.scss` and
      the `ui-f4-*` class names. See the decisions log entry of that date for the
      rule this produced for the rest of phase 4.
- [x] **Skeleton: the theme override example is the wrong pattern.** **Nothing to
      do, 2026-09-07**: the wholesale `_variables.scss` copy the item described
      lived in `domui/examples/skeleton`, which "Delete obsolete examples package"
      removed on 2026-09-07. `domui-skeleton` itself already overrides through an
      empty `_custominit.scss`, which is the supported hook.

- [x] **Framework: remove the obsolete theme engines and the Rhino theme
      templating.** Done 2026-09-07, at the user's direction, after an
      investigation written up in `domui/finished-plans/THEMES.md`. DomUI carried three theme
      engines; only the SCSS one has been selectable since 2017. Removed: the
      `simple` and `fragmented` factories and their registrations; the Rhino
      template machinery they shared (`ThemeManager.getThemeReplacedString()`,
      the deprecated `getThemeMap()`, `ITheme.getPropertyScope()`,
      `DomApplication.augmentThemeMap()`, `ThemeCssUtils`, `CssColor`);
      `ThemePartFactory` (`*.theme.*`) and `SvgPartFactory` (`*.png.svg`);
      `CleanThemeVariant`; `PartUtil.loadProperties()` and `PartUtil`'s
      URL-parameter splitting; `TestRhino`, `extra/TestThemeExpander.java`,
      `makeunsplit.xml` and the orphaned `extra/*.png.svg`; and every
      `resources/themes/` directory except `scss/` - 152 `.frag.css` files among
      them. 693 files, ~28,300 lines. `GrayscalerPart` and `MarkerImagePart` keep
      SVG support, now without theme expansion and without `w`/`h` parameters.
      Rhino stays: `HtmlFullRenderer.renderTemplatePage()` and `OopsFrameRenderer`
      use `RhinoTemplate` for page and error-frame templates, which is not
      theming. Docs: the "stylesheets that are not this theme" section of
      `look-and-feel/the-winter-theme` was deleted and the variant callout in
      `look-and-feel/themes` no longer names `-clean`. Verified: `mvn21 clean
      install` green; `to.etc.domui` 57 unit tests, `to.etc.domui.demo` 9 unit
      tests and 55 Selenium ITs all pass; site build 156 pages clean; against a
      locally run demo `$THEME/<theme>/style.scss` returns 472 KB of CSS and
      `btnCancel.png`, `GrayscalerPart` and `MarkerImagePart` all serve.

- [x] **Framework: one theme, with variants that actually work.** Done
      2026-09-07, at the user's direction, straight after the theme-engine
      removal. DomUI could hold several theme factories at once, registered in a
      static map and selected by the leading word of a four/five-part theme name
      embedded in every themed URL (`scss-winter-default-default`). That is gone:
      `DomApplication` holds one `IThemeFactory`, set with `setThemeFactory()`
      during initialization, and the first segment of a themed URL is now just the
      **theme variant** (`$THEME/default/...`, `$THEME/dark/...`). Removed:
      `THEME_FACTORIES`, `register(IThemeFactory)`, `getFactoryFromThemeName()`,
      `setDefaultThemeName()`/`getDefaultThemeName()`, `getDefaultThemeInstance()`,
      `IThemeFactory.getFactoryName()`/`getDefaultThemeName()`/`appendThemeVariant()`,
      `IRequestContext.getThemeName()`/`setThemeName()`, `calculateUserTheme()`, and
      the icon-set/colour-set search path entries. Added: `IThemeVariant.of(name)`,
      `IThemeFactory.getTheme(da, variant)` + `getDefaultVariant()`,
      `calculateUserThemeVariant()`, and `$themeVariant` as an scss variable.
      `setThemeVariant()` now stores the name in the **session**, so a dark/light
      choice holds across requests, and clears the request's cached `ITheme` so it
      takes effect on the page that makes it. A variant puts one directory in front
      of the theme search path, so `winter/dark/_color.scss` overrides colours and
      every unreplaced file still comes from `winter`.
      `SassPartFactory.decodeKey` takes the variant for its cache key from the URL
      rather than the requesting session, so two sessions in different variants
      share one entry per sheet. Docs: `look-and-feel/themes` rewritten around the
      one-theme/variant model with a worked dark example, and the
      `setDefaultThemeFactory` call in `getting-started/example-skeleton` updated.
      Verified: `mvn21 clean install` green, 57 + 9 unit tests and 55 Selenium ITs
      pass, site build 156 pages clean; and against a locally run demo with a
      throwaway `themes/scss/winter/dark/_color.scss` in the demo webapp,
      `$THEME/dark/style.scss` carried the overridden colour and `$themeVariant`
      of `dark` while `$THEME/default/style.scss` carried neither, and
      `btnCancel.png` resolved in both variants.

- [x] **Framework: the winter theme takes its colours from variables.** Done
      2026-09-07, immediately after the dark variant, at the user's direction: the
      dark variant needed a second file (`dark/_variantstyle.scss`) full of rules
      only because the partials wrote their colours literally, and replacing those
      literals with variables should remove the need for it. It did. The variant is
      now **one file** and `_variantstyle.scss`, its empty base copy and the
      `@import "variantstyle"` hook added earlier that day are all gone again.
      New semantic variables in `_variables.scss`: `$body-color`, `$surface-bg`/
      `-color`/`-border`, `$surface-alt-bg`, `$window-bg`, `$pane-border`,
      `$menu-border`/`-hover-bg`/`-hover-border`, `$input-bg`/`-color`,
      `$input-ro-bg-top`/`-bottom`, `$row-hover-bg`/`-outline`,
      `$row-select-hover-bg`/`-outline`, and `$line-color`. Changed to use them:
      `_core` (body, select, .listtbl), `_panel`, `_captionedpanel`,
      `_labelselector`, `_floatingWindow`, `_hamburgermenu`, `_popupmenu2`,
      `_layout`, `_datatable`, and `ui-input-base`/`ui-ro-base` in
      `bulmaish/_core_defs`. The vendor `calendar-theme.css` became
      `_calendarTheme.scss` with 28 `$cal-*` variables, each defaulting to the
      literal it replaced, and moved below the variable imports in `style.scss` so
      its own !defaults no longer beat a variant's values. Two of the changes are
      additions, not substitutions: the theme never set a text colour on `body` nor
      a background on text inputs, both relying on the browser default - and a
      variant cannot override a colour nobody wrote. **The light theme is
      unchanged**, verified by compiling `$THEME/default/style.scss` before and
      after and diffing: no colour value differs; the only differences are
      `#FFF`/`#fff`/`#FFFFFF` respelled `white`, the calendar block moving
      position, 20 no-op `color: inherit`, 12 `background-color: white` where the
      browser already painted white, and three white-on-white borders becoming
      `transparent`. Verified: `mvn21 clean install` green, 57 + 9 unit tests and
      55 Selenium ITs pass, site build clean; and in a running demo light and dark
      both correct on the home page, the CD-shop artist list, the DateInput2 page
      (read-only and disabled fields included) and a tutorial page.
- [x] **Framework: the theme's border greys are one colour.** Done 2026-09-08 at
      the user's direction, closing the item the previous entry deliberately left
      open. The theme drew its lines in four near-identical greys - `#8c8c8c`
      (hamburger menu), `#aaa` (floating window), `#BBB` (layout pane) and
      `#aaaaaa` (table cell rules) - which the variabilisation had turned into four
      variables saying the same thing (`$surface-border`, `$pane-border`,
      `$menu-border`, `$separator-color`). All four are now `$line-color`,
      defaulting to the median `#aaa`; the other three names are gone rather than
      kept as aliases, so there is one name as well as one colour. The dark variant
      loses three of its four values with them. `$line-color` is deliberately
      distinct from the existing `$border` (`_derived-variables.scss`), which is
      the *control* border and comes from the greyscale ramp - the comment on it
      says so. Unlike the variabilisation, this **does** change the light theme, in
      exactly three compiled lines: `.listtbl TD` #aaaaaa -> #aaa (the same colour
      respelled), `.ui-hmbrg-menu` #8c8c8c -> #aaa (lighter) and `.ui-layout-pane`
      #BBB -> #aaa (slightly darker). Verified by diffing the compiled light sheet
      before and after - those three lines and nothing else - and by reading the
      computed border colour of all four selectors out of the running demo in both
      variants: one value each. `mvn21 clean install` green, 57 + 9 unit tests and
      55 Selenium ITs pass, site build 156 pages clean.

- [x] **Framework + demo: a dark variant of the winter theme, and a switch for it.**
      Done 2026-09-07, at the user's direction, on top of the one-theme/variant
      rework of the same day. The dark theme is **two files** in
      `resources/themes/scss/winter/dark/` and copies nothing: `_color.scss`
      (imported in place of the theme's own, before `_variables.scss`, so its
      values beat the `!default`s and `_derived-variables.scss` recomputes the
      whole derived palette from them - mostly by turning the greyscale ramp
      upside down) and `_variantstyle.scss` (the rules variables cannot reach).
      The second file exists because measurement showed the theme writes most of
      its colours literally: 231 hex literals across 48 partials plus 84 bare
      `white` keywords, against ~60 uses of an overridable colour variable. To
      make it possible `style.scss` gained one line as its **last** import,
      `@import "variantstyle"`, with an empty `winter/_variantstyle.scss` beside
      it - the same shape as the existing `_custominit`/`_userstyle` hooks but
      for rules rather than variables, so a variant's rules land after the
      component they correct and win on source order. Framework also gained
      `DarkThemeVariant`. Demo: `ThemeVariantSwitch`, a sun/moon button added to
      `SourceBreadCrumb` (so it is on every page), which sets the session variant
      and calls `WebUI.refreshPage()` - needed because the stylesheet link is
      written by the full renderer, and the refresh keeps the conversation so page
      state survives. The demo's own stylesheet now imports `parameters` and
      branches on `$themeVariant` for its own hardcoded colours
      (`css/_darkstyle.scss`). Docs: `look-and-feel/themes` gained the dark
      variant as its worked example. Verified: `mvn21 clean install` green, 57 + 9
      unit tests and 55 Selenium ITs pass, site build 156 pages clean; and in a
      running demo, switching to dark and back repainted the home page, the
      CD-shop artist list (search panel, input, buttons, datatable header band,
      row hover) and a tutorial page using the demo's own card/query-box styling,
      with the light variant unchanged.

- [x] **Framework: the dark/light choice survives the session, and starts from what
      the browser prefers.** Done 2026-09-09 at the user's request. Two halves.
      *Persisting:* `RequestContextImpl.setThemeVariant()` writes a
      `domui-theme-variant` cookie (webapp path, a year, HttpOnly) beside the
      session attribute it already set, and `getThemeVariant()` reads session,
      then cookie, then the application. The cookie is marked `Secure` when the
      session cookie is - a new `IRequestResponse.isSecureCookies()`, which
      `HttpServerRequestResponse` answers from the servlet context's
      `SessionCookieConfig` - so an application that spells its session cookie for
      cross-site frames (as the demo does, see `SessionCookieSetup`) gets the theme
      cookie spelled the same way for free, and the container's `CookieProcessor`
      adds `SameSite`/`Partitioned` to both alike.
      *Starting from the browser:* a session with nothing stored gets one line of
      script at the very top of the page head (`HtmlFullRenderer.renderColorSchemeDetection`,
      right after the `color-scheme` meta and before the render blocking
      stylesheet); when `matchMedia` says the browser wants the *other* scheme it
      replaces the location with the new `ColorSchemePart` (`$colorscheme`), which
      stores the variant and 302s back to the page the request came from.
      `DomApplication.getThemeVariantForColorScheme()` maps "light"/"dark" onto
      variants and returning null from it switches the question off, which is what
      a theme with no dark variant must do. Two things make it safe: the target
      travels as a webapp relative path and anything host relative or carrying a
      scheme is refused, so the part cannot be an open redirect; and the answer is
      stored in the session as well as the cookie, so a browser that drops the
      cookie still stops asking after one round trip instead of bouncing forever.
      The conversation id is stripped from the return path - keeping it brought
      back the page built for the old variant, which showed as the sun/moon switch
      returning with the wrong icon (found while verifying, and fixed).
      Docs: `look-and-feel/themes` gained a "The first visit" section with the
      round trip as a sequence diagram. Verified: full `mvn21 install` green, 9
      unit tests and 55 Selenium ITs pass, site build 156 pages clean; and against
      a running demo in a dark-preferring Chrome - first visit lands in dark with
      no flash, the switch to light survives a server restart (so, a new session)
      and beats the browser preference, a `default` cookie renders light, and a
      garbage cookie value falls back to the default instead of failing.

- [x] **Framework: a ladder function for things that nest, and the popup menu on it.**
      Done 2026-09-08 at the user's direction, after a side-by-side of the popup
      menu in its own greys versus the ramp. Two findings drove it. First, the
      popup menu is an *inverted* surface - dark in the light theme - so the
      scan's headline "worst shift", `#999` disabled text -> `$grey-light`, is
      actually a contrast *improvement* (3.4:1 -> 5.8:1) rather than a
      degradation. Second, and the reason for the function: rounding each grey to
      its own nearest ramp step is wrong when the greys are a **sequence**. The
      menu's middle nesting levels were `#666` and `#888`, which both round to
      `$grey` - a naive collapse would have turned a three-rung ladder into two.
      Added `$grey-ramp` (`_variables.scss`), the greyscale as an ordered list,
      and `ladder($from, $level)` (`functions.scss`), which walks it; negative
      levels walk the other way for what sits under the surface, and both ends
      clamp rather than failing. `$ladder-direction` inverts along with the ramp,
      so a ladder steps away from the page ground in either variant - the dark
      variant's whole entry for the popup menu is two lines
      (`$ladder-direction: -1`, `$pmnu-bg: $white-ter`). `_popupmenu.scss` now
      expresses every colour as an offset from `$pmnu-bg`, including its item text
      via the theme's own `findColorInvert()`. Light-theme effect is exactly the
      column the user picked, seven compiled lines, all inside `.ui-pmnu*`:
      background `#444`->`#363636`, levels `#666`/`#888`/`#aaa` ->
      `#4a4a4a`/`#7a7a7a`/`#b5b5b5`, disabled `#999`->`#b5b5b5`, title
      `#222`->`#0a0a0a`, border `black`->`#0a0a0a`. Left alone deliberately, at
      the user's direction: the deepest level puts light text on a mid grey in
      both variants - the component's nesting scheme runs out of contrast before
      it runs out of levels, and a real popup menu does not nest three deep.
      Verified: `mvn21 clean install` green, 57 + 9 unit tests and 55 Selenium ITs
      pass, site build 156 pages clean, and both variants rendered from the
      compiled output.

- [x] **Framework: the error colours go through the theme's own variables.** Done
      2026-09-08 at the user's direction, as the first slice of the colour
      collapse. Seven literals in six components wrote an error colour by hand
      while `$errors_border` / `$errors_foreground` / `$errors_input_background`
      already existed and were already used in fifteen other files - and were
      already set correctly by the dark variant. Changed: `_ace` (`.ui-ace-error`
      underline), `_asyio` (the async-IO failure dialog's border, title band and
      surface), `_bugIndicator`, `_enumsetinput` (the delete button's cross),
      `_errorpanel` (`.ui-err-cont`). One new variable, `$errors_wash`, for the
      dialog's own red surface, defaulting to the `#ffaaaa` it replaced;
      `$errors_background` was **not** pressed into service for it because that
      variable is `#a9c5f1`, blue, which is plainly an old mistake and worth
      leaving visible rather than papering over. **The light theme is
      unchanged**: the compiled diff is five lines, every one of them the keyword
      `red` respelled as `#ff0000` - the same colour, because that is exactly what
      `$errors_border` and `$errors_foreground` are set to. The dark variant now
      renders those six places in its own reds (`#e75555`, `#f28888`, `#5b2929`)
      instead of pure `#ff0000`. Verified: `mvn21 clean install` green, 57 + 9
      unit tests and 55 Selenium ITs pass, site build clean, and the six rules
      read out of both compiled sheets.
- [x] **Framework: `$errors_background` was blue; it is gone.** Done 2026-09-08 at
      the user's direction, closing what the previous entry had only flagged.
      `_errorMessageDiv` and `_flare` both paint a state with a bg/fg/border
      triplet - `$info_bg`/`$info_fg`/`$info_border` for info, the `$warnings_*`
      three for warning - and the error member's background was `#a9c5f1`, a
      blue, which made an error message render blue in a component whose other
      two states were correctly coloured. `$errors_background` is deleted, both
      uses now take `$errors_wash`, and the dark variant loses its entry for it.
      **This one does change the light theme**, deliberately. Seeing it on screen
      then showed a second problem the variable rename had exposed rather than
      caused: `$errors_wash` was `#ffaaaa` (L83%) while `$info_bg` and
      `$warnings_background` are L95% and L97%, so an error band shouted over a
      warning band sitting right under it - and the dark variant was off the same
      way, its wash 6-8 points lighter than its two siblings. Both are levelled:
      `#ffe5e5` in light, `hsl(0, 32%, 20%)` in dark, matching the other two
      states. Net light-theme effect, three compiled lines: `.ui-emd`,
      `.ui-flare-error` and `.ui-ioe-asy` go `#a9c5f1`/`#ffaaaa` -> `#ffe5e5`.
      Seen on screen in both variants on the demo's ErrorDisplayPage, error and
      warning together.
- [x] **Framework: `_bugIndicator.scss` is imported.** Done 2026-09-08 at the
      user's direction. `style.scss` gained `@import "bugIndicator"` next to the
      other floating overlays, so the 26 lines that style
      `DefaultBugListener`'s indicator now compile instead of being silently
      dropped. Verified present in the compiled sheet; **not** seen rendered,
      because nothing in the demo raises a bug - so the indicator's own colours
      (a `black` border and a `yellow` count on the error red) have not been
      looked at on screen and may want a second pass.

- [x] **Framework: the theme's accent is `$primary`, and it is one colour.** Done
      2026-09-08 at the user's direction, after an investigation of the warm
      family (32 distinct colours over 41 uses). Two findings drove it. The brand
      colour was called **`$turquoise`** and held `#f69231`, an orange: the
      commented-out line above it, `//$turquoise: hsl(171, 100%, 41%)`, shows
      someone overrode bulma's real turquoise in place rather than renaming it, so
      the theme's accent was a variable named for a cyan - and it had almost no
      uptake, `$turquoise` reaching nothing but `$primary` and `$primary` reaching
      one component. And `#ff9436` had three spellings: `$selected_bg`,
      `$pmnu-hover-bg` and a raw literal in `_logtailer`, sitting dE 3.2 from the
      brand orange - close enough to be indistinguishable while doing a different
      job. `$turquoise` is deleted; `$primary: #f69231` is defined at the head of
      the colours in `_variables.scss`, before anything derives from it;
      `$turquoise-invert` is gone and `$primary-invert` computes
      `findColorInvert($primary)` directly; and `$selected_bg` and
      `$pmnu-hover-bg` now take `$primary`, which is the user's call that the
      accent and the selection highlight are one colour rather than two. Light
      theme: **four compiled lines**, the tree's selected row and the popup menu's
      hover/subsel going `#ff9436` -> `#f69231`; `.ui-button.is-primary` is
      byte-identical, which is the check that the rename and the invert rewiring
      are inert. Verified: `mvn21 clean install` green, 57 + 9 unit tests and 55
      Selenium ITs pass, site build clean.

- [x] **Framework: a two-tier colour architecture and a naming convention.** Done
      2026-09-08 at the user's direction, generalising what the accent slice had
      arrived at case by case. Colours now live in two tiers: the **main set** in
      `_variables.scss`, whose names say what a colour is in the theme's
      vocabulary and never name a component; and **component colours** in
      `_derived-variables.scss`, one variable per colour a component paints, each
      defaulting to a main-set value. A component's stylesheet reads only its own
      variables - never a literal, never a main-set value directly - which is what
      lets an application restyle one component and a variant move the whole theme
      without either editing a component. The convention, in kebab-case, is
      `$<component>-<part>-<role>`: component is the CSS class prefix with `ui-`
      removed (`tlf` for `.ui-tlf-*`), part is the element inside it where there is
      more than one, and role is `-bg` / `-color` (text, never a background) /
      `-border` / `-outline` / `-shadow`. It is written at the top of
      `_variables.scss` and in the `look-and-feel/themes` chapter.
      This **codifies rather than invents**: a survey found kebab-case already
      dominant 223 to 69, and the component-prefix pattern already in use by
      `$brcr2-*`, `$esic-*`, `$rbb-*`, `$lui-*` and the `$cal-*` set. The 69
      snake_case holdouts are concentrated in four files (`_datatable`,
      `_monthpanel`, `_draganddrop` and older names in `_variables.scss`) and are
      left to be renamed as each component is worked on.
      Seeded with two components: `_logtailer` gains `$tlf-hdr-bg: $primary`,
      which was the user's specific ask and the last of the three spellings of the
      old selection orange - **one compiled line**, `#ff9436` -> `#f69231`; and
      the `$pmnu-*` block moves out of `_popupmenu.scss` into the new section as a
      worked example, which compiled **byte-identical** and is the proof the move
      is inert. Verified: `mvn21 clean install` green, 57 + 9 unit tests and 55
      Selenium ITs pass, site build 156 pages clean.

- [x] **Framework: collapse the theme's colours onto a set.** **Done 2026-09-08.** A perceptual scan of
      every literal in `themes/scss/winter` (2026-09-08) found 233 occurrences,
      137 of them distinct, and enough near-duplicates to make a set worth
      defining. The evidence, in order of how much it buys:
      - **The greyscale ramp already exists and no partial uses it.**
        `_variables.scss` defines 11 steps (`$black` … `$white`); only
        `_derived-variables.scss` consumes them. The partials instead hand-pick
        **33 distinct greys over 178 occurrences**. The `$grey` band (L~48%) has
        eight of them - `#777777`, `#808080`, `#666666`, `#7c7c7c`, `#898989`,
        `#888888`, `#727272`, `#998888` - which nobody can tell apart. Three
        partials hardcode a value that *is* a ramp step: `#363636` (`_form5`) =
        `$grey-darker`, `#dbdbdb` (`_genericheader`) = `$grey-lighter`, `#0a0a0a`
        (`bulmaish/_core_defs`) = `$black`. Mapping all of them onto the ramp
        gives 150 occurrences on 9 steps; the largest visible shift is dE 10.5
        (`#999999` -> `$grey-light`), and only 11 colours shift by more than dE 3.
      - **`_popupmenu` is done** (2026-09-08): it is on `ladder()` and carries no
        colour literal except the accent orange.
      - **The error colours are done** (2026-09-08). The other three state
        families turned out not to exist: the "warning ambers", "info blues" and
        "success greens" the scan grouped by hue are a breadcrumb link, a badge
        digit, a selected-item background, a toggle's branding parameter, a
        pop-in panel's ground and a menu title's tint - not states. Hue grouping
        found them; reading the selectors dismissed them. Two literals must stay
        literal forever: the colour picker's three `#f00` sample swatches, and
        `red($main_color)` in `_draganddrop`, which is the SCSS channel function
        and not a colour at all.
      - **The greys split cleanly by role**, which is what the semantic names
        should be built from: text (7 greys / 32 uses), line (16 / 44), surface
        (14 / 37), shadow (4 / 13).
      - **"Disabled" is invented four times.** `_popupmenu` uses `#999`,
        `_datapager2` `#777`, `_hamburgermenu` the `grey` keyword, `_radiobutton`
        `#ddd` - and `$button-disabled-color: #999999` already exists, used by
        `_radiobutton` alone. One `$text-disabled` would cover all of them.
      - **`_scrollableTable` is a copy of `_datatable`'s palette.** It redeclares
        the same five `$data_tbl_*` variables with identical values, and hardcodes
        the four row-hover colours that `_datatable` now takes from
        `$row-hover-*`/`$row-select-hover-*`.
      - **The accent is done** (2026-09-08): `$turquoise` - a variable named for
        a cyan that held the brand orange - is gone, `$primary` holds `#f69231`
        directly, and `$selected_bg`/`$pmnu-hover-bg` take it, so the accent and
        the selection highlight are one colour. What is left of the warm family:
        `_logtailer` still writes `#ff9436` raw for its header band (the third
        spelling of the old selection orange, and now the only thing not on the
        accent); `_tree2` writes `#f69231` raw where it means `$primary`;
        `_tree3` and `_scrollableTable` write `#ffd55a`/`#fff4d3` raw where they
        mean `$row-hover-outline`/`$row-hover-bg`; and the button ramp
        (`$button-top-color` `#f69231`, `$button-bottom-color` `#f6ac3d`,
        `$button-focus-top-color` `#f6c381`) is three hand-mixed shades whose hue
        drifts 30 -> 36 -> 34 where a `lighten()` chain off `$primary` would not.
        The amber hover set is already correct - one hue at three weights.
      **Check for ladders before mapping anything.** Where a component's greys form
      a *sequence* - nesting depth, striping, magnitude - they take consecutive
      rungs from one base via `ladder()` (see the log entry of 2026-09-08), not
      nearest-neighbour matches one at a time: the popup menu's `#666`/`#888` both
      round to `$grey` and would have lost a nesting level. Checked 2026-09-08:
      the popup menu is the theme's **only** true depth ladder - the trees encode
      indentation with images and have no per-level colour at all, and the tab
      panel's header/tab/content is a three-surface stack rather than a nesting
      sequence (it could use `ladder()`, but nothing breaks if it does not).
      Proposed shape: keep the ramp as the base, add semantic names on top of it
      (`$text`, `$text-disabled`, `$line-color` (done), `$surface-*` (done),
      `$shadow-color`), and one accent/state set fed by the `$errors_*`,
      `$warnings_*`, `$info_*` variables that already exist but that components
      bypass. Note this **does** change the light theme, unlike the earlier
      variabilisation - the shifts above are real and want reviewing on screen.
      **The architecture and the naming convention are settled** (2026-09-08, see
      the log): a component's colours go in `_derived-variables.scss` as
      `$<component>-<part>-<role>`, defaulting to a main-set value, and the
      component reads only those.
      **The sweep is under way, batches 1 and 2 done** (2026-09-08): 214 literals
      in 55 component files at the start, **47 left**. The main set gained the
      values the sweep keeps reaching for: `$line-strong` (the darker line - an h1
      rule, a table header divider), `$text-muted` (a label, a hint, an item that
      cannot be used - the theme wrote #666, #777, #808080 and #999 for this one
      idea) and `$shadow-color`.
      Two exceptions to "default to a main-set value" are settled and documented:
      a component with a **categorical** palette keeps its own values, because the
      entries have to stay apart from each other and the theme's semantics cannot
      say that. `$copa-level-bgs` (ConditionPanel, one colour per nesting depth -
      the same ladder shape as the popup menu but with a palette instead of the
      ramp) and the jquery-layout resizer's green/red/amber feedback set are both
      of that kind. `_devmode` is skipped entirely at the user's direction: its
      near-black purples are a deliberate "this is not production" signal.
      Also fixed: the `$data_tbl_*` palette was declared twice, identically, in
      `_datatable.scss` and `_scrollableTable.scss` - and it is not one component's
      anyway, five files read it (`_datatable`, `_scrollableTable`,
      `_expandingtable`, `_monthpanel`, `_dateinput`). It now lives once, in
      `_derived-variables.scss`, and neither component declares anything. Removing
      it was inert. Two things it surfaced: `$data_tbl_header_btm_border` is
      `undefined`, which is not a colour, so the two rules using it emit invalid
      CSS and the header's left and top borders have never rendered - left alone,
      since giving it a colour makes borders appear that never have; and
      `$data_tbl_header_text_color` stays **black** rather than becoming `$text`,
      because the header sits on its own coloured band where `$text` drops the
      contrast from 10.9:1 to 4.6:1, on the AA threshold for bold text people
      scan.
      Batch 3 (2026-09-08) took the greys, whites and shadows together - 63 of
      them across 26 files - because every target already existed and none of it
      needed a decision. Biggest shift dE 8 (`#666` -> `$text-muted`, three
      places); about half was literally inert (`#eeeeee`->`#eee`, `white`->`#fff`,
      `#AAA`->`#aaa`) and twenty were `rgba(10,10,10,.1)` -> `rgba($shadow-color,
      .1)` at 10% alpha.
      **68 left**, and they need judgement rather than mapping:
      - **blacks: done** (2026-09-08). Three roles, read one at a time. Eight
        hard borders and one inverted background became `$line-hard`, a new third
        step of the line scale (`$line-color` #aaa, `$line-strong` #7a7a7a,
        `$line-hard` $black) - a hairline shift, dE 2.7. Nine `color: black`
        became `$text`: each was checked against the ground it sits on first,
        21:1 -> 8.9:1 on page white and 19.8:1 -> 8.3:1 on the readonly input
        wash, both comfortably above AA. Two MonthPanel dims written as
        `lighten(#000000, 55%/65%)` became `$text-muted` and `$grey-light`. One
        black stayed black on purpose - see the bug below.
      - **blues, about 14.** A family needing one decision, exactly as the
        oranges did. The theme already has *two* link colours (`$link_color:
        #2200cc` and `$link: $blue`) while components separately reach for
        `#013686`, `#051cac`, `#297bc0`, `#1700ee`, `#3273dc` and `#2196f3` for
        "a link, a header, an active thing". Someone has to say which blue.
      - **other hues, 17.** Mostly genuinely component-specific by now: the
        colour picker's `#f00` swatches (stay literal), `_popInPanel`'s greens,
        `_oddcharacters`, `_breadcrumb` (superseded), `_bugIndicator`'s yellow.
      - `_devmode` (8), skipped, and `_old_defaultbutton` (2), which is not
        imported and should be deleted rather than fixed.
      **Batch 5, the blues and the last of the hues, finished the sweep**
      (2026-09-08). Which blue is *the* blue turned out not to need answering,
      because the family split cleanly in two on measurement. The violet-leaning
      link blues (hue 232-250) are `$link_color` and three near neighbours; the
      cyan-leaning accent blues (hue 207-218) are `$blue`/`$link` and four more.
      Nothing in the second group is within dE 16 of `$blue`, so calling them all
      one colour would have been a restyle wearing a dedup's clothes.
      What was actually collapsed, and by how much: `bulmaish`'s focus ring wrote
      `#3273dc` and `rgba(50, 115, 220, 0.25)` raw where `$link` and
      `$input-focus-box-shadow-color` already said the same thing (**dE 0**, and
      the reason the dark diff is four times the light one - eleven focus rings
      across the theme were pinned to a light-mode blue and now follow the
      variant); `_tree2`'s hover wrote `#f69231` where it meant `$primary` (dE 0)
      and `#f4f7f9` where `$white-ter` was already the surface (dE 1.5);
      `_tree3`'s hover outline wrote `#ffd55a` where `$row-hover-outline` said it
      (dE 0); `_oddcharacters`' hover took `$row-hover-bg` (**dE 9.1**) and
      `_percentageprogress`' second bar took `$line-color` (**dE 9.0**) - both on
      the strength of decisions already made, one hover wash and one line colour;
      and `_expandingtable`'s expand cell, which is underlined and takes a
      pointer, took `$link_color` (**dE 15.3**) because it is a link and because
      it was unreadable in the dark variant while `$link_color` is corrected
      there. Two headers were found to share one colour, `#297bc0` in
      `_genericheader`'s level 2 and `_expandingheader`'s bar; it is now
      `$header-accent-color` with both components defaulting to it.
      Everything else got a variable holding its own value, which is the point of
      the derived tier rather than a failure of it: the schedule's
      `$wa-item-bg`/`$wa-note-bg`, `_popInPanel`'s two greens, the switch's on
      colour, `_breadcrumb`'s `#ffff99`, the bug badge's yellow, the cookie
      panel's `#EE4B5A`, `_forms`' read-only wash, GenericHeader's slate and deep
      blues, DataPager2's hover, and Ace's navy rule. `_breadcrumb2`'s five
      `$brcr2-*` declarations moved out of the partial into the derived tier with
      them. One line was deleted rather than variabilised: `_expandingtable`'s
      `filter: progid:DXImageTransform.Microsoft.Shadow(...)`, an IE<=9 shadow
      that no browser DomUI supports has read for a decade.
      **The sweep is finished.** 214 literals in 55 component files at the start;
      what is left is nine occurrences that are all deliberate - the colour
      picker's three `#f00` sample swatches, `_draganddrop`'s
      `red()`/`green()`/`blue()` (SCSS channel functions, not colours), two hex
      values quoted inside a comment in `_popupmenu.scss`, and `_devmode`, which
      is skipped by direction. Total effect on the light theme across all five
      batches: every change measured, the largest single shift dE 15.3, and the
      dark variant now reaches colours that were previously nailed to light mode.
      Verified: `mvn21 clean install` green.


- [x] **Framework: `findColorInvert()` picked white where black was needed.**
      **Done 2026-09-08.** The helper answers "what text colour goes ON this
      colour", and it decided with a single luminance cut - the theme's softened
      black above 0.55, white below - which is not what legibility depends on. A
      mid-tone got white however badly white performed on it. `$primary`
      (#f69231) measures 0.456 and so took white, at **2.3:1** against the 4.5:1
      AA floor, where black gives 5.5:1; it is why `_cookiewarning` hand-wrote
      `color: black` for its accept button instead of asking the helper.
      It now compares the two candidates and returns the one that wins. A new
      `contrastRatio()` in `functions.scss` does the WCAG arithmetic on the
      existing `colorLuminance()`, and `findColorInvert()` measures white against
      `mix(#000, $color, 70%)` - what `rgba(#000, 0.7)` actually composites to
      over the colour, so the softening is part of the comparison rather than
      ignored by it.
      The effect is confined to the eight `.ui-button.is-*` colour classes and
      their `.ui-sib` equivalents, and it is not a blanket flip to black: **`link`
      keeps white**, because on `$blue` white genuinely wins (4.5:1 against
      3.5:1), which is the evidence that the function is measuring rather than
      inverting. Light variant, four changed: `primary` 2.3 -> 5.5, `info` 3.0 ->
      4.7, `success` 2.0 -> 5.9, `danger` 3.5 -> 4.3. Dark variant, five: the same
      four plus `link`, whose blue lifts to #64adf7 there and no longer supports
      white (2.4 -> 5.4). `warning`, `light` and `dark` were already right in both.
      `$text-invert` does not move: white on `$text` is correct in the light
      variant and the old rule already gave black in the dark one.
      With the helper fixed, `$ckw-accept-color` drops its hand-written `black`
      and takes `$primary-invert` like every other coloured surface.
      Two things left alone deliberately. `colorLuminance()` squares the
      linearised channels where sRGB raises them to 2.4 - inherited from bulma,
      and it reads light (0.456 for #f69231 rather than 0.404) - but the error
      goes the same way for both colours of a pair and every comparison above
      comes out identical either way; `powerNumber()` only takes integer
      exponents, so fixing it means writing a fractional power in SCSS for no
      change in output. And `findColorHighlight()` still cuts at 0.30; it chooses
      between `lighten` and `darken`, not between two contrasts, so the same
      argument does not apply to it.
      Verified on screen in both variants, and `mvn21 clean install` green.


- [x] **Framework: the theme's variable names are all kebab-case now.**
      **Done 2026-09-08.** 79 names still spelled with underscores - `$data_tbl_*`,
      `$month_panel_*`, `$dnd_*`, `$tab_pnl_*`, the `$errors_*` / `$warnings_*` /
      `$info_*` states and a group of older names in `_variables.scss` - across 366
      occurrences in 46 files, including the dark variant. Kebab-case had already
      won 223 to 69 and is the written convention.
      The plan had said to do these per component rather than in one sweep, on the
      grounds that a sweep is a big diff that hides the changes which are not
      inert. That objection is answerable rather than permanent: the sweep was done
      in one pass and then **proved** inert by compiling both variants before and
      after and diffing. The compiled stylesheets came back **byte-identical** in
      the light variant and in the dark one, and every component had been worked on
      by then anyway, which was the other half of the reason to wait.
      The one line that did change was not a rule: `_errorpanel.scss` carried a
      commented-out `background-color` inside a `/* */` comment, which libsass
      copies into the output, so the theme shipped a dead declaration to every
      browser. That comment and the `//` one under it are deleted.
      **Why it is free.** SCSS treats `-` and `_` as the same character in an
      identifier: `$link_color` and `$link-color` are one variable, not two. An
      application that overrides a theme variable by its old spelling therefore
      keeps working untouched, which is what separates this rename from the ones
      still open - those change the identifier and would break such an override.
      Documentation brought along: `look-and-feel/themes`,
      `look-and-feel/overriding-the-theme` and
      `look-and-feel/styling-your-component` all spelled variables the old way in
      their tables and samples; so did `finished-plans/THEMES.md`. The callout warning readers that
      69 names had not been converted is replaced by the fact that matters to them -
      that the two spellings are the same variable.

- [x] **Framework: `_old_defaultbutton.scss` deleted.** The one partial `style.scss`
      did not import, superseded by `_defaultbutton.scss`. Deleted by the user
      2026-09-08.


- [x] **Framework: every theme variable is now in the convention's shape.**
      **Done 2026-09-08**, the second half of the naming work and, unlike the first,
      not free: 104 variables changed identifier, so an application overriding one of
      them by its old name has to be updated. The complete old -> new table, with a
      reason per row, is section 14 of `finished-plans/THEMES.md`. All of it
      compiled **byte-identical in both variants** - only names moved.
      Four kinds of wrong name, and the first is the one worth remembering.
      **Three components had two prefixes each.** The tab panel was styled through
      `$tabpanel-*` (six names, in the derived tier) *and* `$tab-pnl-*` (nine, in
      `_variables.scss`); the switch through `$switch-*` and `$ui-swtch-*`; DataPager2
      through `$dp2-*` and `$pager2-*`. Each pair had drifted into two homes with no
      hint in either that the other existed, which is exactly the failure the
      convention prevents. They are now `$tab-*`, `$swtch-*` and `$dp2-*`.
      **Prefixes that were not the component's CSS class**: `$data-tbl-*` styles
      `.ui-dt-*`, `$month-panel-*` styles `.ui-mp-*` (and the derived tier's section
      header claimed `.ui-mpnl-*`, which does not exist), `$ghdr-*` styles
      `.ui-generichd-*`, `$ddtbl-*` styles `.ui-dd-table`, `$dnd-*` styles
      `.ui-drp-*`, `$multiple-lookup-label-*` styles `.ui-mli-*`. Four stale section
      headers in `_derived-variables.scss` were corrected against the partials at the
      same time.
      **Role words that were not roles**: `-foreground`, `-background`, `-fg`, `-col`,
      `-rule`, `-frame`, `-divider`, and names with no role at all. Two said the role
      twice (`$data-tbl-border-color`, `$data-tbl-header-text-color`) and one said two
      roles at once (`$data-tbl-cell-highlight-link-color-bg`).
      **Names that were lying**, which is what makes this more than tidying.
      `$multiple-lookup-label-color` painted the border and
      `$multiple-lookup-label-border` painted the background - the two were swapped, and
      are now `$mli-label-border` and `$mli-label-bg`. `$esic-label-color` was the
      label's *background* while `$esic-text-color` was its text; they are now
      `$esic-label-bg` and `$esic-label-color`. `$lui-warning-color` and
      `$lui-result-color` were both backgrounds. `$succesful-color` was misspelled,
      belonged to one component, and is `$sayt-ok-color`.
      Two variables were deleted rather than renamed: `$common-hdr-color-1`, declared
      in both variants and read by nothing, and `$button-height`, declared and read by
      nothing.
      Left alone deliberately: the vocabulary inherited from bulma (`$link`,
      `$button-*`, `$input-*`, `$size-*`, `$text*`, `$colors`, `$shades`), which is
      kept as upstream spells it, and `$can-toggle-*`, which are a vendor mixin's
      parameters rather than theme variables. `$main-color` did have to go - it is a
      palette entry, not text - and became `$green-accent`; it is the theme's second
      green, read by exactly two rules that have nothing to do with each other, and a
      candidate for collapsing into `$green`.
      Cleared out with it: the last eleven `<%= ... %>` references to the Rhino
      template mechanism removed on 2026-09-07. All were inside comments, and libsass
      copies a `/* */` comment into its output - so the theme was shipping 41 lines of
      dead template syntax to every browser, including four rules
      (`body.ui-stretch-body`, `.dso-add-bottom-margin`, `.ui-flw .ui-emd`,
      `.ui-stab-c`) that contained nothing else and are now gone as empty, and a
      commented-out `.ui-ro` block carrying a dated 2016 explanation of why it was
      commented out. The compiled diff for that removal is deletions only.
      Verified: both stylesheets byte-identical across the renames, `mvn21 clean
      install` green, site build clean.
- [x] **Framework: server-side SVG rasterisation is gone, and `GrayscalerPart`
      with it.** Done 2026-09-09, at the user's direction, after the question
      "is Batik still the best solution to rasterize SVG" was worked out (see the
      decisions log entry of that date). Removed: `PartUtil.loadSvg()`, its
      `BufferedImageTranscoder` and the `.svg` branch of `PartUtil.loadImage()`,
      which now takes GIF, JPEG and PNG only; `parts/GrayscalerPart.java` and its
      two call sites in `HtmlTagRenderer.visitImg()` and
      `DomUtil.calculateImageURL()`; that method's `disabled` parameter, with its
      one caller `PlImage` updated; and the `batik-transcoder` dependency, its
      four exclusions and the `batik.version` property - 20 jars, 4.3 MB. What
      replaces it: `Img.setDisabled()` adds the `ui-disabled` class the way
      `setClicked()` adds `ui-clickable`, and `_helperclasses.scss` carries
      `img.ui-disabled { filter: grayscale(1); }`. The `ImgPage` paragraph that
      explained the server-side grayscaler was rewritten; `finished-plans/THEMES.md` §8, §11 and
      the still-open list were updated and its open issue 3 dropped. Verified:
      `mvn21 clean install` green, 9 demo unit tests and 55 Selenium ITs pass, and
      against a locally run demo the disabled `Img` on `ImgPage` renders
      `class="ui-clickable ui-disabled"` with its original `src` and the compiled
      stylesheet carries the rule.

- [x] **The `finished-plans/THEMES.md` still-open list is empty.** Done 2026-09-09. Six items
      stood in section 15; two had already been fixed in the source since the list
      was written (`UrlPage.getThemeVariant()` - the commented-out getter is gone
      and the setter delegates to `UIContext.getRequestContext()`; and
      `ThemeManager.checkReapThemes()`, which now iterates `m_themeMap.entrySet()`
      and calls `it.remove()`, so entries really leave the map and the last one is
      no longer skipped). Fixed now: the two dangling icon references - a sweep of
      all 93 distinct `THEME/…` literals confirmed they were the only ones -
      repaired without adding image files, `BIG_ACCESS_DENIED` becoming an alias of
      `ACCESS_DENIED` (whose `accessDenied.png` is 96x114, the big one already) and
      `InternalParentTree`'s "Back to structure" button taking `Icon.faArrowLeft`,
      a font icon, which is how a `LinkButton` gets one now; `UrlPage`'s dead
      `m_themeVariant` field, left behind by the getter's removal and read by
      nothing; and `ITheme.translateResourceName()` with its four no-op
      implementations (`SassTheme`, `TestRequestContext`, `StandaloneRequest`,
      `ITheme` itself), which also let `ThemeManager.getThemedResourceRURL()` drop
      the `try`/`catch` that existed only to wrap that call. `$themes/scss/all` was
      **not** removed - see the decisions log entry of this date. The colour-literal
      item was a record of finished work, not an open one: its three rules moved
      into section 14 as "Three rules the colour sweep produced", so section 15 is
      gone entirely and sections 3, 5, 8 and 11 now carry what it used to defer.
      Verified: `mvn21 clean install` builds and unit-tests every module green,
      and `mvn21 verify -pl to.etc.domui.demo` runs 9 demo unit tests and 55
      Selenium ITs green. The install run itself ends in a jetty bind failure on
      port 8088 - an IntelliJ Tomcat was holding it - which is why the IT run was
      repeated with `-Djetty.http.port=8188 -Djetty.http.stopport=8189`; that pair
      moves the webdriver URL too, since `pom.xml:1035` builds it from the port.

- [x] **Framework + demo: the bug indicator has a demo page, and its colours have
      been looked at.** Done 2026-09-09. `BugIndicatorPage`
      (`pages/components/dialog/`) has four buttons that call `Bug.bug()` - one bug,
      one with a Throwable, five at once, and twenty-five to reach the overflow -
      and explains what a bug report is for and how an application switches the
      display on. It is linked from `ComponentListPage` under "Windows, dialogs and
      messages". Nothing had ever shown the indicator because nothing registered the
      listener: the demo's `Application.initialize()` now calls
      `DefaultBugListener.registerSessionListener(this)`.
      **The badge's colours are right in both variants.** Rendered and measured:
      light is `#ff0000` ground, `#0a0a0a` frame, yellow count; dark is `#e75555`
      ground, `#f5f5f5` frame, yellow count - the frame inverts with `$black` as it
      should, and the yellow count reaches 3.7:1 against the light red and 3.4:1
      against the dark one, both above the 3:1 that 25px bold text needs. The yellow
      is loud but it is the badge's long-standing look, so it stays.
      **One real defect, in the report rather than the badge**: the line's
      disclosure marker was `THEME/xdt-collapsed.png`, a grey box around a *black*
      plus, which on the dark variant's ground left an empty box - the "there is a
      stack trace here" affordance was invisible. It is now `Icon.faCaretRight` /
      `faCaretDown`, a font icon, which takes the theme's text colour in either
      variant. `ItemPnl` was rewritten around that: it keeps a boolean and calls
      `forceRebuild()` instead of holding `Img`, `Div` and `TD` in fields and
      mutating them.
      `_bugIndicator.scss` also read `$errors-border` - a main-set variable, and one
      named for a border - as its background. It now reads `$bug-ind-bg`, which
      defaults to `$errors-border`, so the partial reads only its own variables as
      the naming convention requires. The compiled `.ui-bug-ind` and `.ui-bug-count`
      rules are unchanged in both variants.
      Verified: `mvn21 clean install` green, and both variants driven in a browser
      against a locally run demo - badge, count, the infinity overflow, the report,
      and the marker opening and closing a stack trace.

- [x] **Framework + documentation: the browser-side build is current, and documented.**
      Done 2026-09-09. The Typescript pipeline had been frozen since 2021 and could
      not run a modern bundler, which blocked the maxGraph component planned in
      `MAXGRAPH.md`; §6.1 of that plan is this work. `frontend-maven-plugin` 1.11.0
      -> 1.15.1 and node **v8.11.1 -> v22.22.1** (the separate `npmVersion` pin
      dropped, so node's own npm is used); typescript 4.3 -> 5.9; `@types/jquery`
      ^2.0.56 -> ^3.5.32, which is the jQuery that is actually served; esbuild added
      and `npm run compile-typescript` is now `tsc` followed by a minify step, taking
      the bundle from 163KB to 74KB with a source map.
      `VersionedJsResourceFactory` now accepts `$ts/` as well as `$js/`, so
      `$ts/domui-combined.js` resolves to the `-min` sibling outside development mode
      from one unchanged reference - header contributors are registered in
      `DomApplication`'s constructor, before development mode is known, so the choice
      has to be made at resource-resolution time.
      **Two latent Javascript bugs fell out of it.** `tsconfig.json` had no `target`,
      so `class BodyTooLargeException extends Error` was emitted through the
      `__extends` shim, whose constructor returns the `Error` it built - meaning
      `x instanceof BodyTooLargeException` in `domui.fileupload.ts` was always false.
      `"target": "es2017"` emits native classes and the check works. In the same file
      `maxSize === NaN` was always false, so a garbage `fumaxsize` attribute silently
      disabled the upload size check (`NaN <= 0` is false too, so nothing caught it);
      it is `isNaN(maxSize)` now. Also removed: the ~40 committed per-file
      `.js`/`.js.map` outputs, and `resources/ts/package.json`, a duplicate that was
      being packaged into the jar.
      **Documentation**: a new page,
      `development-environment/typescript-build/index.md` ("The browser-side build"),
      which had no equivalent before - what is built and why the tsconfig lists its
      files by hand, the three files that configure it, which of the two bundles is
      served when, and how to run the Typescript build by hand. Linked from the
      section index, from the stack list in `introduction/developer-view-of-domui`,
      and from "Building the code" in `getting-started/running-the-demo`, which now
      says that the first build downloads its own node and therefore needs network
      access. The `-min` rule itself belongs with the other resource-prefix rules
      rather than with the build, so it was written into
      `look-and-feel/header-contributors`, where `$`-prefixed names are documented,
      and the new page points there.
      Verified: `mvn21 clean install` downloads node 22 and runs npm, tsc and esbuild;
      both bundles and both maps are in the jar; a jetty-run demo serves the
      75825-byte minified bundle with `-Ddeveloper.properties=false` and the
      162903-byte full bundle in development mode, at the same URL; 9 demo unit tests
      and 55 Selenium ITs green. The IT run first showed two failures in
      `ITOrderEntryPageObject` which were **not** from this work: the
      `to.etc.domui.selenium` jar in the local `.m2` predated its source and lacked
      the `*[testid$='/lbtn_Order']` fallback in
      `CpDataTableRowBase.getCellComponentSelectorCss`, so the exact-match selector
      could never find a row button (DomUI renders those calculated ids prefixed per
      row: `/r0/lbtn_Order`). Rebuilding that module made them pass - a full
      `mvn21 clean install` avoids the trap.

## Decisions log

### 2026-09-09 - The sass compiler is Dart Sass; the stylesheets still are not

`io.bit3:jsass` wraps libsass, which was declared end of life in 2020 and never got
the module system, the `sass:` built-in modules or anything else the language gained
since. Sass's own advice for a libsass wrapper is to become a host for the *embedded
protocol*, which is how Dart Sass - the reference implementation - is spoken to from
another language: the compiler is a subprocess and the host talks protobuf to it over
stdio. `de.larsgrefer.sass:sass-embedded-host` is that host for Java, and it is what
DomUI uses now.

It fits because it has **custom importers**. `SassPartFactory` compiles per request
with variables that come from the URL, and the sheets it imports are DomUI webapp
resources rather than files, so anything that only compiles from the filesystem - the
sass CLI, a build-time Maven plugin - was never an option. The three new classes in
`to.etc.domui.sass` are `DartSassCompiler` (an `ISassCompiler` over a pool of
dart-sass processes), `DartSassResolver` (an `AbstractSassResolver<ImportSuccess>`)
and `DartSassImporter` (the importer registered on a process). Every DomUI resource is
presented to dart-sass as `domui:/<resource name>`, which means dart-sass resolves the
relative paths itself and only the partial (`_name`) and suffix conventions are left
to the resolver; the virtual `_parameters.scss` is matched on its basename, so it
resolves to one canonical url from whatever directory imports it.

`SassCompilerFactory` registers it ahead of `JSassCompiler` and takes the first
compiler whose `available()` says yes, so libsass stays as the fallback for as long as
it is still here. The processes are pooled - starting one is expensive and one process
compiles one sheet at a time - and `DomApplication.internalDestroy()` now closes the
pool through `SassCompilerFactory.terminate()`. By default the dart-sass binaries
bundled in `sass-embedded-bundled` are used, which covers linux x64/arm/arm64/riscv64
(glibc and musl), macos x64/arm64 and windows x64; that is 47MB of jar, and the
developer option `domui.sass.executable` points at a dart-sass installed on the
machine instead. It also ends the reason `binary-dependencies/jsass` exists: a
hand-patched jsass jar for ARM.

Verified by running the demo: `$THEME/default/style.scss` and `$THEME/dark/style.scss`
both compile through the resolver (208KB and 212KB of css, ~2.5s cold including
starting the process), the pages render styled in the browser, the unit tests pass,
and pointing `domui.sass.executable` at a path that does not exist falls back to
libsass as designed. The Selenium ITs were not run: they bind port 8088, which was in
use.

Dart Sass found two defects that libsass had swallowed: `_derived-variables.scss` 67-73
and `_datapager2.scss` 5 declare variables without the terminating `;`. Both are fixed.
The css it emits differs from libsass only in normalisations - colours as
`hsl(0, 0%, 100%)` rather than `white`, `[type=checkbox]` unquoted,
`[disabled]:hover` written as `:hover[disabled]`, and nine rules split where
declarations follow a nested rule, which is the correct cascade order. 1353 rules
against 1344.

What is *not* done is the language: the sheets are still libsass-era, and the four
deprecations that follow from that are silenced rather than fixed. That is the plan
item in phase 4, and it is deliberately separate - migrating to `@use` would break the
libsass fallback the same day it was put in place.

### 2026-09-09 - A themed image with a fixed-colour glyph is a dark-variant bug

The bug report's expand marker was `xdt-collapsed.png`: 16x22, transparent, a grey
`#808080` box and a black `#000000` plus inside it. In the light variant that reads as a
plus in a box. In the dark variant the box still shows against the dark ground and the
plus does not, so the marker becomes an empty square and the line stops looking as if it
opens.

This is the general problem with a themed *image* whose glyph is a fixed colour: the
variant mechanism can only substitute a whole file, so a variant either ships a second
copy of every such image or lives with the ones that vanish. A font icon has no such
problem - it is text, it takes `color`, and it follows whatever the variant sets. So the
rule for the framework's own small UI glyphs is **a font icon, not a themed PNG**, and
that is what the bug report now uses. The remaining `xdt-*.png` callers - the tree
components - have the same defect and are not fixed here.

### 2026-09-09 - `$themes/scss/all` stays on the search path

`SassThemeFactory` ends every theme search path with `$themes/scss/all`, and no such
directory exists - `resources/themes/scss/` holds only `winter/`. It was on the
still-open list of `finished-plans/THEMES.md` as a path entry to drop.

It stays, at the user's direction. The entry is the shared tail of the search path: a
second style - a `summer` next to `winter` - would use many resources unchanged, and
`all` is where those belong rather than duplicated in both style directories. DomUI
ships one style and therefore ships no `all` directory; that the directory is absent is
what a one-style installation looks like, not a defect. `finished-plans/THEMES.md` §5 now says so where
it lists the path, instead of listing it as an open issue.

### 2026-09-09 - The source viewer's palette belongs in its own sheet

`SourcePage` was unreadable in the dark variant. Its colours live in the demo's
`css/_syntax.scss`, and every one of them was a light-theme value: `#222` text,
`#006121` comments, `#86002f` keywords, `#000fd4` links, and a `#ccc` band behind the
line numbers - dark ink and a light band, both on the dark ground the variant paints.

The demo's other hardcoded colours are repainted in `_darkstyle.scss`, which is
imported last so its rules win. That was not the right shape here: in `_syntax.scss`
*every* rule is a colour rule, so repainting it would have meant a second copy of the
whole file. The palette became a set of variables at the top of the sheet instead -
the light values as plain assignments, and an `@if $themeVariant == "dark"` block
overriding them - so both palettes sit next to the rules they colour and neither can
drift from the other. The dark values keep the hue of their light counterpart and move
the lightness across the ground.

The overrides need `!global`: libsass scopes a variable assigned inside a control
directive to that directive, so the first attempt - the two palettes as the two arms of
an `@if`/`@else` - compiled to `Undefined variable: "$s-text"`. It failed loudly, at
build time, which is the good case.

Verified by running the demo (`mvn21 jetty:run -pl to.etc.domui.demo`) and loading
`SourcePage` in both variants: the sheet compiles in each, and the dark one renders
keywords, types, strings, comments and numbers legibly on the dark ground. The line
numbers got an explicit colour in both variants rather than inheriting one, and the
`.s-diff*` washes of the patch renderer were given dark grounds by the same rule.

### 2026-09-09 - Batik was not the problem, and the rasteriser was not needed

The question asked was whether Apache Batik is still the best way to rasterise SVG.
Three answers came out of it, and the third is what was done.

**The exclusion that broke it was stale.** `PartUtil.loadSvg()` threw
`NoClassDefFoundError: org/w3c/dom/svg/SVGDocument` because the build excluded
`batik-ext` "because it duplicates the org.w3c.dom package". It does not, and has
not for years: `batik-ext-1.17.jar` holds seven classes, all under
`org.apache.batik.w3c.dom`. The missing interfaces live in `xml-apis-ext`, which
was excluded next to it and which splits no JDK package - `java.xml` on 21 exports
`org.w3c.dom{,.bootstrap,.events,.ls,.ranges,.traversal,.views}`, and neither
`.svg` nor `.smil` nor `org.w3c.css.sac`. Dropping the two `*-ext` exclusions makes
the transcoder work on Java 21, on 1.17 and on the current 1.19. Only the
`xml-apis` (non-`ext`) exclusion was ever load-bearing.

**Batik is still the only complete pure-Java rasteriser, and JSVG is not a drop-in.**
Batik 1.19 (2025-05-06) is current and maintained; the costs are 20 jars, 4.3 MB, and
a run of SSRF/RCE advisories driven by it dereferencing URIs found inside the SVG.
The modern alternative, JSVG 2.1.0, is one 784 KB jar with no dependencies and no
scripting - but measured against the demo's three SVGs it rendered two of them
blank, because it does not implement `<switch>`, which is what Illustrator wraps
every export in. Stripping the `<switch>` makes it render. That rules it out for
arbitrary application icons.

**But the rasteriser had no reason to exist.** The only path that reached it in
anger was `GrayscalerPart`, which made a grey copy of a disabled image on the
server - written in 2012, when browsers could not do it. The framework had already
moved: `_button_common.scss` and `_linkButton.scss` grey a disabled button's `img`
with `filter: grayscale()`, and the `img.setDisabled(isDisabled())` calls in
`DefaultButton` and `SmallImgButton` are commented out. `MarkerImagePart`, the other
caller, is fed `THEME/` icons and rejects anything else, and the theme ships no
`.svg` at all. So the branch was dead in the shipped configuration, and wrong where
it was not: it rasterised at the size the SVG declares, which for the demo's own
`checkmark.svg` is 1707x1260 for a 16-pixel icon.

The decision, at the user's direction: remove SVG rasterisation and the part that
depended on it, rather than repair a dependency to keep a feature CSS does better.
This supersedes the 2026-09-07 note that `GrayscalerPart` and `MarkerImagePart`
would keep SVG support. `MarkerImagePart` stays - it draws a caption over a raster
theme icon, which is not a thing CSS does, and `Input.setMarkerImage()` and
`Text2.setMarker*()` are live API. `DomUtil.calculateImageURL()` lost its `disabled`
parameter rather than keeping a boolean that silently does nothing: a compile error
tells an application that greying is the theme's job now.

### 2026-09-08 - Two tiers, because one indirection is the whole point

The rule that a component may not read a main-set value directly looks like
ceremony - `$tlf-hdr-bg: $primary` then `background-color: $tlf-hdr-bg` is two
lines where one would do. It earns itself the moment anyone wants the log tailer's
header to stop following the accent: with the indirection that is one variable in
`_derived-variables.scss`, without it it is an edit to a component, and an
application that has to patch a component's stylesheet is an application that
cannot upgrade.

It also gives the variant mechanism somewhere to stand. `winter/dark` sets main-set
values and gets the whole theme; it can also set a single component variable when
one component needs special handling, which is what `$pmnu-bg` already does. Both
work because there is a layer between "what the theme's colours are" and "what
this component paints".

The convention was surveyed before being written: kebab-case led 223 to 69, and
component prefixes taken from the CSS class were already the habit in five
component families. Writing down what the code mostly already does costs an
argument nobody needs to have; inventing a new scheme would have made 223
variables wrong overnight.

### 2026-09-08 - Hue is not semantics

The colour scan grouped by hue, and the plan inherited that grouping: "error red,
warning amber, info blue, success green", each with a use count. Working through
them, only the reds survived contact. The "warning ambers" were a breadcrumb link
colour and the digit colour on the bug indicator's badge; the "info blues" were a
breadcrumb's selected-item background and a toggle's branding parameter; the
"success greens" were a pop-in panel's ground and a menu title's tint. None of
them is a state. They were grouped together because a machine measured their hue.

The check that actually works is the selector, not the colour - though it is not
sufficient either: grepping for selectors *named* for errors found two of the
seven, because the rest hide behind names like `.ui-ioe-asy` and
`button.ui-esic-del`. Both passes were needed, and both had to be read by hand.

Two literals that a hue-driven pass would certainly have converted, and which must
stay exactly as they are: the colour picker's three `#f00`, which are its sample
swatches - it is *showing the user the colour red*, and theming them would make
the widget lie - and `red($main_color)` in `_draganddrop`, which is not a colour
at all but the SCSS channel function, caught by the scanner's keyword regex.

### 2026-09-08 - Ladders are sequences, and a colour scan cannot see that

The colour scan ranked its findings by perceptual distance, and by that measure the
popup menu's disabled text was the biggest single shift in the theme. Seen on
screen the ranking was almost meaningless: the menu is dark, so "lighter" there
means *more* contrast, and the change the scan flagged hardest is an improvement.
Distance from a colour says nothing about what the colour is doing.

The real finding only appeared once the component was drawn: three of its greys
are one *sequence*, not three independent choices. `#666` and `#888` are a rung
apart, and both round to `$grey` - so the mechanical collapse would have silently
merged two nesting levels. Any colour consolidation that works one literal at a
time will do this wherever a component encodes depth, order or magnitude in
colour, and it will look like a tidy-up in the diff.

Hence `ladder()` rather than a lookup table: it makes the sequence explicit in the
source, so the next person to touch it can see that the levels move together. The
direction variable falls out of the same idea - a ladder is defined by where it
starts and which way it goes, and a variant that inverts the ramp needs to invert
the second thing too or every ladder in the theme runs backwards.

The general rule this sets for the rest of the collapse: before mapping a
component's greys, check whether they form a run. If they do, they take
consecutive rungs from one base, not nearest-neighbour matches individually.

### 2026-09-07 - Variables in the partials, not rules in the variant

The dark variant shipped with two files: a colour file and a rules file that
repainted the colours the partials wrote literally. The user's question - could
the second file go away by replacing those literals - was the right one, and the
answer is yes, with one condition worth recording: **a variant can only override a
colour that some rule asks a variable for.** Where the theme wrote `background:
white` there was nothing to override; worse, where the theme wrote *nothing at
all* - `body` had no text colour, text inputs had no background, both leaning on
the browser default - there was not even a rule to correct. Those two had to be
added, not substituted.

The discipline that follows: when a screen shows a light patch under a variant,
the fix is a variable in the partial, never a rule in the variant. That is why the
`variantstyle` hook was removed again rather than left as an escape hatch - an
escape hatch would have been used.

Where partials used *different* values for the same idea - four near-identical
border greys, #8c8c8c, #aaa, #BBB and #aaaaaa - each first kept its own variable
rather than being collapsed into one, so that the claim "the light theme is
unchanged" stayed literally true and checkable. The check that made it safe was
mechanical: compile the light sheet before and after and census every colour value
in it. The collapse then happened as its own step the next day, at the user's
direction, where its three-line effect on the light theme could be stated on its
own rather than hidden inside a hundred other edits. Separating the two was worth
the extra round: one change was provably inert, the other is a deliberate design
change, and mixing them would have made neither claim verifiable.

### 2026-09-07 - A dark theme by override, and the file the theme was missing

The brief was a dark theme "not by copying all files but by switching some colour
parameters file". The colour file gets most of the way, and for a theme written
against variables it would have been the whole answer: `_color.scss` is imported
before `_variables.scss`, everything there is `!default`, so overriding the
greyscale ramp inverts the entire derived palette in one file.

It was not the whole answer here, and the measurement is the reason to record
this: winter has 231 hardcoded colour literals across 48 partials and 84 bare
`white` keywords, against roughly 60 uses of a colour variable. No variable file
can reach those. The choice was between quietly shipping a half-dark theme and
adding a second override file, and the second file won - but it needed somewhere
to go, because `_userstyle.scss` is imported among the *variables*, far too early
for a rule to win, and a variant's copy of it would have shadowed an
application's own.

Hence `@import "variantstyle"` as the last line of `style.scss`, with an empty
base copy. It costs the theme one line, it is the rules counterpart of the
`_custominit`/`_userstyle` variable hooks that already existed, and it is what any
application variant will want as well. The entries in
`dark/_variantstyle.scss` are a list of the partials that still hardcode colours;
each one deleted from that file is a partial that has been fixed properly.

The switch reloads the page rather than doing an ajax delta, because the
stylesheet `<link>` is only written by the full renderer. `WebUI.refreshPage()`
rather than `UIGoto.reload()`, so the conversation - and whatever the user had
typed - survives the switch.


### 2026-09-07 - One theme, and the variant as the only axis

Three axes of variation existed in the theme name - style, icon set, colour set -
plus a factory prefix and a variant, and none of them were used: every DomUI
application ever written says `scss-winter-default-default`. The user's call was
to collapse all of it to one theme chosen at initialization plus a variant chosen
per session, on the grounds that dark/light is the only variation anyone actually
wants and it is exactly what the old machinery could not do.

The variant is deliberately *just a name*. It is a directory in front of the theme
search path and a string in the URL, and that is the whole mechanism: no registry
of known variants, no validation beyond "usable in a URL segment". An unknown
variant name therefore renders the base theme rather than failing, which is the
right failure mode for something that can arrive from a stale session.

Two things had to come from the URL rather than the session, and getting this
wrong is subtle: `SassPartFactory`'s cache key and the `$themeVariant` scss
variable. The session decides which URL a page *emits*; once a request for a
themed resource arrives, the URL is what says which variant it is. Reading the
session there would give two sessions different cache entries for identical
content, and would have put the wrong `$themeVariant` in the sheet - which is how
the bug was found, by a probe file that printed the variable back out.


### 2026-09-07 - One theme engine, and what "library, not application" does not protect

The three theme engines were not three choices; two of them were unreachable. The
rule that public API is not removed just because nothing in this workspace calls
it (see the `TableFormLayouter` entry) did not save them, and the difference is
worth writing down: `TableFormLayouter` *worked* - an application outside this
workspace could call it and get a form. `SimpleThemeFactory` and
`FragmentedThemeFactory` could still be selected by name, but everything reached
through them ran into `SassTheme.getPropertyScope()`, which throws. Code that
cannot execute is not API. The test applied was reachability, not usage.

`translateResourceName()` was kept for the opposite reason: it is a no-op in
`SassTheme`, but an `ITheme` written outside this workspace can still implement
it and have it called. It is a working extension point with no current user, which
is exactly the case the rule protects.

`GrayscalerPart` and `MarkerImagePart` kept SVG support because the user asked for
it explicitly. Removing the theme expansion from it exposed that the Batik path
throws `NoClassDefFoundError` on `org.w3c.dom.svg.SVGDocument` - `batik-ext` has
been excluded from the build since 2019 because it duplicates the `org.w3c.dom`
package. That is pre-existing: the old code threw in the template expander before
it ever reached Batik. It is now an item in the plan rather than a silent
dependency change, because re-adding `batik-ext` is a build-wide decision.


### 2026-09-07 - The external links, and the generator's runnable jar

**The links.** Eighteen external URLs, all answering 200: the five github.com
repositories (`domui`, `domui-skeleton`, `domui.github.io`,
`domui-intellij-plugin` and `bonigarcia/webdrivermanager`), both
`help.eclipse.org` topics (checked by their titles, "Using null annotations" and
"Using the batch compiler", because that host answers 200 for anything), gnu.org,
plotly twice, maven.apache.org, api.jquery.com, ace.c9.io, flywaydb.org,
www.domui.org and the three on demo.domui.org. The rest of the `http` strings on
the site are deliberate illustrations - `localhost:8088`, `localhost:8082`,
`example.com`, `somehost:4444`. Two redirect: `flywaydb.org` to Red Gate's
Flyway product page and `www.domui.org` to `domui.org`. Both were left as they
are: the first is still Flyway's own domain and the second is inside a code
sample about `ALink`, not a link a reader follows.

The item expected the risk to be in the foreign links. It was not: **the one
wrong URL on the site was a DomUI one.** `building-pages/10-first-page` explained
`@UIPage` with

    @UIPage("/welcome/hello")
    public class HelloPage extends UrlPage

and said that this puts the page at `https://demo.domui.org/welcome/hello`. The
demo's `HelloPage` carries no such annotation - no demo page uses `@UIPage` at
all - and that URL does not 404: DomUI falls through to `getRootPage()`, so the
reader lands on the demo's `HomePage` and has no way to tell the documentation
was wrong. Checked in the browser against the live demo, which is also how the
class-name URL one section earlier was confirmed to be right. The example is now
a `WelcomePage` and puts it "at `/welcome/hello` under the application's root
URL", claiming nothing about the demo.

That `@UIPage` has no live demonstration is worth fixing at some point - the
annotation is documented but nothing on demo.domui.org shows it. It needs a demo
page and a deploy, so it is not done here.

**The runnable jar.** `utilities/hibernate-generator` now runs
`maven-shade-plugin` in `package`, producing `target/hibernate-generator.jar`
with the dependencies inside and `to.etc.domui.hibgen.HibernateGenerator` as its
`Main-Class`. The plugin's `finalName` is set, which means the module's own
artifact (`domui-hibernate-generator-1.2-SNAPSHOT.jar`, 84KB) is left exactly as
it was and the fat jar (17MB) sits next to it - nothing that depends on the
module sees a change, and nothing does today anyway. For the same reason the
plugin's `dependency-reduced-pom.xml` is switched off - it would describe an
artifact that is not being replaced, and it lands in the source directory. Signature files and
`module-info.class` are filtered out and `META-INF/services` entries merged, as
a shade of jdbc drivers needs.

The root pom's unused `maven-shade-plugin.version` property was 2.4.3 (2016,
ASM too old for Java 21 class files) and is now 3.6.0. Nothing else in the build
uses shade.

Verified: `mvn21 install` green, and `java -jar
utilities/hibernate-generator/target/hibernate-generator.jar` with no arguments
prints the args4j option list, which is what the page says it does.
`data/pojo-generator` documents `mvn package` plus `java -jar` instead of the
`mvn exec:java` workaround, and notes that a full `mvn install` makes the same
jar.

### 2026-09-07 - Phase 4, first batch, and what it got wrong about a library

Three phase 4 items looked like code nothing called any more. Two of them were;
the third was not, and the correction is the important part of this entry.

**DomUI is a library. "Nothing in this tree calls it" is not evidence that
nothing calls it.** `TableFormLayouter` was removed on the strength of exactly
that reasoning - the `FormBuilder` constructor makes a `ResponsiveFormLayouter`
and the line that would make a table layouter is commented out next to it,
nothing in the framework, the demo or the site names the class - and it was
wrong: the class is usable and used, through the public
`FormBuilder(IFormLayouter)` constructor, by application code this workspace
cannot see. It and `_form4.scss` were restored the same day, along with the
`ui-f4-mandatory`, `ui-f4-hinticon` and `ui-f4-ta` names on the classes
`FormBuilder` emits, which client stylesheets can target just as legitimately.

The rule that follows from it, for the rest of phase 4: **a public type, method
or css class may only be removed on evidence that it is unusable or wrong, not
on the absence of a caller in this workspace.** A commented-out construction and
an empty grep say what *we* do not use; they say nothing about what a client
does. Application-facing surface - a public class a public constructor can be
handed, a css class the framework emits into a client's pages, a part addressed
by URL - is out of scope for "delete what nothing calls". What is in scope is
what no caller can reach at all: private and package-private dead ends, code
whose only entry point was already deleted, a value that is overwritten before
anything can read it. The two removals that stand below are of that kind, and
the third was not.

**The button part** (removed; confirmed by the user after the correction, on the
grounds that the thing itself is obsolete). `PropBtnPart` painted a button image
from a properties file: a label, an icon and a `defaultbutton.properties`
describing the borders, drawn into a png by `PropButtonRenderer` and cached by
`ButtonPartKey`. Buttons have been css for years. The URL form a client
stylesheet could still name it by is exactly the kind of invisible use the rule
above is about, which is why this one was put to the user rather than decided
here. `ThemeCssUtils.buttonURL()`, the only code that built such a URL, went with
it; `ThemeCssUtils` keeps `color()`, `url()` and `hsl()`, and its constructor
parameter went with the scope it no longer needs.

**The dead default theme name** (removed). `DomApplication` called
`setDefaultThemeName("blue/domui/blue")` and then
`setDefaultThemeFactory(SassThemeFactory.INSTANCE)`, which assigns
`themer.getDefaultThemeName()` to the same field. Both statements are in
DomUI's own constructor and the second overwrites the first, so no client can
observe that the first ever ran - it is dead in the strong sense the rule asks
for. The field's initialiser (`""`) is untouched.

**What the excursion did leave behind.** `ui-f4-row` is how the Selenium layout
tests find the row a control sits in, and the demo's own form pages have not
been built by the table layouter since the responsive one became the default -
so `getParentTR(comp, "ui-f4-row")` finds nothing on them. That did not fail,
because the guard is `Assert.assertNotNull("The form's parent row cannot be
located ...")`, which asserts that the *message* is not null and therefore always
passes, and then returns. The helper is now `getParentPair()`: it takes the
enclosing `div.ui-f5-pair` **or** `tr.ui-f4-row`, whichever the form was built
with, stops at `body`, and its two callers fail properly when there is none.

It is compile-checked only. Every test that would exercise it is
`@Ignore("While redesigning")` - all six of `ITTestForm4Layout`, both of
`ITTestText2Layout` and the three `labelMustBeAligned*` of
`ITTestLookupInput2Layout`; `mvn21 verify` on the three classes is green with 14
of 16 skipped, and the two that do run touch neither helper. That is why an
assert which cannot fail sat there unnoticed. What happens to that suite is a
new plan item.

### 2026-09-06 - The styling chapter, and the last unwritten code on the site

Measuring what was left of the raw Confluence conversion by `git blame` against
the pre-project commits rather than by file date: every page had been *touched*
in 2026, but five still carried code fences nobody had written -
`testing/70-mockito-pitfalls` (41 of 41 fenced lines), `ecj-in-maven` (26 of 64),
`components/rules` (23 of 23), `sass-scss-support` (7 of 9) and `animations`
(4 of 5). That is the whole of the "verify every code sample" item: everything
else on the site was written by this project, out of the demo application.

Two of the five needed nothing:

- **`ecj-in-maven` was already correct.** Its XML matches the root pom exactly -
  `maven-compiler-plugin` 3.14.0, `source`/`target` 21 via `jdk.version`,
  `plexus-compiler-eclipse` 2.8.5, `ecj` 3.36.0, and `1.2-SNAPSHOT` really is the
  project version. The blame count was a false positive: unchanged boilerplate
  lines such as `<groupId>org.apache.maven.plugins</groupId>` are still credited
  to the old commit even though the versions around them were rewritten. Blame
  counts flag pages worth looking at; they do not decide anything.
- **`mockito-pitfalls` was deleted** (user's decision): the framework no longer
  uses it and it is not interesting in this context. It was one of the two
  moves - relocated verbatim into the testing chapter, first-person rant and all,
  without a word being checked. Its entry in `testing/index.md` went with it.

The remaining three were symptoms of the real gap: **the site documented the
stylesheet pipeline and the component naming rules, and nothing in between.**
There was no page saying what a theme is, and no page saying how an application
gives itself its own colours - the single most common thing anyone would want.
`look-and-feel/` was split into a full chapter (user's choice among four
structures):

| Page | What it covers |
| --- | --- |
| `themes/` (new) | what a theme is; the `factory-style-icon-color-variant` name and the `scss-winter-default-default` default; the search path; the four-step resource lookup; a plantuml sequence of how `style.scss` reaches the browser; `ThemeManager` caching |
| `overriding-the-theme/` (new) | `_custominit.scss` and `_userstyle.scss`, why `!default` makes them work, which to use when, and the variables worth setting |
| `the-winter-theme/` (new) | the import order of `style.scss` as the theme's architecture; the `bulmaish` base layer; reset and the border-box model; one partial per component |
| `styling-your-component/` (new) | writing a partial, wiring it in through `_userstyle.scss`, the `ui-` base name, class-only selectors, never borrowing another component's classes |
| `sass-scss-support/` | unchanged; renumbered to 50 |

What was verified in the source while writing them:

- `DomApplication` sets `setDefaultThemeFactory(SassThemeFactory.INSTANCE)`,
  whose default theme name is `scss-winter-default-default`. The
  `setDefaultThemeName("blue/domui/blue")` on the line immediately before it is
  overwritten by that call and is dead.
- `SassThemeFactory` builds the search path from the style, icon and colour
  parts; with the default name only `$themes/scss/winter` and
  `$themes/scss/all` remain, which is why "the theme" and "winter" are the same
  thing in practice. No `-color` or `-icons` directory ships.
- The `$` prefix is stripped by `SimpleResourceFactory`, which calls
  `DomApplication.getAppFileOrResource()`. That looks in the webapp directory
  **first** and the classpath `/resources/` last, so an application file shadows
  the framework's. This is what makes the whole override mechanism work, and it
  was documented nowhere.
- Every variable in `_color.scss` and `_variables.scss` is declared `!default`,
  and `style.scss` imports `_custominit` *before* them and `_userstyle` *after*.
  The two files carry comments saying they are meant for exactly this and must
  stay empty in DomUI itself.
- `SassTheme.getStyleSheetName()` emits `$THEME/<theme>/style.scss?$hash=<hash>`
  with a hash of the compiled result, so the URL changes when the stylesheet
  does.
- `_form5.scss` (`ui-f5-*`, flexbox) is the current form layout and `_form4.scss`
  (`ui-f4-*`, table) belongs to the dead `TableFormLayouter`, which matches the
  open plan item about removing it.

`components/rules` was rewritten to match the split: the stylesheet half moved to
`styling-your-component`, and what stays is what a component may *do* - the two
shapes, one containing node for an inline component, no padding of its own,
behaving inside a form. That also removed the page's Confluence residue: six
untagged code fences, an `[IMAGE HERE]` placeholder, a `</button` missing its
`>`, an IE-8 aside, and the external `blog.teamtreehouse.com` link (which went
with the box-model text, now on `the-winter-theme` without it).

Site builds clean: 156 pages, the plantuml diagram rendered to SVG, no dangling
links.

### 2026-09-06 - `to.etc.db`, the connection pool, is documented

`common/to.etc.db` had no page anywhere on the site: the only mention of it was
one paragraph in `getting-started/example-skeleton` saying "DomUI has its own
pool" and how to configure it from `app.properties`. It is a substantial module
and the thing every database-backed DomUI application actually runs on, so it now
has a page of its own, `data/connection-pool`, at sort 20 in
"Databases and queries" - before the POJO generator, since the pool is what the
generated classes end up talking through. The skeleton paragraph links into it
rather than repeating anything.

Written from the source, not from the class javadoc, which is stale in places:

- The define/initialize split, the `.dbpool.properties` and `.dbpool.xml` forms
  with the `<poolid>.<key>` naming and the `.local` override file, and a
  parameter table with the real defaults out of `PoolConfigBuilder` (minconn 5,
  maxconn 20, `scan` enabled, everything else off) including the `p.`/`extra.`/
  `p-` pass-through to the driver.
- Pooled versus unpooled, described as what it is - both come from the same set;
  the difference is that unpooled connections are not counted against `maxconn`
  and the janitor leaves them alone - with the naming called out as bad, because
  it is.
- What the proxies add: resources closed with the connection, `BetterSQLException`
  carrying the SQL and its bound parameters, and the timing that the statistics
  are built from.
- The hanging-connection janitor as an activity diagram, with the actual rule
  from `ConnectionProxy.calcLongRunState()` and `checkHangState()`: unused for
  two minutes, or allocated more than two minutes plus the five minute grace
  period ago; destroyed in `enabled` mode, only reported in `warn`; and
  `disabled` overridden in forced mode when the pool runs dry.
- `pool.jsp`, statistics with `StatisticsRequestListener`, and `DbReplay`.

**Verified.** `checksql` is optional after all - `ConnectionPool.checkParameters()`
supplies `select 1 from dual` for Oracle and `select 1` for PostgreSQL and MySQL,
and only throws when the type is unknown; the table says so. The live demo serves
`pool.jsp`, so the page embeds it with `!demo(pool.jsp, 100%, 620)` - checked in a
browser, the pool overview renders inside the iframe. Site builds clean (153
pages, no link or link-check errors) and the plantuml diagram renders.

**Deliberately not documented:** the "connection used for more than 8 seconds
generates a warning" that `ConnectionPool`'s class javadoc describes.
`getConnectionUsedTooLongWarningTimeout()` has no callers - the behaviour is gone
and only the comment survives. A warning about it is also added nowhere; the page
simply describes what the janitor really does.

### 2026-09-06 - The triage worked off: six pages gone, two moved, eleven rewritten

The verdicts of the inventory above, carried out the same day. The site went from
158 pages to 152.

**Deleted (6).** `99-todo/spi-pages-and-logins` went whole, not half: its SPI
half documents machinery that is not in the framework, and its login half - the
one section worth keeping - turned out to be documented better already in
`getting-started/example-skeleton`, which has the round trip as a sequence
diagram, the `ILoginDialogFactory` the skeleton registers, `UILogin.login()`, the
brute-force limit and impersonation. Salvaging it would have created a second
description of one subject, which is the thing this project is removing. With it
went `99-todo` itself, `development-environment/github-environment` (TravisCI and
DeployHQ), `development-environment/intellij-tips-and-tricks` (one tip, for a
path IntelliJ no longer uses), `look-and-feel/css-problems-and-solutions`
(jsfiddle links and IE Edge notes) and `faqs-and-issues` (one bullet, no FAQ).

**Moved (2)**, both into `70-implementation-details` and both rewritten on the
way, because their subjects are live framework code that was sitting in a section
called "todo":

- `subpages`. Every claim held up, with three corrections. The exception is
  `SubPageInjectorException`, not `SubFieldInjectionException`; the annotation is
  `@UIReinject(false)`, not `@ReInject(false)`; and the enforcement is sharper
  than the page said - a field whose *type* is a persistent class and which is
  not annotated throws `ProgrammerErrorException` in **every** mode, at the
  moment the SubPage is first added, while the development-mode checking injector
  is for fields whose type says nothing (`Object`, a type parameter) and which
  turn out at runtime to hold an entity. Also written down: an annotated field may
  not be `final`, and a removed SubPage's conversation is destroyed at the end of
  the request, not at removal, so a SubPage can be moved within one request
  without losing its state.
- `url-contexts`. "Since 2.0" gone, and the example - which did not compile -
  rewritten. Two things the page never said are now in it: `@UIUrlContext` goes
  on a **setter**, and the injector matches the decoder's values to setters **by
  type**, not by the names in the map; and a missing context or a missing value
  throws `UrlContextUnknownException` unless `optional = true`. How the string is
  split off is now described from `RequestContextImpl`: last dot, last slash, and
  a context that always ends in a slash.

**Rewritten (11).** The four with wrong facts were rewritten from the source:
`ecj-in-maven` from the root pom (maven-compiler-plugin 3.14.0,
plexus-compiler-eclipse 2.8.5, ecj 3.36.0, jdk 21, `<compilerArgs>`, and what the
four arguments and the `.settings/org.eclipse.jdt.core.prefs` file do);
`sass-scss-support` with the Vaadin fallback removed and the *application*-level
variables added, which it never mentioned - `setThemeProperty()` and
`IThemeVariablesCalculator` feed `_parameters.scss` alongside the URL parameters;
`animations` with the real method list and `slideUpAndRemove()`; and
`pojo-generator` with its option table regenerated from the `@Option`
annotations. `what-is-domui` lost the 2011 announcement (the mailing lists stay -
the user confirms they are alive), `about` gained the truth about the build and
the deploy script, `coding-rules` lost its "TBD" opener, and `urlpage` grew from
sixteen lines into a real page: the `title` versus `pageTitle` trap, the
lifecycle hooks, and the page's shared `QDataContext`.

**`header-contributors` was written, not deleted.** The triage had it down for
deletion because its body was the word "tbd", but two current component pages -
`fonticon` and `icons` - point at it as *the* explanation of how a font's
stylesheet gets onto the page. Deleting it would have broken them and removed a
genuinely needed reference, so it is now a page: the two places a contributor can
be added and how the two lists are merged and ordered, the factory methods, how a
resource name is resolved (`THEME/`, `$`, webapp-relative, `http`), why
`equals()` is not optional, and the fontawesome initializer as the worked
example.

**Two findings that are not documentation problems:**

- The hibernate generator cannot be run the way anyone would try. Its module
  builds a plain jar - no `Main-Class`, no dependencies - so `java -jar` fails
  however the page words it. The page now documents `mvn exec:java`, which was
  verified to work (it prints the option list), and the missing manifest is an
  open item in the plan.
- `.travis.yml` was still in the framework repository, naming openjdk11 and a
  `scripts/` directory that does not exist. Removed. The workspace `CLAUDE.md`
  and `deploy-demo`'s own usage line both said `scripts/deploy-demo`; both now
  say what is true, `domui/deploy-demo`.

### 2026-09-06 - The documentation inventory, and what the source says about it

Phase 1's first item: a page-by-page verdict on `site/content`. Of its 158 leaf
pages, 126 are in `components/`, `building-pages/` and `testing/` - all written by
this project against the current source, so the inventory is only about the other
**32**. Each of those was read, and every claim that could be checked was checked
against the framework rather than judged by its prose. The verdicts - 12 current,
11 needing update, 7 to delete, 2 to move - are the table in phase 2 of
`IMPROVEMENT-PLAN.md`; what follows is what the checking turned up, because that
is the part that would otherwise have to be found again.

**The SPI documentation describes something that does not exist.**
`99-todo/spi-pages-and-logins` explains SPI pages at length: an `SpiPage` hosting
named `SpiContainer`s, fragment identifiers after the `#`, a javascript call that
posts `window.location.hash` back to `PageRequestHandler.loadSpiFragments`, and a
redirect that appends `location.hash` to the new URL. None of it is in the tree.
There is no class whose name contains `Spi` (the only match in the framework is
`Icon.faSpinner`), no `loadSpiFragment*` method, and no `location.hash` anywhere
in the framework's java or TypeScript sources. The javascript redirect it builds
its story on is real - `ApplicationRequestHandler` line 100 emits a
`location.replace(...)` document rather than an HTTP redirect - but it does not
append the hash, so even the mechanism the page explains is not the mechanism the
code has. What *is* real is the first section, the login round trip:
`NotLoggedInException` carries the original URL, `ILoginDialogFactory` turns it
into the login page's URL with a `target=` parameter. That section is worth
keeping; the rest goes.

**The other things the source contradicted:**

- `look-and-feel/sass-scss-support` says DomUI has two SCSS compilers and falls
  back from jsass to the Vaadin one. `SassCompilerFactory`'s static initialiser
  registers `JSassCompiler` and nothing else; the Vaadin compiler is gone, and
  all that is left of it is an unused `vaadin.sass.compiler.version` property in
  the root pom.
- `look-and-feel/animations` teaches `Animations.slideUpAndDestroy()`. The method
  is `slideUpAndRemove()`. The class is `to.etc.domui.dom.Animations`, which the
  page never says, and it is used by `Tree2`, `Tree3` and `SearchPanel`.
- `data/pojo-generator` names the jar `hibernate-generator.jar` (it is
  `domui-hibernate-generator-1.2-SNAPSHOT.jar`) and documents `-pkg` where the
  tool's `@Option` declares `-pkgroot`, required. Four options it never mentions
  exist.
- `development-environment/ecj-in-maven` documents `plexus-compiler-eclipse`
  2.8.3 with a section on the unreleased 2.8.4, ecj 3.13.101,
  `maven-compiler-plugin` 3.7.0 and `<compilerArguments>`. The root pom uses
  2.8.5, ecj 3.36.0, 3.14.0 and `<compilerArgs>`, on jdk 21.
- `development-environment/github-environment` credits TravisCI and DeployHQ. The
  build is `.github/workflows/build.yml` - JDK 21, sonarcloud, CodeQL - and
  `.travis.yml` is a leftover that still names openjdk11 and `scripts/demo-deploy`,
  a path that does not exist.
- `about` and the workspace `CLAUDE.md` both say the demo is deployed with
  `scripts/deploy-demo`. There is no `scripts/` directory in the framework repo;
  the script is `deploy-demo`, at the root.
- The two pages worth moving out of `99-todo` check out: `SubPage`,
  `SubPageInjector` and `@UIReinject` exist for `subpages`, and
  `IUrlContextDecoder`, `@UIUrlContext` and `getUrlContextString()` exist for
  `url-contexts`. The latter's example does not compile (`Map<String, Object)`,
  a `return` in the wrong block), which is a good sign nobody has used the page.

**What the triage did not settle**, because it cannot be settled from the source:
whether the three `googlegroups.com` mailing lists on `introduction/what-is-domui`
are still alive, and whether the IntelliJ plugin of
`getting-started/intellij-plugin` still installs into a current IntelliJ. Both
need someone to try them; both pages are marked needing update regardless, the
second for its six screenshots from May 2018.

No page was changed by this pass.

### 2026-09-06 - The form builder has a group of its own

The last of the three dissolved `components/` subdirectories was
`forms-and-input`, down to one page: `form4-formbuilder`. That page was still
the raw Confluence conversion - it opened by saying that "DomUI contains many
FormBuilders, most of them are attempts to get it right", its first section was
the word **TBD**, and what it did document was the readOnly and disabled
bindings and nothing else. Next to it, in `components/rules`, sat
`vertical-form-builder-details`: nine hundred words on how the baselines of a
label and an input are aligned inside the `<td>`s of a `ui-f4` table.

**The form builder is a group, `components/15-forms`, between text input and
choice input.** It is not a component, but every group page's examples are
written in terms of it, and the walkthrough's one-paragraph pass over it in
`building-pages/20-using-components` is a teaching pass, not a reference. The
group has an index and three pages, mirroring three new demo pages:

- `formbuilder` - the pair a form is made of: what the builder is handed, what a
  label can be, `mandatory()`, hints as tooltip or as icon, and `item()` for a
  row that is not a control.
- `from-a-property` - `property()`: the control made from metadata and bound to
  the property, `control()`/`control(Class)`/`control(control)`, and the
  `readOnly`/`disabled`/`disabledBecause` trio in its three forms (set, bound
  for one row, bound for a run of rows).
- `form-layout` - vertical and horizontal, `nl()`, `append()` and
  `appendAfterControl()`, the label width classes, `cssLabel()`/`cssControl()`,
  and the layouter that turns a row into html.

`vertical-form-builder-details` **is deleted rather than updated**: it describes
the `TableFormLayouter`, and that is not what a form is any more. The builder's
constructor makes a `ResponsiveFormLayouter`, which builds flexbox `div`s -
`ui-f5`, `ui-f5-pair`, `ui-f5-lbl`, `ui-f5-ctl` - and the page's table, its
`<td>` classes and its baseline arithmetic are all about a layout nothing
creates. Verified against the running demo: a form is
`div.ui-f5.ui-f5-v > div.ui-f5-pair-v > (div.ui-f5-lbl-v, div.ui-f5-ctl-v)`.
The `ui-f4` class names that *are* still emitted are the three the responsive
path uses - `ui-f4-mandatory` on a mandatory label, `ui-f4-hinticon` on the hint
icon and `ui-f4-ta` on a textarea's label - and the new pages name those.

Three demo pages were added under `pages/components/form`, in a new "Forms"
group of `ComponentListPage`: `FormBasicsPage`, `FormPropertyPage` and
`FormLayoutPage`. Everything the pages claim was checked in the browser against
them: the metadata labels ("Title" for `Track.name`), `control(ComboLookup2.class)`
turning the default `LookupInput2` for `Track.genre` into a combo, the
`readOnlyAll()` binding following a checkbox tick, `append()` putting a second
control in one pair, `ui-label-wide` widening the label column, and the hint
icon appearing only under `hintAsIcon(true)`.

**Two defects found while writing it, both fixed in `FormBuilder`:**

- `controlOnly()` leaked its own marker. The no-label marker is the string
  `$nor$`; `determineLabel()` knew it, but `labelTextCalculated()` did not, so a
  control added with `controlOnly()` got `$nor$` as its error location and as
  its calculated id. It returns null for the marker now.
- `disabledBecause("some text")` threw the text away. The literal branch was
  `//ctl.setDisabledBecause(diMsg); // FIXME` followed by `setDisabled(true)`,
  because `IControl` has no such method - the property exists only on the
  controls that have it. It is set through the control's metadata now, the same
  way the *bound* form of `disabledBecause()` already set it, and a control
  without the property is still just disabled. Verified in the browser: the
  `LookupInput2` for `Track.mediaType` carries "The media type is set when the
  track is imported" as its tooltip.

**What was left alone:** `unlabeled()` and `controlOnly()` do exactly the same
thing in both layouters (`unlabeled()` is `label("")`, and an empty label
produces no label node), so only `controlOnly()` is documented - and only
`controlOnly()` exists as an entry point on the builder itself, which is how the
demo page found the duplication.

### 2026-09-06 - IClicked and IValueChanged removed; IExecute is the handler

The click and change handlers were `IClicked<T extends NodeBase>` (`clicked(T
node)`), `IClicked2<T>` (`clicked(T node, ClickInfo)`) and `IValueChanged<T>`
(`onValueChanged(T component)`). Their type parameter never earned its keep: it
was declared as a wildcard in every field and setter that stored one, and all
but one of the lambdas written against them ignored the parameter, because the
node a handler is set on is a local variable of `createContent()` that the
lambda already captures.

The session before this one added `IExecute` overloads next to them. That made
it worse rather than better: two SAM types on one method name is exactly what
Kotlin cannot resolve, so `button.setClicked { ... }` became ambiguous and every
Kotlin call site had to be written `setClicked(IExecute { ... })`.
`ConditionPanel.kt` was rewritten that way and it read badly.

**So the old interfaces are gone**, and with them every method that took one:

- `IClicked`, `IClicked2`, `IClickBase` and `IValueChanged` are deleted.
- `NodeBase.setClicked(IExecute)` / `getClicked()` is the click handler, and
  `clearClicked()` removes it.
- `NodeBase.setClicked2(IClickedInfo)` / `getClicked2()` is the handler that
  wants the click's details; `IClickedInfo` is `clicked(ClickInfo)` - the new
  interface carries the `ClickInfo` and *not* the node. `hasClicked()` answers
  whether either one is set.
- `IHasChangeListener` is `IExecute getOnValueChanged()` /
  `setOnValueChanged(IExecute)` / `clearOnValueChanged()`, with
  `callOnValueChanged()` the way a control reports a change. `IHasChangeListener.DUMMY`
  replaces `IValueChanged.DUMMY` as the marker an "immediate" control returns.

Handlers that genuinely used the node now capture it: `MonthPanel` builds a
per-cell lambda instead of sharing one handler, `ListShuttle` calls a
`toggleSelected(TD)` method, `RowRenderer`, `ExporterButtons`, `DataTable`,
`ScrollableDataTable`, `DataPager1`, `MsgBox`, `MsgBox2`, `LookupInputBase`,
`LookupInputBase2`, `SearchAsYouTypeBase`, `EditableDropDownPicker` and
`LogTailerFragment` capture the control they are set on. `RadioButton.getClicked()`
returns the group's handler directly rather than wrapping it, and
`TreeSelectionWindow` no longer overrides `setClicked2()`.

`ConditionPanel.kt` is back to Kotlin's own lambda syntax
(`fieldC.setOnValueChanged { ... }`, `LinkButton("Delete", Icon.faMinus) { ... }`).
One call in it still needs `IExecute { ... }`: `MsgBox.yesNo(dad, text, ...)` has
both an `IExecute` and an `IAnswer` overload, and two SAM types on one name is
the same ambiguity as before - a candidate for a later rename, not something this
change touched.

The documentation followed: twelve pages listed `IClicked<...>` or
`IValueChanged<...>` in their API tables, and thirty-odd code samples wrote
handlers as `a -> ...`; they are `IExecute` and `() -> ...` now. The
`110-writing-a-component` page showed `StarRating` fetching and calling its
change listener by hand, which the class stopped doing long ago - it calls
`callOnValueChanged()`, and the page says so. The first-page tutorial's click
section had the worst of it: its sample still called `clickedNode` inside a
no-argument lambda, and the paragraph under it taught the old contract.

`setClicked2()` was never documented anywhere, so the click section of
`building-pages/10-first-page` now ends with it: what `ClickInfo` carries, that
it does not carry the node because the lambda captures it, that a node has one
click handler so the two setters replace each other, and a `!demo()` of the
`Click2HandlerPage` fixture to click on. It sits there rather than in
`components/40-buttons` because that is where clicking is first explained.

Verified with a full `mvn21 verify`: the framework, the Kotlin sources, the
integrations and the demo build, and the 9 unit tests and 55 Selenium tests pass.
The site builds clean at 157 pages.

### 2026-09-06 - The testing documentation, and the fixture pages that stay

The `testing/` section was the raw Confluence conversion: one page about
Selenium, one about the HtmlEditor's screenshot test, one listing two data
binding tests, one about Mockito, and 2017 screenshots. Nothing in it described
the page objects, the proxies or the generator - the things a DomUI test is
actually written with today.

**The decision it rests on**: the JUnit/Selenium fixture pages
(`to.etc.domui.demo/pages/test/**`) are **not** moved out of the demo
application. They have two jobs - putting a component in the situation a test
checks, and showing what a testable page looks like - and the second one only
works if they sit next to the tutorial and the component demos where people
read them. The plan's phase 3 item asking for the opposite is gone; extra
fixtures are added there like all the others.

**The section is now seven pages**, in the order someone learns it:

- `10-writing-a-ui-test` - a test that opens a page, types, clicks and asserts;
  `AbstractWebDriverTest`/`wd()`, `openScreen()`, the testid (who renders it,
  when, and the calculated ids that `FormBuilder`, `DefaultButton`,
  `LinkButton`, `SmallImgButton`, `RadioButton` and `DataTable` hand out), the
  `cmd()` command builder, the reading calls and the waiting calls. A sequence
  diagram of one test's round trip.
- `20-running-the-tests` - `IT*` (failsafe) versus `Test*` (surefire), the jetty
  the build starts in `pre-integration-test`, the three system properties
  failsafe passes (`webdriver.url`, `webdriver.hub`, `domui.testui`), the
  browser and its driver, `~/.test.properties` and how `-D` wins over it,
  running one test from the IDE, and the screenshot a failing test leaves in
  `target/failsafe-reports`.
- `30-page-objects` - the same two tests written against a page object, the
  proxy library table, `AbstractCpPage`, writing one by hand, tables, and
  putting the vocabulary of the tests in the page object.
- `40-generating-page-objects` - **the page object generator**, which nothing
  documented before: how to run it (Ctrl-Shift-` twice, development mode plus
  `.developer.properties`, and the fact that it generates from the tree *on the
  screen* - a table needs rows in it), what it writes for the order entry page,
  the generation model (`PoGeneratorContext` walking the tree,
  `PoGeneratorRegistry`, `IPoProxyGenerator` with its three-way
  `acceptChildren()` answer, the two passes, the four `IPoSelector`
  implementations, `PoClass`/`PoField`/`PoMethod`/`RefType` and
  `PoClassWriter`), what is recognized, and three ways to extend it: your code
  in the generated class's subclass, a proxy plus `PoGeneratorRegistry.register`
  for a component of your own, and an `IPoProxyGenerator` of your own when one
  proxy is not enough. Carries a screenshot of the generator's own result window.
- `50-test-pages-in-the-demo` - why the fixture pages are in the demo, the five
  groups they fall into with the tests that drive them, the order entry page
  itself, and the five rules for adding one (link it from `JUnitTestMenuPage`,
  make it deterministic, set testids, keep it one-subject, and follow the
  ordinary page conventions).
- `60-rendering-tests` - `ScreenInspector`, `elementScreenshot()` and the colour
  histogram, the HtmlEditor case that it was written for, and the failure
  screenshots. This absorbs the old HtmlEditor page.
- `70-mockito-pitfalls` - moved as it was; it is general Java advice and still
  true.

`data-binding-tests` and `junit-testing` are deleted, with their three 2017
screenshots; what was still true in them (the build-order and binding-order
tests, the HtmlEditor screenshot test, the Selenium and headless-Chrome facts)
was rewritten into the pages above. The one link to `testing/junit-testing`, in
`introduction/developer-view-of-domui`, now points at the section.

**A fixture page was added**, `pages/test/uitest/OrderEntryTestPage`, linked
from `JUnitTestMenuPage`: a form of three controls built with `FormBuilder`, a
basket as a `DataTable` with a `LinkButton` in every row, a mandatory customer
field, and an answer div. Fixed data, no database, so every assertion on it is
stable. Every fragment in the new pages comes from it or from the tests that
drive it: `ITOrderEntry` (the connector alone), `ITOrderEntryPageObject` (the
generated page object), the generated `POOrderEntryTestPageBase` +
`POOrderEntryTestPageBasket` + `POOrderEntryTestPageBaseBasketRow`, and the
hand-written `POOrderEntryTestPage` that adds the `answer()` proxy and an
`order(album)` step. **All five tests run and pass** under
`mvn21 verify -pl to.etc.domui.demo`.

**Three defects found while writing it**, all fixed:

- **A generated page object could not click anything inside a table row.**
  `PogDataTable` merges the components it finds per column by their test id with
  the row prefix stripped (`/r1/lbtn_Order` -> `lbtn_Order`), which is right,
  and then handed that stripped id to the *selector* as well, which is not: the
  rendered attribute still holds the prefix, so `*[testid='lbtn_Order']` matched
  nothing and every generated row accessor for a button timed out. Fixed in
  `CpDataTableRowBase.getCellComponentSelectorCss()`, which now uses the plain id
  when the cell really has it (a component whose testid was set explicitly, like
  the display spans of the value columns) and falls back to matching the end of a
  repeated one. `ITOrderEntryPageObject` failed before the fix and passes after
  it.
- **`WebDriverCommandBuilder.timeout(int milliseconds)` was passing milliseconds
  to a field counted in seconds**, so `timeout(5000)` asked for a wait of 5000
  seconds. The builder now converts, and `WebDriverConnector.setNextWaitTimeout`
  says in its name and its javadoc that it takes seconds.
- **The generator's own "extend me" class imported `UrlPage` and never used it.**
  `PoGeneratorContext` no longer adds that import.

Also noted, not acted on: `PogButton` generates a proxy class called `ButtonPO`,
which does not exist - nothing registers it (`DefaultButton` is handled by
`PogSimple`), so it is dead code; and `IPoProxyGenerator.identifier()` is
`@Deprecated` with two implementations that throw.

### 2026-09-05 - The release notes deleted, and the version-history asides

Phase 4's two documentation items, both in `domui.github.io`.

**`release-notes/` is gone** - the section page and `release-notes/domui-2-0`,
which described the 2017 move from Hibernate 3.6 to 5.2 (wrong twice over on
Hibernate 7.2), the conversion of the javascript to TypeScript, `setClicked()`
changing shape, and thirty other "this changed" items. Everything on it that is
still a live subject - the AceEditor, the SearchPanel, FileUpload2, sass, the
typed properties in QCriteria - has its own current page from the component
group work, so nothing was lost by deleting it.

Two exceptions were rescued rather than deleted, because they describe behaviour
the framework still has and nothing else documented them:

- **The login brute-force limit.** Ten failed attempts for one user id within
  five minutes and `UILogin.login()` returns false even for the right password
  until the five minutes pass. Both numbers sit in `DefaultLoginHandler`;
  another `ILoginHandler` replaces them.
- **Impersonation.** `UILogin.impersonateByLoginId(id)` authenticates a user
  without a password and makes them the current user - rights included - if the
  real user's `IUser.canImpersonate()` allows it (it is false by default);
  `getRealUser()` gives the user behind it and `impersonate(null)` ends it.

Both are now in `getting-started/example-skeleton`, next to the `UILogin.login()`
call that page already walked through.

**The version-history asides.** Each page said what something "used to be" or
what changed "since DomUI 2.0"; all seven now state what is true:

- `about` - the paragraph about pages describing "work in progress on DomUI 2.0"
  became one about the chapter-by-chapter rework. While there: the demo url on
  that page was `etc.to/demo` and it claimed the demo deploys automatically from
  the master build; it is `demo.domui.org`, deployed with `scripts/deploy-demo`.
- `70-implementation-details/state-management` - "In DomUI 2.0 we also have
  SubPages" is now "There are also SubPages".
- `99-todo/subpages` - the paragraphs explaining that DomUI 1.x could not do
  single-page applications and that "DomUI 2.0 has a new concept" became a plain
  statement of what SubPages are for.
- `components/rules` - the largest one. The opening about the mistakes made when
  DomUI started, why there are numbered components and how "questionable" the old
  stylesheets are, is replaced by the rule a reader needs (the highest number is
  the current component; styles come from the SCSS theme `winter`). "Reset
  scripts - no longer used" is "No reset stylesheet". The browser section - the
  `iehell.jpg` picture, "those times of yore", Internet Explorer, Netscape 1.1
  and what older stylesheets could preprocess - is two sentences about writing
  for current browsers and preferring feature detection; the image file is
  deleted. The fragment rule now says where fragments live
  (`resources/themes/scss/winter`) and that a new one is added with an `@import`
  in that theme's `style.scss`, which is what actually happens - the old text
  named `style.theme.scss` and promised automatic discovery "in a later version".
  The form builder is "the FormBuilder from `component2.form4`", not "the current
  best version".
- `getting-started/intellij-plugin` - "Since DomUI 2.0 all code that earlier
  accepted property paths as strings now also accept typed properties" is now a
  statement that it accepts both.
- `look-and-feel/animations` - the trailing "Since: DomUI 2.0" line is gone.
- `testing/junit-testing` - more than an aside: the page still offered PhantomJS
  as an alternative test platform (and said in the same breath that it is
  unsupported), explained why htmlunit was abandoned for it, and had a section
  about the Travis-CI build, including how to pin the phantomjs version in it.
  `BrowserModel` has had PhantomJS commented out for years and the build has not
  been Travis for as long, so those three sections are deleted. What is left says
  what the tests do now: headless Chrome by default, `webdriver.hub` for another
  browser, where the chromedriver is looked for, and where the screenshot of a
  failed test lands. The chromedriver download link pointed at the retired
  `sites.google.com` page and now points at Chrome for Testing.

The site builds: 154 pages, down from 156.

### 2026-09-05 - The last pre-component2 demo pages; agenda and drag and drop

Two things at once: the deletions phase 4 was waiting for, and the two features
that were only reachable through the pages being deleted.

**Deleted from the demo** - 50 files, none of them referenced by the
documentation (three of them, the agenda and the two drag and drop pages, are
replaced by the rewrites below):

- `pages/binding/tut1/**` (9 files): the old binding tutorial, and the
  "Binding tutorial" block at the end of `TutorialListPage` that linked it. The
  tutorial's own binding chapter (`pages/tutorial/binding/**`, the pages the
  data-binding chapter `!demo()`s) covers the same ground with current APIs.
- The whole `pages/overview/**` tree: `allcomponents` (a pre-`component2`
  "all components" fixture only the JUnit menu still linked), `buttons`, `tbl`,
  `form`, `BadPage`, `DatabaseSchemaExpl` (which described the demo database as
  DerbyDB and pointed at a codeplex url that has not existed for years), and the
  empty `dynaima`, `fasthtml`, `meta` and `graph` directories.
- `BasicOverviewPage` (referenced by nothing once `OldHome` was deleted),
  `TableMenuPage` (the pre-group table menu, superseded by the group 6 pages) and
  `DataTable1Page` (the same `DataTable` again), plus the "Tables: more examples"
  section of `ComponentListPage` that held the last two, and
  `img/chinook-schema-1.1.png`, which only `DatabaseSchemaExpl` used.

**The agenda is a component group now.** `WeekAgendaComponent` and `MonthPanel`
were only demonstrated by `DemoWeekAgenda`, a page under "Special components"
that used `FloatingWindow`, `DateInput` and `SplitPanel` and had a Dutch new-
appointment window. They are group 14 of the components section
(`components/140-agenda`), with two demo pages under
`pages/components/agenda/`:

- `WeekAgendaPage` - a schedule model with work hours, a holiday and
  appointments; buttons that move the period and switch between the day, the
  work week and the week; an item renderer that colours an appointment by its
  type; and dragging on the raster asking for a new appointment through an
  `InputDialog`, which is added to the model and appears without a rebuild.
- `MonthPanelPage` - two panels, a day click handler, and the clicked day marked
  with `setMarked()`.

Five defects were fixed to make that work:

- **The raster lost its last hour.** `initModel()` had
  `m_endHour /= 60 + 1`, which divides by 61 instead of dividing by 60. A model
  whose work hours end at 17:30 got a raster to 17:00 rather than to 18:00.
- **`setMode()` threw when no date was set yet.** It called `initDateBounds()`
  unconditionally, and that requires a date; a page that sets the mode before the
  date got a `NullPointerException` ("date must not be null"). It only computes
  the bounds when there is a date now - `createContent()` defaults the date to
  today anyway.
- **`BasicScheduleModel.getScheduleItems()` ignored the period.** It filtered the
  items into a list and then returned the unfiltered field, so a model with a
  year of appointments handed all of them to the component for every week shown.
- **`ScheduleMode.MONTH` did not exist.** The enum offered it, `initDateBounds()`
  handled it, and `createContent()` threw "mode not implemented yet" for it. The
  value is gone; `MonthPanel` is the month view.
- **`MonthPanel` had no stylesheet at all.** The component renders `ui-mp*`
  classes that only the obsolete css themes ever styled, so in the current theme
  it came out as a wall of numbers. There is a `_monthpanel.scss` now: the month
  name, the weekday header, the week numbers, the greyed-out days of the
  neighbouring months, the hover on clickable days and the marked day. While
  writing it: the weekday header cells used the day cells' class (`ui-mp-dh`
  now), the default marker class was the misspelt `mp-ui-mrk` (`ui-mp-mrk`, as
  `MonthPanel.MARKED`), and `mark()` located the clicked day by dividing a
  millisecond difference by 86400000, which is off by one in the week a
  daylight-saving change falls in - it counts days with `DateUtil.deltaInDays()`
  now.

**Drag and drop is documented, and works.** The two demo pages
(`DemoDragDrop`, `DemoTableInDrag` - the second with a Dutch explanation) became
`pages/components/dragdrop/DragDropDivPage` (pets dragged into a basket and back)
and `DragDropRowPage` (tracks dropped into a playlist at the position they are
dropped at, rows reordered by dragging, and dragged back off the list). The
documentation is `components/150-drag-and-drop`, described as what it is: not a
component but two handlers hung on nodes.

Four defects, three of them making it plainly broken:

- **Dropping into an empty table did nothing at all.** `RowDropzoneHandler`
  computed the column under the mouse from the last row it found, and with no
  rows at all that is `null`, so `locateBest()` threw a `TypeError` before the
  server was ever called. The dragged node stayed hidden - the drag hides it -
  and the drop vanished. Every playlist starts empty, so this was every first
  drop. An empty body now gives index 0, and the insert marker gets a cell to
  live in.
- **The drop zones were measured once per page.** `dropGetList()` caches the
  zones with their positions and sizes, and the comment says the cache is cleared
  when an ajax request is done - but `dropClearList()` was never called by
  anything. After the first drop the cached rectangles are stale, so a drop
  outside the old rectangle is not seen. `dragMouseDown()` clears the list, so
  the zones are measured once per drag.
- **The winter theme had no drag and drop css either.** `ui-drgbl`,
  `ui-drp-hover` and `ui-drp-ins` only existed in the obsolete css themes: no
  move cursor, no zone highlight, and an invisible insert marker.
  `_draganddrop.scss` supplies them.
- **`IDropHandler.getDragMode()` was never called.** The mode is a property of
  the target - `Div.setDropHandler()` defaults to `DropMode.DIV`,
  `Div.setDropBody(body, ROW)` selects row mode - so every implementor wrote a
  method that did nothing. It is gone.

**And the dead half of the drag and drop implementation went with it.**
`UIDragDropUtil` kept a registry of `IDragNdropPlugin`s keyed on a `DROP_MODE`
attribute, whose second entry, `DivModeDragAndDropPlugin`, emits
`DDD.makeDraggableById(...)` against `divModeDragAndDropPlugin.js` - a file
nothing loads, so that path could only ever produce a javascript error. The
plugin, the interface, the unreferenced js and css, and the attribute that
selected them are deleted; `UIDragDropUtil` renders the attributes and that is
all it does.

Verified by running the demo: the agenda in all three modes, moving the period,
the new-appointment dialog adding to the model, the month panel's day click and
marking, dragging pets in and out of the basket, and dropping tracks into an
empty playlist, reordering them and dragging them back off. `mvn21 install` over
all modules is clean and the 42 `TestDbQCriteria` tests pass; the site builds
(156 pages).

### 2026-09-05 - The open candidates worked off

Everything in "candidates still open" that was not marked IGNORE or already
FIXED has been done. The framework changes:

- **`IUIAction` buttons are generic everywhere.** `addButton(IUIAction<Void>)`
  became `addButton(IUIAction<?>)` in `IButtonBar`, `ButtonFactory`, `ButtonBar`
  and `ButtonBar2`, and `LinkButton` now has the same pair `DefaultButton` has:
  `LinkButton(IUIAction<?>)` and `<T> LinkButton(T instance, IUIAction<T>)`. The
  old `LinkButton(action, instance)` argument order is gone; nothing called it.
- **`@UIUrlParameter` on an unreachable setter is now an error.** A property
  exists only when it has a getter, so the annotation on a write-only property
  was never seen and the page silently kept its default.
  `DefaultPagePropertyInjectorFactory` scans the page's setters after computing
  the injectors and throws a `ProgrammerErrorException` naming the method. It
  fires only when there is no getter at all - an annotated property no factory
  accepts is still ignored, as before.
- **`DateInput2` tells the browser which date pattern the server will read.**
  `DateConverter.getDatePattern(locale)` is now public, `DateInput2` translates
  it into the calendar's `%`-notation and renders it as `data-datefmt`, and
  `domui.dateinput.ts` uses that attribute for the repair and for the calendar
  popup, falling back to `Calendar._TT["DEF_DATE_FORMAT"]` when it is missing.
  Only a Dutch and an English calendar bundle exist, so before this every other
  locale repaired input as Dutch `d-m-Y` while the server parsed the JDK SHORT
  pattern.
- **A `MsgBox2` shows its own errors.** It adds a non-propagating
  `ErrorMessageDiv` to its content when it builds, so a control error raised in
  the box stays in the box. Before, the box had no listener of its own and the
  application's default error component (a *propagating* `ErrorMessageDiv`)
  showed the message in the box and again on the page.
- **`ExpandCollapsePanel` keeps its content inside itself.** It was a `Span`
  that hung its content div next to itself with `appendAfterMe()`. It is now a
  `Div` with a header and, while expanded, a content div; the state is a boolean
  and toggling rebuilds. Its only user is `AsyncDiv`'s "Details" stack trace.
- **The source viewer's css.** `.dm-srcp-scrl` was absolutely positioned over
  the page, drawing line 1 on top of the "Source for ..." title; it is a normal
  scrolling block now.

The demo:

- `pages/OldHome.java`, `pages/special/BasicPage.java` and
  `pages/special/BasicListPage.java` are deleted. The two binding-tutorial
  `InvoiceListPage`s, the only users of `BasicListPage`, were rewritten as plain
  `UrlPage`s.
- `AbstractSearchPage` and `AbstractCdShopListPage` no longer keep components in
  fields. The pattern used for both: the result container is a local `Div` of
  `createContent()` and the search handler closes over it, so a rebuild makes a
  new div and a new handler that uses it. No `forceRebuild()` is involved, so the
  search panel keeps what the user typed.

**The demo database was repaired.** The corruption in
`to.etc.domui.derbydata`'s `CreateDB.sql` was one mechanical accident: every `n`
standing directly in front of a quote character in the script had been deleted.
That is every value ending in `n` - `Iron Maide`, `Led Zeppeli`, `Quee`,
`Yamma Brow`, `Berli`, `Bosto`, `Dubli` - and every `n` before an escaped
apostrophe: `Ca''t`, `Do''t`, `Ai''t`, `Talki''`, `Heave''s`, `Ma''s`. Fewer
than five values in the whole file still ended in `n`, which is what gave the
rule away. There is no clean copy to restore from - every copy on this machine
and every version in git history is corrupt - so the values were reconstructed:

- the apostrophe cases from the rule itself (`Ca''t` -> `Can''t` and so on);
- the value-final ones from the file's own vocabulary: a word that appears
  somewhere *inside* another value was never corrupted, so a final word `W`
  whose `W+n` occurs elsewhere and which does not occur elsewhere itself is a
  truncation. That is how `Zeppeli` (next to the album `Led Zeppelin I`),
  `Dixo`, `Dickinso`, `Clapto` and 350 others were found;
- the rest by hand from a dictionary pass over what the vocabulary rule left,
  including the short ones it cannot judge (`Ma` -> `Man`, `Su` -> `Sun`,
  `Gu` -> `Gun`, `So` -> `Son`, `Ru` -> `Run`, `O` -> `On`).

431 distinct values in 829 rows changed. Titles that really do end without the
`n` were kept: `The Evil That Men Do`, `You Shook Me`, `Fast And Loose`,
`She Suits Me To A Tee`, `Yo-Yo Ma`, `Down by the Sea`, and the roman numerals.
A handful of never-repeated names cannot be judged either way and were left
alone; the file is not guaranteed identical to upstream Chinook, but no
recognisably broken name is left. The 42 `TestDbQCriteria` tests pass on the
repaired script.

Verified by running the demo under jetty: the source viewer, the reworked search
pages (`SearchPanelPage`, `ArtistListPage`, the binding tutorial's
`InvoiceListPage` and its edit screen), `MsgAskPage`'s validation error, and
`AsyncDivPage`'s expandable details.

### 2026-09-05 - Four more candidates fixed

- **`AceEditor.selectWord()`** now feeds the computed range back:
  `selection.setRange(selection.getWordRange(row, col), true)`, with the line
  made 0-based first, the way `select()` does it. Verified in the demo's ace
  page by running exactly the statement the method emits: it selects the word.
- **`LoadedImage.create()`** no longer builds the `LoadedImageInstance` it threw
  away.
- **`ImageSelectControl` has a focus target.** `getFocusID()` and
  `getForTarget()` point at the `FileInput` (and at nothing when the control is
  disabled or readonly, which is when no input is rendered); the dead `m_sib`
  field and the commented-out `HoverButton` are gone. Two things were needed to
  make it actually work:
  - the `FileInput` is now created with the control rather than in
    `createContent()`, because a label's `for` is calculated before the control
    it points at is built - the same reason `Text2` keeps its `Input` in a final
    field;
  - **`FileInput` implements `IForTarget`**, returning itself. `HtmlTagRenderer.
    visitLabel()` walks the `for` chain and drops the target the moment it meets
    a node that is not an `IForTarget`, so without this the label got no `for`
    at all. `Input`, `TextArea` and `Checkbox` already did this.

  Verified in the demo: `ImageUploadPage`'s "Your avatar" label now renders
  `for="_11"`, the id of the rendered file input.
- **`Icon.of(String)`'s javadoc** says what the method does: it returns an
  `ImageIconRef`, and the extension decides what *that ref* creates - an
  `ImgIcon`, an `SvgIcon` or a `FontIcon`.

`mvn21 test` passes on the framework (58) and the demo (9).

### 2026-09-05 - The open items checked against the tree

Every open box and every entry of the candidate list was checked against the
current source, the current site and the running demo, to find the ones that had
been fixed or had become pointless in passing.

**Marked done.** Two candidates and three phase items:

- *The form builder's labels have no `for`.* `ResponsiveFormLayouter` calls
  `Label.setForTarget(control)` in both places it attaches a label; it was fixed
  in `f7f9df6af` while the group 1/2 pages were written, so a form label focuses
  its control now.
- *`DemoAppTitle` wants eyeballing.* The page is gone (`8a564b553`).
- *Phase 2: purge `javax.*`, Java 8, Launchpad and 1.1/2.0-branch references.*
  Nothing on the site says Java 8, Launchpad, `2.0-stable`, `1.1` or
  `master-java11` any more, and the one remaining `javax.` is the deliberate one
  in `typed-properties`.
- *Phase 3: the tutorial tracks the spine of the demo.* `TutorialListPage` walks
  eleven sections from "a page with a div in it" to "writing a component"; the
  old binding tutorial is the only remnant, and phase 4 deletes it.
- *Phase 3: the source-viewer story.* Verified by running the demo: every page
  gets the `</>` link from `Application.onNewPage()`, and `SourcePage` renders
  the file with the Java highlighter built in phase 0. `SourceIcon` is dead code;
  the link comes from `SourceBreadCrumb`.

**Checked and still true**, so left open: every other candidate. The `IUIAction`
asymmetry, `AbstractSearchPage`/`AbstractCdShopListPage` holding components in
fields, `EnumSetInput.setMatcher()`, all three `CKEditor` items and the bundled
4.3, both `AceEditor` items, `FileUpload2.renderEmpty()`, `LoadedImage.create()`,
`ImageSelectControl`'s focus target, `Icon.of()`'s javadoc, `@UIUrlParameter` on
a getterless setter (`ClassUtil.calculateProperties()` skips any property without
a getter, so the annotation is never seen), `RadioGroup.createFromEnum`,
`DateInput2`'s locale-blind repair, `ExpandCollapsePanel`, the message box shown
in two error fences, the Dutch default bundles, `OldHome`, the deprecated
`BasicPage` under the binding tutorial, the truncated names in `CreateDB.sql`,
the jetty comment in `pom.xml` and the stale `README.md`. The JPA executor is
still in hibutil's unbuilt `removed/jpa/`, `release-notes/` is still there, 14 of
the site's 16 images are still 2017-2018 screenshots, and 46 framework sources
still carry `@Deprecated`.

**One new candidate**, found while checking the source viewer: `SourcePage`'s
title is drawn over the first line of the file.

### 2026-09-05 - The four group 8 candidates, done

All four things group 8 turned up were fixed rather than left as candidates.

**`Dialog` now builds a `ButtonBar2`.** It made a pre-`component2` `ButtonBar` -
a `<table width="100%">` with one cell - so every dialog in every application had
a button bar of the old kind while `ButtonBar2` is what the documentation tells
people to use. `IButtonBar` is what `Dialog` and its callers talk to, so the swap
is the field type and one `new`. The look is kept by css rather than by the
table: `_floatingWindow.scss` gives a `.ui-bbar2` directly inside `.ui-flw-tc` /
`.ui-flw-bc` the padding and the right alignment the old `.ui-bb-middle` cell
had. Verified: the dialog's bar renders as
`ui-bbar2 ui-bbar2-horizontal > ui-bbar2-l > ui-bbar2-bc`, save and cancel still
in that order (`ButtonBar2` sorts on the button order, which is -1 for all of
them, and the sort is stable), and saving, cancelling and refusing still behave
as they did. The old `.ui-bb-middle` rules stay: `PopInPanel`,
`TreeSelectionWindow`, `MiniTableBuilder`, `AbstractImportPage` and the log pages
still use the old bar, and those are not group 8's to change.

**`InputDialog`'s two confirmation dialogs use `Text2<String>`.**
`confirmDeleteInBlood()` and `confirmWithReason()` built their input with
`TextStr`, the only remaining use of a pre-`component2` control in this corner of
the framework. Both now render a `ui-txt2`, and `setMaxLength()` / `setSize()`
mean the same thing on it. Verified by driving both dialogs: a wrong answer is
still refused with an error box, the right one deletes, and the reason dialog
hands its text to the handler.

**`Window` takes an `IIconRef` for its title bar.** `setIcon(String)` created an
`Img` and inserted it into the title bar by hand (with an IE-era comment about
where the close button sits); the title bar therefore could not carry a font
icon. The window now keeps the `IIconRef` and renders it in `createTitleBar()`,
which is the method that already rebuilds the bar when the title changes.
`setIcon(String)` stays, as `setIcon(Icon.of(url))`, so the three callers that
pass a `THEME/...` png are unchanged - verified: the `LookupInput2` popup still
shows `ttlFind.png` - and `WindowPage` in the demo gained a window with
`Icon.faMusic` in its title bar, which renders as `fas fa-music`.

**The old `MsgBox` is out of the documentation.** The "MsgBox, the older form"
section of `building-pages/90-telling-the-user` documented the superseded class
next to `MsgBox2`, which is exactly what the guiding principle rules out; it is
deleted. The one other mention, an example in `80-metadata` using
`MsgBox.info(this, $(...))`, is now `MsgBox2.on(this).info($(...))`. While there,
the same page's claim that message box buttons are "laid out by priority rather
than by the order you add them" was corrected to what the code does: the box
sorts them only as long as no button was given a priority of its own.

`mvn21 test` on the framework and the demo passes (58 + 9). Site builds clean,
130 pages.


### 2026-09-05 - Components groups 12 and 13: charts, and asynchronous work

The last two groups, and with them the thirteen are done.

**`components/120-charts/`**: a group page plus `plotlygraph`, `traces` and
`layout`, carried by the five chart demo pages. The group page is built on the
one thing that makes this component unlike every other: **the page does not carry
the chart's data**. The graph renders as an empty box, the browser then asks for
the dataset, and `IPlotlyDataSource.createDataset()` runs in a request of its own
with the page no longer active and a `QDataContext` created for that call. So a
slow query does not delay the page - and a source that reads a field of the page
it was made in is a bug waiting for a second user. The `traces` page is organised
by what each trace type *draws* (a line, bars, a pie, a hierarchy, a dial),
because that is how someone arrives at the question.

**`components/130-async/`**: a group page plus `asynccontainer`, `asyncdiv` and
`pollingdiv`, carried by three new demo pages. The group page states the shared
mechanism once - all three work only because the browser polls the server every
two and a half seconds - and the shared rule once: while the job runs the page is
not active, so the job may touch none of it, not its components, not its fields
and not its shared `QDataContext`. `AsyncContainer` versus `AsyncDiv` is settled
as *who builds the result*: the job on the worker thread, or the component
afterwards on the page's thread.

**Two open decisions from the inventory of 2026-09-01, settled.**

- **`DynaIma` and the JGraph charters: not documented, and the demo is deleted.**
  `GraphPage` was the "DOES NOT YET WORK" entry of the component overview, and it
  earns the label: it queries `dis_documents` and `kbc_topics`, tables of some
  other application that have never existed in the Chinook demo database. It
  cannot have worked in this demo at any point. `PlotlyGraph` is the charting
  component, so the page and its `GraphSource` are gone and `component.dynaima`
  is a phase-4 deletion candidate.
- **The agenda (`WeekAgendaComponent`, `MonthPanel`): not documented.** It is a
  member of none of the thirteen groups. `DemoWeekAgenda` is **left in place**
  rather than deleted, because deleting it would remove the only way to see a
  component that still exists - but it is built on three superseded components
  (`DateInput`, `FloatingWindow` and `SplitPanel`), so it teaches the wrong thing
  wherever it is looked at. It wants either a rewrite or a deletion, and that is
  a call for phase 3 rather than one to make while documenting something else.

**Demo pages moved and renamed rather than rewritten.** The five Plotly pages
were written in 2021, use the Chinook data properly, and are good; they moved
from `pages/plotly` to `pages/components/charts` and were renamed for what they
show - `TimeSeriesChartPage`, `BarChartPage`, `PieChartPage`,
`SunburstChartPage`, `GaugeChartPage` - and given page titles. The sunburst's
`ImportDataset` helper came with them; its csv is read from an absolute classpath
path, so the move did not break it. The three async demos are new
(`AsyncContainerPage`, `AsyncDivPage`, `PollingDivPage`), replacing
`DemoAsyncContainer` and `DemoPollingDiv`/`SillyClock`.

`ComponentListPage` gained "Charts" and "Asynchronous and long-running work"
sections; "Plotly" and "Graphical components" are gone, and "Special components"
is down to the agenda.

**A mistake in my own demo page, caught by driving it.** The first
`PollingDivPage` printed a poll counter that never moved: the count was built in
`createContent()` and only the time was updated in `checkForChanges()`. That is
precisely the trap the page is about, so the demo now keeps both nodes in fields
and updates both - and the doc page says that a polling div is the one place
where holding a node in a field is right, because the alternative is the default
`checkForChanges()` throwing the whole tree away.

**Verified by driving the demo in Chrome:** the time-series chart draws two
spline series with a legend, a watermark and an axis title; the gauges draw the
plain one and the formatted one with its steps, bar and red threshold at 490; the
sunburst draws 180 paths of the coffee-flavour hierarchy out of its csv. The
`AsyncContainer` shows "75% step 7 of 8" with a Cancel button and then replaces
itself with the job's result; the failing job produces a message box reading
"Exception while creating result for asynchronous task:
java.lang.IllegalStateException: The job could not finish" with its stack trace.
The `AsyncDiv` shows its counted lines on success, and on failure keeps its
heading over "Error in background task: There is nothing to count" plus a
foldable Details panel. The polling clock ticks about every three seconds.

Site builds clean, 152 pages. `mvn21 test` on the framework and the demo passes.
**Not deployed**: the frames are 404 until the demo is redeployed with
`scripts/deploy-demo`.


### 2026-09-05 - Components group 11: rich content editors

`components/110-editors/`: a group page plus `htmleditor`, `ckeditor` and
`aceeditor`, carried by three demo pages in
`to.etc.domuidemo.pages.components.editors`.

The group page splits the three by what they edit - two produce **html a user
formatted**, the third edits **code** - and then splits the two html ones by
weight: `HtmlEditor` appears instantly and has one row of buttons, `CKEditor` can
do far more and takes a visible moment to start. All three are
`IControl<String>`; what differs is what the string is.

**A framework defect fixed, and it was breaking more than the editors.**
`DomUtil.getRelativeApplicationResourceURL()` returned
`"/" + getApplicationContext() + "/" + resource`, but `getWebappContext()`
already ends in a slash - so every url it produced had an **empty path segment**:
`/demo//$ckeditor/domuiconfig.js`. Jetty 11 answers those with
`400 Bad Message: Ambiguous URI empty segment`. The consequence for this group
was total: CKEditor's `customConfig` never loaded, so **none of the four toolbar
sets had any effect** and every editor got CKEditor's stock toolbar. With the
extra slash removed, the demo page shows `DOMUI` drawing the DomUI toolbar and
`TXTONLY` drawing exactly its nine buttons. The same method is
`CachedImagePart.getURL()`'s, so the image cache's part urls were malformed in
exactly the same way and are repaired by the same line; and on a root context
(`""`) the old code produced `//resource`, which a browser reads as a
protocol-relative url - wrong in a second way.

**Three demo pages were dead and nobody had noticed.** `DemoCKEditor` and
`DemoCKEditorResizing` render an empty space and log `CKEDITOR is not defined`,
because `DomApplication` deliberately stopped putting `$ckeditor/ckeditor.js` on
every page - "old and has vulnerabilities. Use CKEditor.initialize on pages using
it", says the comment next to the commented-out line - and the demos were never
updated. The new `CKEditorPage` calls `CKEditor.initialize(this)`, and that
requirement is a callout on both the group page and the CKEditor page: forget it
and nothing appears at all.

**What the old `forms-and-input/aceeditor` page had wrong**, corrected against
the source and the running editor:

- it said the component "loads the Ace editor's javascript from a CDN
  (cdnjs.cloudflare.com)". Those two lines are commented out; Ace is served from
  the framework's own `$js/aceeditor-1.4.13/`;
- its example wrote `setTheme("ace/theme/iplastic")`. `updateTheme()` prefixes
  `ace/theme/` itself, so the **theme takes a bare name** (`"iplastic"`) while
  the **mode takes the full path** (`"ace/mode/pgsql"`). That asymmetry is now a
  callout, because it is invisible until the theme silently does not apply;
- it described the editor as having a "live demo" that was never there, and two
  2017/2018 screenshots that the `!demo()` frame replaces.

`toolbar_DOMUI` and `toolbar_FULL` are byte-identical in `domuiconfig.js`, so the
first thing I wrote - "DOMUI is FULL plus the framework's own buttons" - was
wrong and is corrected: they are the same toolbar, and they are the two sets that
load the extra plugins (image picker, special characters, colours, smileys).

**A group 10 leftover, cleaned up here.** `components/forms-and-input/fileupload`
should have gone when group 10 replaced it and did not. It is deleted now; it
claimed the control "shows a progress bar" during the upload (the component's own
javadoc says "No upload progress reporting is done") and called the value class
`UploadFile`, which is `UploadItem`.

**`components/forms-and-input` is down to the form builder alone.** Its index now
says so and points at the groups that took file upload and the editors. The form
builder is not a member of any of the thirteen groups and still needs a home -
worth settling when the remaining groups are done.

**Demo pages deleted:** `DemoHtmlEditor`, `DemoCKEditor`,
`DemoCKEditorResizing` and `AcePage`, taking the `pages/overview/htmleditor` and
`pages/special/ace` packages with them; the two javascript resources the Ace demo
reads moved to the new package. `ComponentListPage`'s "Rich content editors"
section now lists the three new pages.

**Verified by driving the demo in Chrome:** the Ace editor renders with syntax
colouring, the `iplastic` theme and line numbers, and "Mark every 'var'" puts a
warning marker under each one; `HtmlEditor` shows its fixed toolbar and
`getValue()` hands back `<p>The <b>small</b> editor: ...</p>`; and both CKEditors
appear with the toolbars their sets ask for once the config loads.

Site builds clean, 144 pages, and the `!demo()` frames of the group resolve.
`mvn21 test` on the framework and the demo passes. **Not deployed**: the frames
are 404 until the demo is redeployed with `scripts/deploy-demo`.


### 2026-09-05 - Components group 10: images, icons and file upload

`components/100-images-and-icons/`: a group page plus `icons` (the `IIconRef`
mechanism with `Icon` and `Theme`), `fonticon`, `svgicon`, `imgicon`, `img`,
`displayimage`, `imageselectcontrol`, `fileupload2` and `fileuploadmultiple`,
carried by four demo pages in `to.etc.domuidemo.pages.components.images`.

The group page is built on the one idea the whole group turns on: **an icon is a
reference, not a component**. A component lives at one place in the node tree, so
the same icon component cannot mark two buttons; what components accept is an
`IIconRef`, from which a `FontIcon`, an `SvgIcon` or an `ImgIcon` is made
whenever one is needed. The second half of the group is the other thing: an
actual picture, either shipped with the application (`Img` pointing at a
resource) or held by it (`DisplayImage` and `ImageSelectControl` over an
`IUIImage`).

**`look-and-feel/icons` is dissolved into this group and deleted**, with its
`fontawesome-support` child. It was the largest piece of genuinely wrong
documentation found so far: it said DomUI supports "FontAwesome 4.7 and 5.0
currently" while the tree has `fontawesome4`, `fontawesome5free` **and**
`fontawesome6free` and the demo runs on 6; it carried two paragraphs of
DomUI-1.0-versus-2.0 history that the guiding principle forbids; and the
`fontawesome-support` page told the reader to download FontAwesome by hand, drop
the css in a folder, and write `new FaIcon("fa-folder")` - an API that no longer
exists, `FaIcon` being an enum in the integration modules for years now. What was
true and worth keeping - the `IIconRef` contract, `Icon.of()`, the icon map and
its remapping, how a font pack registers itself, `IconFromCss` - is rewritten
against the source on the new `icons` page. `look-and-feel` is now stylesheets,
animation and header contributors, and says where the icons went. The one link
into the deleted page, from `release-notes/domui-2-0`, is repointed.

**`DisplayImage` was written up and given a demo for the first time.** It had
zero references anywhere in the tree; it is the read-only half of
`ImageSelectControl` and the component a list of avatars wants, so it is
documented rather than dropped - the same call as `PercentageCompleteRuler2` and
`SwitchButton` in earlier groups.

**Verified by driving the running demo in Chrome**, including two real uploads
through the browser's file input:

- the size classes (`is-size-1`..`is-size-7`, `is-size-small`/`normal`/`medium`/
  `large`) and the colour classes (`is-primary`, `is-danger`, ...) are real and
  work on a `FontIcon` and on a single-colour `SvgIcon`. They come from
  `$sizes-map` and `$colors` in `_derived-variables.scss` via an `@each` in
  `_fonticon.scss` and `_svgicon.scss` - which is why grepping for `is-size-1`
  finds nothing;
- **an `ImgIcon` cannot be recoloured**, as the demo shows side by side. That is
  the practical argument for a font or an svg icon in anything themed, and it is
  the callout on the group page;
- an `Img` resolves all three source kinds: `img/logo-small.png` from the web
  application, `THEME/btnSave.png` to
  `$THEME/scss-winter-default-default/btnSave.png`, and a java resource to
  `$RES/to/etc/domuidemo/...`. A **disabled** `Img` has its src rewritten to
  `to.etc.domui.parts.GrayscalerPart.part?icon=...`: the grey version is
  generated on the server, which is worth saying rather than "it greys out";
- `FileUpload2` end to end: choosing a file posts it in the background and
  `onValueChanged` sees an `UploadItem` - `upload-test.png, image/png, 6026
  bytes, written to /tmp/upld…png`;
- `ImageSelectControl` end to end, with ImageMagick doing the work: a 128x123 png
  uploaded through it comes back out of the three `DisplayImage`s at 16x15, 32x31
  and 96x92 - resized on the server, aspect ratio kept.

**A false alarm worth recording so it is not chased twice.** The first upload
test used `img/java-icon.png` and every display rendered "broken". It was not
broken: that file is **16x16**, and these components resize *down* only - when
the source is smaller than the size asked for they hand back the source
untouched. Fetching the THUMB urls from the page showed valid PNGs with
`naturalWidth: 16`. The never-scale-up rule is now a callout on the
`DisplayImage` page.

**Demo pages deleted:** `SvgIconPage` (its content is the new `IconsPage`, minus
the `MsgBox` and `DisplayHtml` it was built on), `DemoFileUpload` and
`DemoBulkUpload` (both `FileUploadMultiple` and nothing else), taking the
`pages/overview/misc` and `pages/overview/input` packages with them.
`ComponentListPage` gained an "Images, icons and file upload" section;
"Simple components" is gone, and "Input Components" - which had lost its uploads -
is renamed "Rich content editors", which is what was left in it and what group 11
will take.

**Candidates found while reading, not acted on** - see the candidates list.

Site builds clean, 142 pages, and the ten `!demo()` frames - four distinct demo
pages - resolve. `mvn21 test` on the framework and the demo passes. **Not
deployed**: the frames are 404 until the demo is redeployed with
`scripts/deploy-demo`.


### 2026-09-05 - Components group 9: navigation and menus

`components/90-navigation/`: a group page plus `breadcrumb2`, `apppagetitlebar`,
`popupmenu2`, `hamburgermenu` and `alink`, carried by five demo pages in
`to.etc.domuidemo.pages.components.navigation`.

The group page sorts the five by what they are handles **on**: the page stack -
which `BreadCrumb2` draws, `AppPageTitleBar` puts a back button on and `ALink`
changes - and the action, which both menus are made of. What a move *does* to the
page stack stays where it was written up, in the walkthrough's
`building-pages/60-page-navigation`; the component pages point there rather than
repeating it.

**A defect fixed: `BreadCrumb2.setValue()` never redrew the crumb.** It read
`if(old != m_value)` before assigning the new value, so the comparison was
`m_value != m_value` - always false, `forceRebuild()` never called. Setting a new
path on a crumb that was already on the screen therefore changed nothing at all;
only the observable-list route worked. Now `if(old != value)`, and the demo page
swaps a rock path for a jazz one to prove it.

**A defect fixed: every `AppPageTitleBar` carried a Dutch button nobody asked
for.** `addDefaultButtons()` added a `SmallImgButton` titled *"Toon lijst van
bijzondere tekens"* which opened `OddCharacters` - a developer aid, on the
"not documented, not a component" list of 2026-09-01, and itself built on the
`@Deprecated` `FloatingWindow`. So the framework's standard title bar put a
Dutch-labelled typing aid on every page of every application using it. The method
is now empty and documented as the hook an application overrides to give all of
its title bars the same buttons.

**Learned while verifying, and worth knowing: `@UIUrlParameter` on a setter does
nothing unless the property also has a getter.** The demo page's `setLevel(int)`
was simply never called - silently, no warning - until `getLevel()` was added
next to it; injection works from the metamodel's property list, and a property
needs a getter to exist. `NavDetailPage` has the getter, which is why the same
annotation works there. A silent no-op is a poor failure mode; worth a defect of
its own (see the candidates).

**Corrected against the running demo:** the first version of the `HamburgerMenu`
page said the menu positions itself against whatever opens it. It does not - it
is `position: absolute; right: 0`, so it lines up with the right edge of the
block it is added to, wherever the button is. That is why it looks right in an
`ExpandHeader` (whose three-bar button *is* at the right) and lopsided anywhere
else, and it is now the callout on that page and a paragraph on the demo page.

**Verified by driving the running demo in Chrome:** the popup menu opens below
its button with icons and texts, greys out the entry that has a disable reason,
closes on choice and reports it; a menu of nothing but icons renders no text
column at all. The hamburger of an `ExpandHeader` opens under its button with the
same four entries, and choosing one runs the action through `onSelection`. The
page crumb grows a step per `moveSub` and gains its back arrow as soon as there
is a page below to go to; the step text is the page's `getPageTitle()`. A title
bar made with `catchError` shows a mandatory-field message under its own title,
and does not leak it to the page's `ErrorMessageDiv`, because the block it is in
is a fence. Every `ALink` renders a real href without a `$cid`, which is the
whole point of the component - `?level=3` and all.

**Not verifiable through the automation:** a synthetic click on an `ALink` does
not fire DomUI's click handler, so the SUB/REPLACE moves were confirmed from the
rendered hrefs and the source rather than by clicking. Every one of those links
is how a person navigates the demo, so the behaviour itself is not in doubt.

**Demo pages deleted:** `DemoALink` (superseded by `ALinkPage`), `DemoAppTitle`
(a title bar and nothing else), `DemoBreadCrumb` (built on the superseded
`BreadCrumb`) and `DemoPopupMenu` (built on the superseded `PopupMenu` and
`SimplePopupMenu`), taking the `pages/overview/layout` and `pages/overview/menu`
packages with them. `ComponentListPage` gained a "Navigation and menus" section
with the five new pages; "Layout: more examples" is gone entirely, "Simple
components" is down to `SvgIconPage` and "Special components" lost its popup menu
link.

**Site pages deleted:** `components/tables-trees-navigation/`, whose whole
content was one Confluence-era `BreadCrumb2` page with a 2018 screenshot. The
`components/index.md` entry pointing at it is replaced by the group entry.

Site builds clean, 134 pages, and the five `!demo()` frames resolve. `mvn21 test`
on the framework and the demo passes. **Not deployed**: the frames are 404 until
the demo is redeployed with `scripts/deploy-demo`.


### 2026-09-04 - Components group 8: windows, dialogs and messages

`components/80-windows-and-dialogs/`: a group page plus `window`, `dialog`,
`inputdialog`, `msgbox2`, `exceptiondialog`, `errorpanel`, `errormessagediv`,
`messageflare`, `messageline`, `infopanel` and `explanation`, carried by seven
demo pages in `to.etc.domuidemo.pages.components.dialog`.

The group page sorts the eleven components by **how much they interrupt** - an
overlay that has to be dealt with, a message that waits to be read, a flare that
is gone in a second - and states the rule the group shares: an overlay is added
to the **page**, never to the `ContentPanel`, and the code that opened it is not
blocked, so whatever must happen after the user answers belongs in a handler.
Messages themselves (the `UIMessage`, where it is posted, the error fence) stay
where they were written up, in the walkthrough's
`building-pages/90-telling-the-user`; the component pages say what each display
component looks like and when to pick it, and point there for the mechanism.

**A defect fixed: `MsgBox2.buttonDefault()` did nothing at all.** `createContent()`
computed a `defaultButton` - from the explicit `buttonDefault(...)` call, falling
back to the single primary button - and then never read the variable; the focus
always went to the *first* button in the bar. So the one thing that distinguishes
`buttonDefault(mbb, prio)` from `button(mbb, prio)` had no effect. The box now
focuses the explicitly marked default button when nothing else took the focus.
Deliberately *not* changed: a box with no explicit default (a plain `yesNo()`)
still focuses the first button, because letting the auto-computed primary take
the focus would silently make enter mean YES in every existing confirmation
dialog in every application.

**A broken constructor deleted:** `Explanation()` left the type null, so
`createContent()` threw on `"THEME/big-" + m_type.name()`. It had no callers
anywhere in the tree and no way to become usable (the class has no setter for the
type), so it is gone rather than repaired.

**Verified by driving the running demo** (jetty on 8088, the pages fetched and
then driven through DomUI's own ajax protocol, since the browser extension was
not connected): the dialog's save sequence stops at `onValidate()` and leaves the
dialog standing with an error box stacked on top of it (z-index 200 over 100);
save closes with reason `save` and cancel with `closed`; `close()` removes a
window without calling the close handler where `closePressed()` calls it;
`confirmDeleteInBlood` refuses a wrong answer and deletes on the right one; the
message box answers arrive through `onAnswer`, `onAnswer2` and the input handler;
three flare messages in one request end up in one flare that turns into an error
flare; a mandatory field reports itself into the panel it is in, the propagating
fence shows the same message in the panel *and* on the page, and the `ErrorPanel`
title follows the severest message ("Errors on the page" turns into "Warning(s)
on the page"). The `MessageLine`, `InfoPanel` and `Explanation` icons resolve
against the demo's theme (`scss-winter-default-default`), which has all six
`mini-*`/`big-*` images.

**Learned while verifying, worth writing down:** a delta renders every button
with `disabled=""`, which looks alarming but is the protocol - `copyAttrs` in
`domui.packets.ts` removes `disabled`, `readonly`, `checked` and `selected` when
their value is empty. And a demo page can be fetched with curl only by following
the redirect chain twice: DomUI redirects a session-less request to `?$cid=….r`,
that one to `?$cid=….x`, and only the third request renders the page.

**Demo pages deleted:** `DemoMsgBox` (built on the superseded `MsgBox` and the
old `ButtonBar`), `DemoMessageLine` (one line), `DemoInfoPanel` and
`DemoFloatingWindow` (a demo of a `@Deprecated` class), replaced by the seven new
ones. `ComponentListPage` gained a "Windows, dialogs and messages" section with
all of them; "Layout: more examples" is down to `DemoAppTitle`, which group 9
takes.

**No site pages were deleted:** the old `components/` tree had nothing at all
about windows, dialogs or messages - the whole group is new material.

Site builds clean, 130 pages, and the eleven `!demo()` frames of the group -
eight distinct demo pages - resolve. `mvn21 test`
on the framework and the demo passes. **Not deployed**: the frames are 404 until
the demo is redeployed with `scripts/deploy-demo`.


### 2026-09-03 - The plan checked against the tree, and the decisions groups 1-7 made

Seven of the thirteen component groups are done, so the plan was read back
against what is actually in the two repositories. Three things were out of step,
and are now fixed.

**The group 7 entry claimed a deletion that had not happened.** The six layout
demo pages it says were "deleted" had only been *unlinked* from
`ComponentListPage`; the files were still in the tree, unreferenced but still
reachable by url. They are deleted now - `DemoCaption`, `DemoCaptionedHeader`,
`DemoCaptionedPanel`, `DemoTabPanel`, `DemoScrollableTabPanel`,
`DemoSplitterPanel` and `DemoVerticalSpacer` - and the entry is corrected. The
same entry said the FIXME'd `ScrollableTabPanel` link was "back", which it is
not: the component works again, and the new `TabPanelPage` demonstrates it.

**A stale candidate was still listed.** `pages/cddb/TrackDetails.java` was
offered as "an empty stub page with no content at all" - but the CD-shop step of
phase 0 made it the track screen, and it has been a real page for days. Removed.

**Every demo page was checked for being orphaned**, the way the layout ones
turned out to be. Of 198 demo pages 30 are referenced by no other java file, and
all 30 are that way on purpose or already recorded: the `pages/test/**` Selenium
fixtures (which are driven by url, and whose separation is a phase 3 item), the
old `pages/binding/tut1/**` tutorial (phase 3), `OldHome` (a candidate),
`BadPage`, `HelloWorld`, `RxTimePage`, `FormBuilderPage1`, and
`DemoBreadCrumb` / `DemoFloatingWindow` / `DemoInfoPanel`, which belong to groups
8 and 9 and will be dealt with there.

**What the groups decided about the components nobody was sure of.**

The inventory of 2026-09-01 listed components "whose future is not obvious",
to be decided when their group came up. For groups 1-7 it now has:

| Component | Group | Decided |
| --- | --- | --- |
| `ChildFragment` | 7 | **documented** - it is the master/detail screen everybody writes |
| `ExpandCollapsePanel` | 7 | **not documented**: `ExpandHeader` does the same thing properly, and this one puts its content *next to* itself. Still a deletion candidate |
| `SplitPanel` | 7 | **not documented**: a table-based sibling of `SplitterPanel`, which is the current one |
| `PopInPanel`, `SizedPanel` | 7 | **not documented**: unused, and nothing on a current screen needs them |
| `LayoutPanelBase` / `XYLayout` | 7 | **not documented**: marked experimental in the source |
| `ActionContainer` | 4 | **not documented**: unused; what it does (show/hide a group of buttons) is a binding on a `Div` |
| `CheckboxSetInput` | 2 | **not documented**: superseded by `EnumSetInput` |
| `DropDownPicker`, `EditableDropDownPicker` | 2 | **not documented**: old, unused, and covered by `ComboFixed2` and `SearchAsYouType` |

None of those were deleted: not documenting one is a documentation decision, and
removing framework code is phase 4, after the docs have stopped pointing at it.

Of the "unused as of today" list, five were written up and given a demo page in
the process - `PercentageCompleteRuler2`, `SwitchButton`, `ColorPickerInput`,
`ExpandHeader` and `Tree3` - and every one of them turned out to need a fix or a
correction to work as documented, which is what being unused for years does to a
component.

### 2026-09-02 - Components group 7: layout and page structure

`components/70-layout/`: a group page plus `contentpanel`, `panel`,
`captionedpanel`, `caption2`, `genericheader`, `expandheader`, `tabpanel`,
`scrollabletabpanel`, `splitterpanel`, `verticalspacer` and `childfragment`,
carried by five demo pages in `to.etc.domuidemo.pages.components.layout`.

The group page states the one rule (**a page's content goes in a
`ContentPanel`**, and overlays go on the page instead) and points at the
walkthrough for writing a fragment of your own, which is what most of a screen
is actually made of. The component pages then answer the questions the
walkthrough does not: which of the three headers to use, what a lazy tab saves,
what folding costs, and what `ChildFragment` does for you.

**A defect fixed: `ScrollableTabPanel` really was broken** - `ComponentListPage`
had its demo link commented out with *FIXME Broken* - and the cause was two
characters. In `domui.scrolltabpanel.ts` both scroll functions guard with
`if(this._ignoreScrollClick != 0) return;`, but `_ignoreScrollClick` is a
variable of the `WebUI` namespace, not a property of anything, so `this.…` is
`undefined`, `undefined != 0` is true, and **every click on a scroll arrow
returned immediately**. With twenty tabs on a fixed-width list inside an
`overflow: hidden` header, the arrows are the only way to reach the later tabs,
so the component was unusable. The guard now reads the namespace variable
directly. Verified in the browser: the tab list's `marginLeft` goes from `0px`
to `-730px` on a click of the right arrow, where it did not move before. The
component is usable again, and the new `TabPanelPage` demonstrates it - so the
FIXME'd link to the old demo is gone rather than restored.
(Note for the build: `tsconfig.json` compiles everything into
`domui-combined.js`, which is what the pages load; the per-file `.js` next to the
`.ts` are stale leftovers from an older setup and are not served.)

**Corrected while verifying:** `SplitterPanel`'s boolean is the orientation of
the **bar**, not of the split - `true` gives a *vertical bar* and therefore two
panels side by side, `false` a horizontal bar and two panels stacked. The first
draft of the demo had it exactly the wrong way round, which the rendered css
(`ui-splt-vert` with `ui-splt-left`/`ui-splt-right`) showed at once. Both the
page and the demo now say it, with a callout, because it reads as the opposite.

Also verified: all six `GenericHeader` types and both `CaptionType`s render as
documented; `ExpandHeader` folds its content; a `TabPanel(true)` marks the tab an
error came from with `ui-tab-err` (verified by leaving a mandatory field empty on
a tab that is not open); a lazy tab's content reaches the page only when the tab
is first opened; and `ChildFragment` shows AC/DC's two albums from nothing but
the artist and `Artist_.albumList()`.

**A demo trap worth remembering:** the first artist by name in the demo database
(*A Cor Do Som*) has **no albums at all**, so the `ChildFragment` page opened on
an empty table and looked broken. The page now queries only artists that have
albums (`exists(Album.class, Artist_.albumList())`).

**Demo pages deleted:** `DemoCaption`, `DemoCaptionedHeader`, `DemoCaptionedPanel`,
`DemoTabPanel`, `DemoScrollableTabPanel`, `DemoSplitterPanel` and
`DemoVerticalSpacer`, replaced by the five new ones. `DemoAppTitle`,
`DemoMessageLine` and `DemoMsgBox` stay under "Layout: more examples" until
groups 8 and 9 take them.
(They were first only unlinked from `ComponentListPage` and deleted a day later,
when the plan was checked against the tree - see the entry of 2026-09-03.)

Site builds clean, 118 pages, all five `!demo()` frames resolve. `mvn21 clean
install` builds the whole tree; the framework's 58 tests and the demo's 9 pass.
**Not deployed**: the frames are 404 until the demo is redeployed with
`scripts/deploy-demo`.

### 2026-09-02 - sigeto: links into a place inside a page

The generator refused any internal link carrying a `#fragment` - it resolved the
whole url as a document name, found nothing, and failed the build with *link to
unknown document*. So a link into a section of another page was impossible, and
the components pages had to point at whole pages instead.

Fixed in `sitegenerator`, and it does more than accept them:

- `Content.documentPart()` / `fragmentPart()` split a url; `ContentItem.resolveURL()`
  resolves the document part only, and `LinkUpdater` re-appends the fragment to
  the rewritten href, so `../datatable/index.md#data-binding-in-a-table` becomes
  `…/datatable/index.html#data-binding-in-a-table`.
- **The fragment is checked as well.** While scanning, each page's anchors are
  collected: its heading ids, computed exactly the way commonmark's
  `HeadingAnchorExtension` computes them at render time (the `Text` and `Code`
  inside the heading, trimmed and lowercased, through a per-document
  `IdGenerator`), plus every `id="…"` written in raw html - which is how the
  older pages anchor their sections. A fragment matching none of them fails the
  build, naming the file, the line, and the anchor that comes closest.
- Same-page links (`[the database](#the-database)`) are checked too; they were
  skipped entirely before.
- The check runs after every page has been scanned (`MarkdownChecker.checkAnchors()`,
  called from `Main`), because a link may point forward at a page not read yet.

Verified against the real site, which is a better test than a fixture: 106 pages
build clean with the fragment links restored; the anchors survive into the html;
a misspelled cross-page fragment gives *link to unknown place in a document:
../datatable/index.md#data-binding-in-a-tabel - did you mean
#data-binding-in-a-table?* and exit code 9; and a misspelled same-page fragment
does the same. The generator's README and CLAUDE.md describe the behaviour.

**This is a change in the `sitegenerator` submodule**, so it needs its own commit
there, and the site repository's submodule pointer needs updating with it.

### 2026-09-02 - Data binding in a table is documented with the table

Binding inside a table is a subject of its own - the cells are made by the
component, only the visible page exists, and the rows can come from an
observable list - so it belongs with the table rather than with the general
data binding page. `components/60-tables-and-trees/datatable` gained a
**Data binding in a table** section covering:

- every value cell **is already a bound control** (a `DisplaySpan` bound to that
  property of that row), which is why changing a row object changes the screen
  with nothing else being called;
- a cell built by a **renderer** has no value to compare and is *not* updated -
  `rerenderOnBind()` redraws it once per request instead (a `CalculatedBinding`
  with `updateAlways`), and `valueHint()` binds the cell's tooltip the same way;
- **editable** cells: `editable()` binds a control to the row's property, the
  control comes from metadata or from `factory()`, and - the trap -
  *whichever way it is made the binding is always to the column's property*, so
  a factory-built control must have that value type. The source carries a FIXME
  saying the same thing;
- **style binding** per cell (`styleBinding(StyleBinder).to(property)`) and the
  **footer** (`getFooterBody()`), where a total binds to the page or a
  controller rather than to a row;
- **rows from an `IObservableList`** (`setList()`): one add/delete/modify moves
  one row, an assign or a multi-change event rebuilds the table. A Hibernate
  relation list is observable, which is the natural master/detail screen;
- and the special cases: a row that is `modified()` is **thrown away and
  rebuilt** (new controls, so an unsaved keystroke, a validation error and the
  focus go with it); only the rows of the visible page have bindings at all; the
  usual `areObjectsEqual` trap, which bites hardest here because the cells are
  made for you; and the cost - the binding pass walks every control on the page
  once per request.

A demo page carries it: `TableBindingPage`, with the same computed column twice -
once with `rerenderOnBind()` and once without - a style-bound cell, a footer
total bound to a property of the page, and a button that adds a line through the
model. Verified in headless Chrome: pressing the button takes the bound cell from
2 to 3 to 14, the `rerenderOnBind` column follows (`3 x 14.95 = 44.85`), the
column without it stays at what it first rendered, the cell gains `dm-tut-hi`
once the row passes ten copies, and the footer total goes 15 -> 16.

`DemoTableBinding2`, one of the older table examples kept under "more examples",
used the superseded `LookupInput` in a control factory; it now uses
`LookupInput2`. The other binding examples there are current and stay.

Also noted: the site generator's link checker rejects an internal link carrying
a `#fragment`, although the generated headings do have ids - so cross-page links
into a section have to point at the page. Candidate below.

### 2026-09-02 - Components group 6: tables, lists and trees

`components/60-tables-and-trees/`: a group page plus `datatable`, `rowrenderer`
(RowRenderer and ColumnDef together), `tablemodels`, `datapager`,
`expandingedittable`, `datacelltable`, `listshuttle` and `tree3`, carried by
seven demo pages in `to.etc.domuidemo.pages.components.tables`.

The group page states the split the whole group rests on - a **model** says what
the rows are, a **renderer** says what one row looks like, a **component** puts
them on screen, and none of the three knows what the others do - with a diagram
and the rule that follows from it: every change to the data goes through the
model, because that is what tells the table which rows to redraw.

**Tree2 is retired in favour of Tree3**, as the inventory decided: the demo page
moved to `Tree3` (with `DemoNode` and the tree model moved into the components
package), the old `Tree` demo is deleted with it, and the `tree3` page documents
the model, the selection predicate and the rendered `ul`/`li` structure - the
last taken from the old `tree-rendering` page, which was already written about
tree3 and is now part of the component's own page.

**Verified in headless Chrome** against the running demo: a table pages without
re-querying (a query counter in the model's own query functor stays at one while
paging); an empty model shows its empty message and keeps its header when told
to; a column's converter, alignment, `maxWidth` truncation (`showTitle`),
renderer and `cellClicked` all come out as documented, and a cell handler wins
over the row handler; a column with **no property** sorts on the property it was
given; the list model's `add`, `modified` and `delete` move exactly one row each;
editable columns put what is typed into the object (`Rubber Soul x 7`), a
`factory` gives one combo per row, and the `ExpandingEditTable` renders its rows;
the `DataCellTable` lays 12 albums out four to a row; the `ListShuttle` moves a
selected album from left to right and the page reads it back; and the tree
renders `ui-tree3-item ui-tree3-closed ui-tree3-branch` with
`ui-tree3-unselectable` on the nodes its predicate refuses.

**Corrected while verifying**, each checked in the source:

- A row that a selection model's `IAcceptable` refuses **still gets a checkbox** -
  a dead one. DomUI has no read-only checkbox, so `setReadOnly(true)` becomes
  `disabled`. The first draft said the row had no checkbox at all.
- **Select-all respects the acceptor**: with an acceptor that takes only AC/DC
  albums, the tick in the header selects 2 of the model's 347 rows.
- `IRowRenderHelper.setRow()` is called **per row**, before that row's cells are
  rendered - not once per page with all of them, which is what the first draft
  assumed.
- A `RowRenderer` becomes **immutable the first time a table uses it**: changing
  a column afterwards throws *This object has been USED and cannot be changed
  anymore*. Now a callout.
- `DataPager` is a wrapper that renders the application's default pager
  (`DataPager2` unless `DataPager.setPagerFactory()` says otherwise), which is
  why a screen never names `DataPager1` or `DataPager2`.
- `ListShuttle.moveSourceToTarget()` is called with a target index of **9999**
  when the shuttle means "at the end"; a model that takes it literally throws.
  The demo model clamps it and the page says so.

**Deleted, superseded:** `components/tables-trees-navigation/datatable` (its
column-width and resizing material is now on the `datatable` page, without the
2017 screenshot, the `!w` callout and the "TBD" opening), `.../tree2` (Tree2 is
superseded by Tree3, and the page's only link pointed at a demo host that no
longer exists) and `.../tree-rendering` (folded into `tree3`). What remains of
that directory is `breadcrumb2`, which group 9 will take; the section is
retitled **Navigation** until then. The demo pages `Tree2DemoPage`, `DemoTree`
and `DemoTreeModel` are deleted.

`TableMenuPage` and its twelve older table examples are **kept for now**, under a
"Tables: more examples" heading: several of them show data binding against
tables, which is a subject the group pages point at rather than cover, and
`DemoTableBinding2` still uses the superseded `LookupInput`. Sorting that out
belongs with the binding pages in phase 3.

Site builds clean, 106 pages, the group diagram renders and all seven `!demo()`
frames resolve. `mvn21 clean install` builds the whole tree; the framework's 58
tests and the demo's 9 pass. **Not deployed**: the frames are 404 until the demo
is redeployed with `scripts/deploy-demo`.

### 2026-09-02 - Security: the html sanitizer now checks values, not just names

`HtmlUtil.removeUnsafe()` is what `DisplayHtml` and `HtmlEditor` put every value
through. It allow-listed element names and attribute names but **never looked at
an attribute's value**, and `href` and `style` were on the allow-list - so a link
carrying a script scheme passed through sanitizing untouched and arrived in the
browser as a working link. Reproduced while writing the group 5 documentation.

Fixed in three parts:

- **Url attributes are checked against a scheme allow-list.** `href`, `src`,
  `action`, `background`, `cite`, `formaction`, `longdesc`, `poster` and
  `xlink:href` may use `http`, `https`, `mailto`, `ftp`, `ftps` and `tel`; a url
  with no scheme is relative and always allowed. Before the scheme is read, the
  characters a browser ignores while resolving one (spaces, tabs, newlines,
  control characters) are stripped from the copy being examined, so
  `java\tscript:` does not slip through, and the comparison is case-insensitive.
  Entities were already decoded before parsing, so `&#106;avascript:` is caught
  as well. A refused *value* costs the attribute, not the element: the link keeps
  its text and loses its href.
- **`style` values are checked**: a value containing `url(`, `expression`,
  `javascript`, `vbscript`, `behavior`, `binding`, `@import` or a backslash
  escape is dropped, after css comments and whitespace are removed so that
  `expr/**/ession` is caught too. Ordinary colour and font styling - which is
  what the editors produce - is unaffected.
- **Dangerous elements are removed with their content.** Previously only the
  *tags* of a rejected element were removed, which is right for a table cell but
  wrong for a script: its text is code, and it stayed on the page. `script`,
  `style`, `iframe`, `object`, `embed`, `applet`, `noscript`, `svg`, `math`,
  `template`, `title`, `head`, `frame`, `frameset`, `base`, `link` and `meta` now
  disappear entirely, and an unclosed one of the non-void ones takes everything
  after it - which is what a browser treats as its content too.

Two smaller hardenings while in there: an `id` value starting with `_` is
dropped (it could collide with a DomUI node id in the browser), and a link
carrying a `target` gets `rel="noopener noreferrer"`.

`HtmlUtilTest` pins all of it down - 16 tests, split into what must survive
(formatting, ordinary and relative links, inline styling) and what must not
(script elements and their content, unclosed scripts, the other content-killing
elements, script schemes in every disguise tried, unsafe style values, event
handler attributes). The framework module's 58 tests and the demo's 9 pass, and
the sanitizer's output on ordinary html - including the example in the class's
own `main()` - is unchanged.

The `displayhtml` page and its demo page now describe this behaviour, with the
element and attribute rules tabled; the warning that html from an untrusted
source is not safe with this component alone is gone, because it no longer is.

### 2026-09-02 - Components group 5: display-only components

`components/50-display-only/`: a group page plus `displayspan`,
`displaycontrol`, `displaycheckbox`, `displayradiobutton`, `displayhtml`,
`percentagecompleteruler2` and `embeddedcode`, carried by four demo pages in
`to.etc.domuidemo.pages.components.display` (the boolean pair and the
ruler/code pair share a page each).

The group page states what an `IDisplayControl` *is* - `IControl` with a marker,
answering `getValue()` with what it was given, `isReadOnly()` with true, and
`getOnValueChanged()` with null - and, more usefully, **when not to use one**:
most input controls already render as plain text when read only, so a screen
that switches between viewing and editing should use one control and one
binding, and a display component is for a value that is never editable here.

`DisplaySpan`'s page documents the six-step order it renders by (converter,
renderer, empty string, the registry's default converter for the value's class,
the domain label, and finally the class's default renderer) - which is why a
`DisplaySpan<Date>` formats a date and an enum shows its label without being
told anything. `DisplayControl` is the same thing as a div and its page says
only what differs.

**Verified against the running demo**: the money converter gives `$ 14.95`, a
renderer puts markup inside the span, an unset value shows the empty string, a
`Date` comes out formatted; the ruler at 35% of 300 pixels renders a 105-pixel
bar with the class `ui-rlr2-pct-35`, and at 100% `ui-rlr2-pct-100` (the only one
the theme styles); `DisplayCheckbox` swaps its image `src` between the theme's
two pictures and shows `null` as unticked.

**A security defect found in `HtmlUtil.removeUnsafe()`** - the sanitizer
`DisplayHtml` and `HtmlEditor` put every value through. It is an allow-list of
elements (`b`, `i`, `u`, `p`, `br`, `a`, `ol`, `ul`, `li`, `code`, `div`,
`strike`, `strong`, `blockquote`, `sup`, `sub`, `hr`) and of attributes (`id`,
`class`, `href`, `target`, `title`, `color`, `face`, `size`, `style`), and:

- **attribute values are never inspected**, so an `href` carrying a script
  scheme survives the sanitizer intact and reaches the browser as a working
  link. Reproduced in the demo before the page was changed;
- the tags of a rejected element are dropped but **its text content is kept**,
  so a `<script>` element leaves its script text behind as visible text (ugly
  rather than dangerous).

The first is a real hole in the one method whose job is to close it. It is now a
candidate below, with the fix named (check the value of `href` - and of `style` -
against a scheme allow-list, or hand the whole job to a library built for it).
**The demo does not ship a proof of it**: the page's "html it does not allow"
example uses a `<script>` and a `<table>` and no script-carrying link, and the
`displayhtml` page states the limitation as two warning callouts - html from a
source you do not control is not made safe by this component alone - without
giving a recipe.

**Deleted, superseded:** the demo pages `DemoDisplayValue` (which demonstrated
the `@Deprecated` `DisplayValue`, seven times over, and nothing else),
`DemoDisplayCheckbox` and `DemoDisplayHtml` - the last of which called
`setText()` rather than `setValue()`, so it never exercised the html path it was
demonstrating.

Site builds clean, 100 pages, all four `!demo()` frames resolve. `mvn21 clean
install` builds the whole tree and the demo module's 9 unit tests pass. **Not
deployed**: the frames are 404 until the demo is redeployed with
`scripts/deploy-demo`.

### 2026-09-02 - Components group 4: buttons and actions

`components/40-buttons/`: a group page plus `defaultbutton`, `linkbutton`,
`smallimgbutton`, `hoverbutton`, `checkboxbutton`, `switchbutton`,
`actionbutton` (IUIAction and ActionButton together) and `buttonbar2`, carried
by five demo pages in `to.etc.domuidemo.pages.components.buttons`.

The group page answers the question the eight pages cannot: **which button**.
An activity diagram walks it - does it hold a value (CheckboxButton if the two
states have names, SwitchButton if not), is it the action of the screen
(DefaultButton on a ButtonBar2), is it inside a control or a row
(SmallImgButton), otherwise a LinkButton - and the page tables what all of them
share: the click handler, disabled-with-a-reason, `IIconRef` icons, and the `!`
accelerator.

**Verified in headless Chrome**: `"S!ave"` really renders `S<u>a</u>ve`;
`setDisabledBecause` puts its reason in the title of a disabled button; the
`is-primary`/`is-small`/`is-outlined` classes come out on the button element as
documented; `mini()` **replaces** the button's classes with `ui-sdbtn-mini`
rather than adding one (so the `is-` classes do not apply to a mini button - now
a warning callout); a CheckboxButton renders its two texts as the css attributes
`data-checked`/`data-unchecked`, defaulting to On/Off; `ButtonBar2` sorts on the
`order` argument rather than call order (first/second/third added 300/100/200
come out in the right order), renders `ui-bbar2-l` and `ui-bbar2-r` groups with
each button in a `ui-bbar2-bc` cell, shows the confirm box before the handler
for `addConfirmedButton`, and `addBackButton()` renders as **Close** when the
page was opened directly; and an `IUIAction` gives its name, tooltip, icon and
disable reason to every button made from it.

**Two defects fixed:**

- **`SwitchButton` did not work as a control at all.** It extends
  `AbstractDivControl<Boolean>` but never overrode `internalGetValue()` /
  `internalSetValue()`, so the control kept a value of its own that the checkbox
  inside it never saw: `getValue()` returned **null** however the switch was
  set, and `setValue()` changed nothing on screen - which also means it could
  not be bound. It now delegates both to the checkbox (the two lines
  `CheckboxButton` already had), and `setChecked()` goes through `setValue()` so
  one path sets the value. `setDisabled()` and `setReadOnly()` are passed on too,
  which they also were not. Verified: the demo page reported `switch=null`
  before the fix and `switch=true` after it.
- **`ActionButton` dropped its instance.** `ActionButton(T instance,
  IUIAction<T> action)` called `super(action)` rather than
  `super(instance, action)`, so the action was executed with a null instance -
  every action written against an instance would fail. Verified: the demo's
  action button now reports "Shipped For Those About To Rock We Salute You".

**Left alone, noted below:** `LinkButton(IUIAction)` and
`ButtonBar2.addButton(IUIAction)` take a `Void` action only, while
`DefaultButton(instance, action)` and `ButtonBar2.addAction(instance, action)`
are generic. The pages document the working pair and warn about the other; the
inconsistency is a candidate.

**Deleted, superseded:** the site's `components/forms-and-input/defaultbutton`
(most of it was a note on how the scss was adapted from Bulma, with a 2017
screenshot, and its rendered-structure example named a css class -
`ui-btntext` - that the code does not use; the real one is `ui-sdbtn-txt`) and
`components/forms-and-input/checkboxbutton` (accurate but a stub, with a 2018
screenshot). The demo pages `DemoDefaultButton`, `DemoLinkButton`,
`DemoSmallImageButton`, `DemoButtonBar` and `DemoCheckbox` are replaced by the
five new ones. `forms-and-input` now holds only the form builder, file upload
and the editors - it will disappear entirely as the remaining groups are done.

Site builds clean, 92 pages, the group's activity diagram renders and all five
`!demo()` frames resolve. `mvn21 clean install` builds the whole tree and the
demo module's 9 unit tests pass. **Not deployed**: the frames are 404 until the
demo is redeployed with `scripts/deploy-demo`.

### 2026-09-02 - Components group 3: lookup and search

`components/30-lookup-and-search/`: a group page plus `lookupinput2`,
`searchinput2`, `searchasyoutype` and `searchpanel`, carried by eight demo pages
in `to.etc.domuidemo.pages.components.lookup`. The old
`components/lookup-and-search/` directory is gone; its four pages were a "TBD"
stub, a rules page written for two generations of the control at once, a
search-as-you-type page and a long SearchPanel article - see below for what
happened to each.

The group page draws the line the section needed: **`LookupInput2` finds one
record to put in a field, `SearchPanel` searches for the records a screen is
about**. They meet where a search panel's relation field is itself a
`LookupInput2`. A diagram carries that, and the page tables the three things the
group shares: where search properties come from (`SEARCH_FIELD` / `KEYWORD` /
`BOTH`), that a search value is what the user may *express* rather than a
property value, and the control/query-builder split.

**What the `lookupinput2` page says that nothing said before**: the three things
it can render and which one you get; and the four-way branch on the number of
records a quick search finds - none says *no matches*, **exactly one is selected
without asking**, 2..100 drop down as a list, more than 100 shows the count. Then
the `$$3` and `$$city=Oslo` prefixes of `DefaultStringQueryFactory`, minimum
lengths per keyword property, the three ways to limit what can be found at all
(root criteria, query manipulator, fixed list), and the three separate renderers
(value, drop-down, dialog table).

**Verified in headless Chrome** against the running demo: all four result
branches reproduced on a `LookupInput2<Track>` - "zzzz" gives
*no matches* (`ui-lui-result-none`), "a" gives *199 record(s)*
(`ui-lui-result-count`), "wonderful tonight" **selects the track outright**, and
"he" on the customer control drops down Helena Holý and Heather Leacock;
`addKeywordProperty("artist.name", 2)` really does refuse to search on one
character and searches on two; the three render states come out as
`ui-lui ctl-has-addons ui-control` with an input, *(no selection)* without one,
and `ui-lui-selected ui-lui-selected-ro` with no buttons when read only.
On the SearchPanel side: metadata alone produces exactly Date of invoice /
Billing City / Customer and searching with an empty form returns all 458
invoices; `addDefault()` after one manual field yields four fields in that order;
`action(() -> builder.addBreak())` really produces two `ui-dfsb-part` columns;
`setOnNew` adds the **Add** button; and **Reset** puts the *default* values back
(`>=5.0`, "until 2010-01-01"), not an empty form.

**Two defects fixed:**

- `SearchAsYouType.MatchMode` was **package-private** while `setMode(MatchMode)`
  is public, so no application outside `to.etc.domui.component.input` could pick
  a match mode at all. The enum is public now.
- (Group 2's `EnumSetInput` fix carries into this group: the control the
  `SearchPanel` example uses for a `Set<Genre>` search now works for enums too.)

**Corrected against the old page while rewriting**, each checked in the source:
`ILookupQueryBuilder` is `<Q, D>` with `appendCriteria(QCriteria<Q>, D)`, not
`<D>` with a method-level `<T>`; `EnumSetQueryBuilder` is `<Q, V>`;
`SearchControlLine` is `<T, D>`; `control(control, queryBuilder)` takes the
control first; `EnumSetInput` needs its label property in the constructor;
`ObjectLookupQueryBuilder` only appends the `%` when `lookupWildcardByDefault`
is on and treats a trailing `.` as "exact"; the clear button is labelled
**Reset** and restores `defaultValue` rather than emptying the form; and
`initialValue()` differs from `defaultValue()` exactly in that Reset goes to the
latter.

**Deleted:** `components/lookup-and-search/` entirely.
`lookupinput2/index.md` said "TBD"; `lookupinput-rules` documented
`LookupInput` and `LookupInput2` side by side with seven 2017 screenshots, which
the "one current way" rule does not allow - what is still true about the control's
states is now on the `lookupinput2` page; `searchasyoutype` was rewritten (its
seven 2018 screenshots dropped, and its closing section on a `SearchAsYouTypeQ`
component removed - **no such class exists**); `searchpanel` was rewritten around
the same structure but against the current signatures, without its
LookupForm comparison section (that class is gone from the framework) and without
the six "the result looks like this:" lines that pointed at images the page never
had. The two release-note links into the deleted pages were repointed.

**Demo pages deleted or moved:** `pages/searchpanel/**` (six pages plus their
menu) became four pages under `pages/components/lookup`, merged in pairs -
`SearchPanelPage` (metadata), `SearchPanelItemsPage` (the builder and default
values), `SearchPanelControlPage` (own control, own query builder),
`SearchPanelFormPage` (addDefault, addBreak, the buttons); `AbstractSearchPage`
moved with them. `pages/overview/lookup/DemoLookupForm{,2}` are deleted - despite
their names they were two more `SearchPanel` examples - and so are
`DemoSearchAsYouType1/2`, replaced by `SearchAsYouTypePage`.

Site builds clean, 85 pages, the group diagram renders and all eight `!demo()`
frames resolve. `mvn21 clean install` builds the whole tree and the demo module's
9 unit tests pass. **Not deployed**: the frames are 404 until the demo is
redeployed with `scripts/deploy-demo`.

### 2026-09-02 - Components group 2: choice input

`components/20-choice-input/`: a group page plus `checkbox`, `radiogroup`,
`combofixed2`, `combolookup2` and `enumsetinput`, each with a demo page in
`to.etc.domuidemo.pages.components.choice` and a `!demo()` frame.
`ComponentListPage` gained a "Choice input" section with the five pages. A small
`Medium` enum with a `Medium.properties` bundle carries the group, so every
control in it takes its texts from metadata rather than from the code.

The group page carries the thing the individual pages cannot: **which control to
use**, tabled by how many values there are and where they come from, with the
five-value rule (`ControlCreatorEnumAndBool`: five or fewer domain values gives a
`RadioGroup`, more gives a `ComboFixed2`) and the fact that a relation property
gets a `LookupInput2` unless its component type hint says `comboLookup`.

**Verified in headless Chrome against the running demo** (devtools protocol, real
key and mouse events): enum labels come out of `Medium.properties`
("Compact disc", "Vinyl LP", "Cassette tape") in every control;
`createEnumRadioGroup` sorts by label and `createEnumRadioGroupUnsorted` does not;
`asButtons()` gives `ui-rbb-buttons` and an empty group `ui-rbb-empty`; a
mandatory radio group and a mandatory combo both report *Mandatory field* with
the label prefixed; a read-only `ComboFixed2` renders as plain text
(`ui-cbb2-ro`, no select at all); `ComboLookup2` renders `Genre` and `MediaType`
from their `@MetaCombo` and a renderer of your own from the code, and
`addExtraButton` sits next to it; a checkbox click handler runs at once while a
box without one keeps its value until the next request; and in `EnumSetInput`
typing "jaz" offers Jazz, clicking it adds the label and fires the change
handler, and its cross takes it off again.

**Corrected while verifying** - things that read plausibly but are not what the
code does:

- A **mandatory combo does not lose its empty choice when a value is picked**.
  The empty option is decided in `renderEditable()`, so it is only gone when the
  control is *built* with a valid value; picking one in the browser leaves the
  option there until something rebuilds. The page says that, and the demo shows
  both cases side by side.
- A **read-only `EnumSetInput` keeps its search box** (read-only), it only loses
  the crosses on its labels.
- `EnumSetInput`'s search is a **server round trip** over the list it holds, not
  browser-side filtering.

**Three defects fixed:**

- `EnumSetInput` **could not be used with an enum** - the thing it is named
  after. Its inner `SearchAsYouType` was given the property name and nothing
  else, so for a value class that is not a "simple type" and has no string
  property (every enum) building the page threw
  `ProgrammerErrorException: You must specify either a property or a converter to
  handle search on a complex data class`. It now hands the input its own
  `getLabelText()` as the converter, so the box searches in exactly the text the
  labels show - which also makes `setConverter()` work for searching, not just
  for display.
- `EnumSetInput`'s constructors declared the label property `@NonNull` (the class
  is `@NonNullByDefault`) while the implementation explicitly handles `null` -
  so the metadata-labelled case could not be written at all. Both constructors
  and the field are `@Nullable` now.
- `ControlCreatorEnumAndBool.accepts()` refused any `controlClass` that was not
  assignable from `ComboFixed2`, so `control(RadioGroup.class)` - asking for the
  other control that same factory makes - was rejected. It now accepts either,
  and an explicit `RadioGroup.class` is honoured whatever the domain value count
  is. (Same shape as the `ControlCreatorDate` defect fixed for group 1.)

**Two defects found and left** (added to the candidates list):

- `ResponsiveFormLayouter` - the **default** form layouter - never calls
  `Label.setForTarget(control)`, which `TableFormLayouter` does, so no label a
  form builder writes has a `for` attribute: clicking a form label focuses
  nothing and does not tick a checkbox. One line, but it changes the rendering of
  every form in every application, so it is offered rather than done.
- `EnumSetInput.setMatcher()` stores a matcher that nothing ever reads, and
  `setAddSingleMatch()` is commented out while the getter remains.

**Deleted, superseded:** the site's
`components/forms-and-input/radiobutton-and-radiogroup` (its main example added
buttons to the page rather than to the group, which the current `RadioGroup`
throws away on its next build, plus two 2018 screenshots and a `ui-rbb-buttoned`
css class that does not exist), and the demo pages `DemoComboFixed` (used the
superseded `ComboFixed`), `DemoRadioButton` (the same wrong pattern) and
`RadioButtonPage` with its `TestEnum`. `DemoCheckbox` lost its checkbox half -
which used the deprecated `Checkbox.setOnValueChanged` - and is now the
`CheckboxButton` demo it mostly was, linked under Buttons until group 4 replaces
it.

Site builds clean, 85 pages, all five `!demo()` frames resolve. The demo module
compiles and its 9 unit tests pass. **Not deployed**: the frames are 404 until
the demo is redeployed with `scripts/deploy-demo`.

### 2026-09-01 - Components group 1: text and value input

The first of the thirteen component groups is written:
`components/10-text-and-value-input/`, a group page plus one page per component,
each with its own demo page under `to.etc.domuidemo.pages.components.input` and a
`!demo()` frame. `ComponentListPage` gained a "Text and value input" section
listing all eight demo pages, in the same order.

| Page | Demo pages |
| --- | --- |
| group index | - |
| `text2` | `Text2Page` (the types and what `getValue()` hands back), `Text2LookPage` (size, maxLength, placeholder, marker, password, hint, attached buttons), `Text2ValidatePage` (mandatory, regexp, validators, converter, and the three states) |
| `textarea` | `TextAreaPage` (cols/rows, maxLength, maxByteLength, read only, the value round trip) |
| `dateinput2` | `DateInput2Page` (date, date+time, seconds, hideTodayButton, read only, disabled, and a box printing the request locale with the date format it produces) |
| `colorpicker` | `ColorPickerPage` |
| `colorpickerbutton` | `ColorPickerButtonPage` |
| `colorpickerinput` | `ColorPickerInputPage` |

The `Text2` page states the value type as the thing that decides converter,
keyboard filter and error message; the exact order `getValue()` checks in
(mandatory, regexp on the raw text, converter, validators) and that the result is
remembered until the raw text changes; the three states; the presentation
methods; buttons rendered *inside* the control (`ctl-has-addons`), which is how
`DateInput2` is built; the rendered DOM; and the `createXxxInput` factories.

**Verified by driving the running demo**, partly through a scripted DomUI round
trip and partly in headless Chrome over the devtools protocol:
`Text2Page` hands back `String/Integer/Long/Double/BigDecimal` values with the
right classes and renders `isAnyKey`/`isNumberKey`/`isFloatKey` per type;
`Text2ValidatePage` gives *Mandatory field*, *Invalid email address*,
*Input format must be 9999 AA*, *Value too large (maximum is 99)* and
*Unexpected character (a) in number abc*, each as the label-prefixed line in the
error div plus the message as the control's tooltip, and accepts `1.234,56` as
1234.56 through the money converter; `setDisabledBecause` puts its reason in the
title; the two buttons on a `Text2` read and clear the box; `TextAreaPage`
returns text with its newlines intact and renders `mxlength`/`maxbytes`;
the colour pickers work (the flat one builds its 356x176 panel and its value
reaches the server on the next request, the button opens exactly one picker and
the disabled ones open none, the optional input hands back `null`).

**Deleted, superseded by the new pages:** the site's
`components/forms-and-input/text2` (it described a `<table>` structure `Text2`
has not rendered for years, next to a "replaces Text<T>" note and a 2017
screenshot), and the demo pages `DemoText`, `DemoDateInput`, `DemoTextArea`,
`DemoColorPicker`, `DemoColorPicker2`, plus `DemoTextStr` and `DemoHiddenText`
which demonstrated superseded controls and were linked from nothing. `DemoALink`
now links to `DateInput2Page`.

**Three defects fixed while writing this:**

- `ColorPickerButton.setDisabled()` was empty and `isDisabled()` returned a
  hardcoded `false`, so the control could not be switched off at all - and it is
  an `IControl`, so a form builder or a binding may do exactly that. Both states
  are now kept, the picker is not attached when either is set, and the button
  gets `ui-cpbt-off` (a `not-allowed` cursor and some transparency, added to
  `_colorpicker.scss`).
- `ColorPickerInput.getValue()` threw a `NullPointerException` for an empty box
  on a control with `setMandatory(false)` - the one combination that makes the
  "or null" branch reachable.
- `ControlCreatorDate.accepts()` tested `controlClass.isAssignableFrom(DateInput.class)`
  while creating a `DateInput2`, so `fb.property(...).control(DateInput2.class)` -
  asking for the control the factory actually makes - was refused.

**Two defects found and left, both in `DateInput2`'s client side** (added to the
candidates list):

- The browser-side repair of a typed date assumes the day-month-year shape
  whatever the locale is, while `DateConverter` has three branches (`nl`:
  `dd-MM-yyyy`, read leniently with all the short forms; `en`: `yyyy-MM-dd`, read
  strictly; anything else: the JDK SHORT pattern for that locale). Reproduced:
  with `___locale=en_GB`, typing `13-3-13` leaves `2013-03-13` in the box while
  the value taken from it is the year **13**.
- Typing something unparseable produces a browser `alert()` rather than the
  framework's own error reporting (already a listed candidate; now confirmed
  from the `en_GB` case, where `13/3/2012` alerts *Invalid date*).

The `dateinput2` page therefore does **not** repeat the javadoc's list of
accepted formats as though it were universal: it tables the three locale
branches and carries both defects as warning callouts, and the demo page prints
the locale of the request together with the format it produces (`05-02-2013`,
`2013-02-05`, `05.02.13` for nl/en/de - checked).

Site builds clean, 80 pages, all eight `!demo()` frames resolve. The demo module
compiles and its 9 unit tests pass. **Not deployed**: the frames are 404 until
the demo is redeployed with `scripts/deploy-demo`.

### 2026-09-01 - The component inventory, and the thirteen functional groups

The whole component set was surveyed from the source rather than from the existing
documentation: every class in `to.etc.domui` that ends up below `NodeBase` was
collected (213 of them), the raw HTML tag nodes in `dom/html` and the framework's own
internal pages (`login`, `trouble`, `log`, `util/importers`, `pages/generic`) were set
aside, and of every pair or triple of generations only the current one was kept. This
grouping is settled and is what the `components/` section is built from.

**Only the current component of each kind is documented.** These are the ones that are
superseded, with what replaces them; they are not named on the site at all:

| Not documented | Current |
| --- | --- |
| `Text` (@Deprecated), `TextStr`, `HiddenText` (@Deprecated), `AutocompleteText` | `Text2<T>` |
| `DateInput` | `DateInput2` |
| `ComboFixed`, `ComboLookup`, `ComboComponentBase`, `ComboFixedClientFilter`, `ComboBoxBase`, `SelectFixed`, `ComboOption` | `ComboFixed2`, `ComboLookup2` |
| `LookupInput`, `LookupInputBase`, `AbstractLookupInputBase`, `AbstractFloatingLookup`, `KeyWordSearchInput` | `LookupInput2`, `SearchInput2` |
| `LookupForm` (gone), the old `component/controlfactory`, the old form builders | `SearchPanel`, `component2/controlfactory`, `form4.FormBuilder` |
| `ButtonBar` | `ButtonBar2` |
| `BreadCrumb` | `BreadCrumb2` |
| `Caption`, `CaptionedHeader` | `Caption2`, `GenericHeader`, `HTag` |
| `MsgBox` | `MsgBox2` |
| `PercentageCompleteRuler` | `PercentageCompleteRuler2` |
| `SmallHoverButton` (@Deprecated) | `HoverButton` |
| `DisplayValue` (@Deprecated) | `DisplaySpan` |
| `FloatingWindow` (@Deprecated) | `Dialog` |
| `ScrollableDataTable` (@Deprecated) | `DataTable` |
| `Tree`, `Tree2`, `TreeSelect`, `TreeSelectMulti`, `TreeSelectionWindow` | `Tree3` |
| `DataPager1`, `DataPager2` | `DataPager` (the wrapper that picks the default) |
| `PopupMenu`, `SimplePopupMenu` | `PopupMenu2` |
| `CheckboxSetInput` | `EnumSetInput` |
| `DropDownPicker`, `EditableDropDownPicker` | - (nothing; they are old and unused) |
| `LiteralXhtml` (@Deprecated) | - |

`Tree3` is `Tree2` plus selection, `ICellClicked2` and double-click expand, and has its
own `_tree3.scss`; nothing in the framework or the demo uses it yet, and the demo still
shows `Tree2`. Documenting `Tree3` therefore means moving the demo to it.

**The thirteen groups**, in the order the section presents them. Each is a page of its
own describing the group and listing its members; each member gets a page and at least
one demo page.

1. **Text and value input** - `Text2<T>`, `TextArea`, `DateInput2`, `ColorPicker`,
   `ColorPickerButton`, `ColorPickerInput`.
2. **Choice input** - `Checkbox`, `RadioButton`/`RadioGroup<T>`, `ComboFixed2<T>`,
   `ComboLookup2<T>`, `EnumSetInput<T>`.
3. **Lookup and search** - `LookupInput2<T>`, `SearchInput2`, `SearchAsYouType<T>`,
   `SearchPanel<T>`. The two complex ones (`LookupInput2`, `SearchPanel`) get several
   demo pages each: the plain case, a query manipulator, own popup columns / own
   renderer, a `SearchPanel` from metadata, from a property list, with `add()` items,
   and with a base `QCriteria`.
4. **Buttons and actions** - `DefaultButton`, `LinkButton`, `SmallImgButton`,
   `HoverButton`, `CheckboxButton`, `SwitchButton`, `ActionButton` + `IUIAction`,
   `ButtonBar2`.
5. **Display-only components** - `DisplaySpan<T>`, `DisplayControl<T>`,
   `DisplayCheckbox`, `DisplayRadiobutton`, `DisplayHtml`, `PercentageCompleteRuler2`,
   `EmbeddedCode`.
6. **Tables, lists and trees** - `DataTable<T>`, `DataPager`, `RowRenderer<T>` with
   `ColumnDef`, the `ITableModel` family (`SimpleSearchModel`, `SortableListModel`,
   `SimpleListModel`, selection models), `ExpandingEditTable`, `DataCellTable`,
   `ListShuttle`, `Tree3<T>` with `ITreeModel`.
7. **Layout and page structure** - `ContentPanel`, `Panel`, `CaptionedPanel`,
   `Caption2`, `GenericHeader`, `ExpandHeader`, `TabPanel`, `ScrollableTabPanel`,
   `SplitterPanel`, `VerticalSpacer`, `ChildFragment`.
8. **Windows, dialogs and messages** - `Window`, `Dialog`, `InputDialog`, `MsgBox2`,
   `ExceptionDialog`, `ErrorPanel`, `ErrorMessageDiv`, `MessageFlare`/`Flare`,
   `MessageLine`, `InfoPanel`, `Explanation`.
9. **Navigation and menus** - `BreadCrumb2`, `AppPageTitleBar`, `PopupMenu2`,
   `HamburgerMenu`, `ALink`.
10. **Images, icons and file upload** - `Icon` (the Font Awesome enum) and `IIconRef`,
    `FontIcon`, `SvgIcon`, `ImgIcon`, `Img`, `DisplayImage`, `ImageSelectControl`,
    `FileUpload2` (single), `FileUploadMultiple`.
11. **Rich content editors** - `CKEditor`, `HtmlEditor` (the small fast wysiwyg),
    `AceEditor` (code).
12. **Charts** - `PlotlyGraph` with its traces and layout classes.
13. **Asynchronous and long-running work** - `AsyncContainer`, `AsyncDiv`, `PollingDiv`.

Groups 6, 7 and 8 overlap with walkthrough pages that already exist
(`70-showing-rows`, `100-layout`, `90-telling-the-user`). The walkthrough teaches; the
component pages are the reference, and each says what the other covers rather than
repeating it.

**Decisions still needed, per component, when its group comes up** - these are the ones
whose future is not obvious and which are not in any group above yet:

- `WeekAgendaComponent` + `MonthPanel` (the agenda), `DynaIma` with the JGraph charters
  (the demo's own link says "DOES NOT YET WORK"), `LayoutPanelBase`/`XYLayout`
  (marked "experimental"), `SplitPanel` (a table-based sibling of `SplitterPanel`),
  `PopInPanel`, `SizedPanel`, `ActionContainer`, `ExpandCollapsePanel` (already an open
  candidate), `ChildFragment`: document or delete?
  ~~Settled for the ones whose group has been done~~ - see the entry of
  2026-09-03; `ChildFragment` is documented, the rest of that line is not, and
  the agenda and `DynaIma` are still open (groups 12 and 13).
- Developer aids rather than application components, and not documented in this
  section either way: `InternalParentTree`, `DebugWindow`, `MiniLogger`,
  `OddCharacters`, `KeyCodeDiv`.
- Unused as of today (0 references outside their own file): `ActionContainer`,
  `AsyncDiv`, `ColorPickerInput`, `DisplayImage`, `EditableDropDownPicker`,
  `ExpandHeader`, `InputDialog`, `PercentageCompleteRuler2`, `PopInPanel`, `SizedPanel`,
  `SwitchButton`, `Tree3`, `CheckboxSetInput`. Being unused is not itself a reason to
  drop one - `Tree3` and `PercentageCompleteRuler2` are the *newest* of their kind - but
  it does mean the demo page is written from scratch and the component gets exercised
  for the first time.

### 2026-08-31 - `qcriteria` moved into `Implementation details`

`data/qcriteria` was the reference page `building-pages/30-using-databases` points at. It
became `70-implementation-details/qcriteria`, "The generic query layer (QCriteria)",
rewritten from the source of `to.etc.webapp.query`, hibutil's `CriteriaCreatingVisitor`
and the demo's `TestDbQCriteria`, and now opens by pointing back at the walkthrough page.

What it says that the walkthrough does not: the query as an expression tree with the
`QRestrictor` / `QCriteriaQueryBase` / `QCriteria` / `QSelection` hierarchy and the
`QNodeVisitor`s that turn it into a rendered string, a JPA `CriteriaQuery` or an
in-memory match; the executor registry, asked per queried class, with `@QJdbcTable`
routing a class to `JdbcQueryExecutor` and everything else to `HibernateQueryExecutor`;
what the translator makes of a dotted path (explicit joins, `LEFT` for optional
relations and `INNER` for required ones, joins cached per relation, a path ending in the
relation's id needing no join at all); how an `exists` subselect is built from the
collection's `mappedBy` - and that a unidirectional collection therefore cannot be used -
plus the automatic rewrite of a two-collection path into nested `exists`; `QSelection`
with its implicit `group by` over every plainly selected property; `@QFld` interfaces and
`QQueryUtils.queryCount`; `in(property, QSelection)` and correlated `subquery()`;
`sqlCondition()` and its `this_.` alias rewriting; `MetaManager.query(Collection,
QCriteria)`; and `testId` with `TestDataContextMock` for querying code under test.

Corrected while rewriting: the old page sent the reader to an `examples/tutorial` module
that does not exist (the tree has `examples/astfixer` and `examples/skeleton`), used an
`Album.year` property the entity does not have, told people to use the *Eclipse* plugin
for property checking, embedded two Confluence-hosted images and a 2018 screenshot, left
"Parent and child relation queries: joins" as `TBD`, and claimed DomUI aborts a query
that eagerly fetches with a `limit()` in place - `handleFetch` in
`CriteriaCreatingVisitor` validates the fetch path and then ignores the strategy
entirely, so eager fetching is simply not implemented. The page now carries a "What is
not implemented" section saying that, plus `QMultiSelection` being rejected by the
Hibernate translator and there being no plain-JPA executor in the built code.

`data/` now holds only the POJO generator; its index points at the walkthrough pages and
at the two implementation-details pages instead of listing what has moved out. The
closing pointer of `30-using-databases` names the new page and what is actually on it.
Links in `introduction/developer-view-of-domui` and `getting-started/example-skeleton`
were repointed by the build's own link repair. Site builds clean, 74 pages, both new
diagrams render.

### 2026-08-31 - `typed-properties` moved into `Implementation details`

`data/data-binding/typed-properties` was the last document left under
`data/data-binding` besides its index, and it was superseded twice over by
`building-pages/40-typed-properties`. It became
`70-implementation-details/typed-properties`, "Typed properties: the annotation
processor", rewritten around what `40-typed-properties` does *not* say: the two classes
generated per annotated class (`X_` with its private constructor and static methods, and
`X_Link<R> extends QField<R, X>`), `QField` as a parent-linked list flattened by
`getName()`, and the exact order in which the generator decides what a property becomes.

Everything on it was verified against
`common/property-annotation-processor` and by running the processor over a probe class
with `javac -proc:only`: primitives come out wrapped, `Collection`/`Map` properties are
endpoints rather than steps, arrays are generated, a reserved-word property gets a
trailing underscore (`getNew()` -> `new_()`), and - the useful surprise - a `java.time`
or `UUID` property is generated only when its getter carries `@Column`, because those
types are in neither the simple-type list nor the annotated-class rule. The old page's
`javax.persistence`, `1.2-SNAPSHOT` poms, `target/annotations` path, "to be released"
plexus note, "work in progress" banner and two 2018 IntelliJ screenshots are gone; the
pom and IntelliJ setup is not repeated but pointed at in `40-typed-properties`.

Links repointed to `building-pages/40-typed-properties`: `data/index.md`,
`data/data-binding/index.md`, `getting-started/intellij-plugin`,
`release-notes/domui-2-0`. `40-typed-properties` gained a closing pointer to the new
page and `70-implementation-details/index.md` a bullet for it. Site builds clean, 75
pages.

With that page gone, `data/data-binding/index.md` - the old data binding article that
`building-pages/50-data-binding` supersedes, still showing `Text`, `TextStr` and string
bindings - was all that was left of the section, and it was deleted the same day, with
its two 2018 screenshots. Its one remaining inbound link, from
`building-pages/10-first-page`, now points at `building-pages/50-data-binding`. `data/`
holds only QCriteria and the POJO generator now, so its index is retitled
**"Databases and queries"** and closes with a pointer to the walkthrough's data binding
page and to `data-binding-details`. Site builds clean, 74 pages.

### 2026-08-31 - `Implementation details` is where the in-depth material goes

`70-implementation-details` collects the in-depth descriptions of how the framework
works: the mechanisms the tutorial and the component pages use without explaining them.
It starts unordered - its index says so - and order is imposed later, once it has enough
pages to have a shape.

First move into it: `data/data-binding/how-does-it-work` became
`70-implementation-details/data-binding-details`, "Data binding details". It now opens by
pointing at `building-pages/50-data-binding`, and everything that page already explains
well was dropped from it: the request round-trip sequence diagram, the bidirectional /
unidirectional walkthrough with its `IControl.DISABLED` examples, and the whole
`bindValue` / `bindErrors()` section with its activity diagram. What is left is what only
this page has - soft versus hard binding and why the update moments are tied to the
request cycle, where a binding object lives (the `IBinding` / `ComponentPropertyBinding*`
class diagram), binding order with its collect-then-move diagram, binding performance,
and when a value counts as changed for collections and mutable compound values. A little
overlap was kept where removing it would have made the remaining text hard to follow: the
two binding moments are still named, and the fact that a value binding is bidirectional
and the rest is not is still stated before the consequences that depend on it.

Links repointed: `data/index.md` (which also lost its bullet to the already-deleted
`property-references`), `components/rules/index.md` (now pointing at the tutorial page
rather than at implementation detail), and the closing pointer in
`building-pages/50-data-binding`. Site builds clean, 75 pages.

### 2026-08-31 - `developer-view-of-domui` rewritten against the source

Every claim on the page was checked against the code. What was wrong:

- "can be made completely cookieless" - `NormalContextMaker` does
  `request.getSession(true)` and `AppFilter` reads `rq.getSession().getId()`; the
  demo needs its `SessionCookieSetup` listener precisely because a blocked session
  cookie makes DomUI fail outright. The page now says the servlet session cookie
  is required.
- "`$cid` contains a session ID" - `ConversationContext` builds it as
  `windowID + "." + conversationId`, so it is the window (tab) plus the
  conversation. Stated as such now.
- "OptimalDeltaBuilder" - the class is `OptimalDeltaRenderer`.
- "implementations exist for JPA and Hibernate" - removed; the JPA executor is in
  hibutil's unbuilt `removed/jpa/`. Restoring it is now a Phase 4 to-do rather
  than something the documentation describes as present.
- The two `help.eclipse.org/neon/` links (2016) became `/latest/` ones, and the
  typed-properties link was repointed from `data/data-binding/typed-properties` to
  `building-pages/40-typed-properties`.

Correct after checking, and kept: querying a collection of objects with a
`QCriteria` (`MetaManager.query(Collection, QCriteria)`, via
`CriteriaMatchingVisitor`), Hibernate 7.2, jQuery 3.7.1, `Text2<T>`,
`NodeBase`/`NodeContainer`, the `web.xml` filter and the singleton
`DomApplication`.

What the page was missing and now has: the stack (Java 21, `jakarta.servlet`,
Jetty 11 / Tomcat 11, Maven with ecj); how a URL reaches a page (`AppFilter`
mapped to `/*`, `getRootPage()`, `UrlPage.createContent()`, class name plus `.ui`,
`@UIPage`, `PageParameters` and `@UIUrlParameter`); per-page access control with
`@UIRights`; layer 2 - the metadata-driven builders and renderers - which finishes
the layering that stopped at layer 1; and the test framework
(`AbstractWebDriverTest`, the `Cp*` page-object proxies, and `PageObjectGenerator`
on `ctrl-shift-~` twice in development mode). Two plantuml diagrams replace the
three overlapping prose descriptions of the delta mechanism: a sequence diagram of
a page load plus one round trip, and the layer stack.

Theming was left alone deliberately: it is due to change, so the page says nothing
about it.

### 2026-08-31 - The Maven archetype is gone

`archetypes/domui-hello` and its `archetypes` module are removed from `domui`, and
`getting-started/maven-archetype` (with its two 2017 screenshots) is removed from the
site; the link to it in `getting-started/index.md` went with it.

The archetype generated a single-page "hello" project against DomUI 1.1 and was never
updated afterwards - the page told the reader to run it with `-DarchetypeVersion=1.1`.
There are now two ways to start an application, and only one of them should be shown:
`domui-skeleton` is the encouraged starting point, and it is being brought to the
current stack. Keeping a second, stale bootstrap route contradicts the "one current
way" rule, so it is deleted rather than modernized.

### 2026-08-31 - `running-the-demo` rewritten; its 2017 screenshots deleted

Everything on the page was checked against the repository rather than reworded.
What was wrong and is now fixed:

- The branch section named `master`, `2.0-stable` and `1.1` as the branches that
  matter, and told the reader to `git clone -b master`. There is no `master` and
  no `2.0-stable` branch; the default branch is `skarp-master`, so a plain clone
  is what the page now shows. Only `1.1` still exists of the three.
- Java 8 as the project SDK and language level became Java 21 (`jdk.version` in
  the root pom, JDK 21 in `.github/workflows/build.yml`).
- "Maven >= 3.5" became "Maven 3.9 or newer, running on a JDK 21".
- `-Dmaven.test.skip=true` became `-DskipTests`: the root pom chains
  `skipTests` -> `domui.test.skip` -> `maven.test.skip` -> failsafe -> `jetty.skip`,
  so that one property is what actually switches off unit tests, integration
  tests and the Jetty run together.
- The manual chromedriver install (and its dead
  `sites.google.com/a/chromium.org/chromedriver` link) is gone: `WebDriverFactory`
  calls `WebDriverManager.chromedriver().setup()`, so only Chrome itself is needed.
- The IntelliJ section no longer describes the "event log -> Add as Maven project"
  dance, which modern IntelliJ does not have, and no longer blames
  `vaadin-sass-compiler` for the generated-source problem - that module is gone;
  the reason to run Maven first is stated plainly instead.
- Added: the build uses ecj rather than javac (with a link to
  `development-environment/ecj-in-maven`), the committed `.idea/compiler.xml`
  already selects it, and the committed `demo` run configuration is a Tomcat 11
  configuration deploying the exploded war at `/demo` on port 8088.
- Added: the demo needs no database setup (embedded HSQLDB with the Chinook data).

All six `image2017-11-28_*.png` screenshots were deleted rather than annotated.
Each of them showed something that no longer exists: a module tree containing
`sass-compiler [vaadin-sass-compiler]`, `jsr305`, `to.etc.db`, `to.etc.dbcompare`
and `to.etc.domui.formbuilder`; a 2017 build-success terminal; and an IntelliJ
import flow that has since changed. Phase 4 can add current screenshots if any
are worth having; none of these were.

### 2026-09-09 - The browser is asked for its colour scheme once, by redirect

The dark/light preference lives in the browser and the theme is a server side
stylesheet, so the two have to meet somewhere. Of the ways to do that, the one
chosen is a script at the top of the page head that navigates to `$colorscheme`
and comes back by 302.

Rejected, and why: **client hints** (`Accept-CH`/`Critical-CH` and
`Sec-CH-Prefers-Color-Scheme`) are the tidiest - no script at all - but Firefox
and Safari do not send them, so a large part of the users would never be asked.
**Letting the script write the cookie itself** would need the script to know the
`SameSite` spelling the container gives DomUI's cookies, which the servlet API
does not expose; and a browser that silently drops the cookie would then be asked
again on every page. **Reporting the answer on a URL parameter of the page
itself** would put a parameter into the page's own identity and leave it in the
address bar. The redirect has none of those problems: the server writes the
cookie, so it is spelled like the session cookie; the answer also lands in the
session, so one round trip ends the question whatever the browser does with
cookies; and the address bar ends up on the URL that was asked for.

Asking before the stylesheet is fetched is the point of putting the script where
it is: nothing has been painted when the browser leaves, so the user sees the
scheme they wanted rather than a flash of the other one.

### 2026-08-30 - Documentation style, and components are never fields

Two rules, settled and not to be re-litigated per page. Both are also written into the
workspace `CLAUDE.md`.

**Documentation style.** A concept is introduced with a short description, then the
example (code plus a `!demo()` where one exists), and only then the explanation of how it
works - the reader sees what is done before being told how. No forward references: a page
does not explain or lean on concepts that come later, because the goal is learning rather
than completeness. Where a picture carries the mechanism better than a paragraph, use a
```plantuml block (sequence diagram for a round trip, activity diagram for a decision
path, nested rectangles for a built node tree).

**Components are never fields.** In demo pages and in every documented example, controls
and other components are local variables of `createContent()`. `forceRebuild()` discards
the built tree and calls `createContent()` again; a component in a field survives that,
so the screen ends up showing a new component while the code still holds the old one.
State that the page rebuilds itself from - booleans, values, the edited entity - is what
belongs in fields; a handler changes those and calls `forceRebuild()`.

### 2026-08-28 - Current version and current usage only

The focus is the CURRENT version and CURRENT usage. Historic information is
removed, skipped, or changed to represent the actual and correct state. We do not
document two generations of an API side by side, and we do not preserve version
history in the documentation or the demo.

Consequences, applied throughout this plan: the `release-notes/` section goes;
"since version X" / "this used to be" asides are rewritten into plain statements
of current truth; `component2`/`form4`/`lookupinput`-era APIs are the only ones
documented and demonstrated; a page whose subject is entirely historic is deleted
rather than updated.
