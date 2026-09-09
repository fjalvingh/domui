"use strict";
var DomUIMaxGraph = (() => {
  var __defProp = Object.defineProperty;
  var __getOwnPropDesc = Object.getOwnPropertyDescriptor;
  var __getOwnPropNames = Object.getOwnPropertyNames;
  var __getOwnPropSymbols = Object.getOwnPropertySymbols;
  var __hasOwnProp = Object.prototype.hasOwnProperty;
  var __propIsEnum = Object.prototype.propertyIsEnumerable;
  var __defNormalProp = (obj, key, value) => key in obj ? __defProp(obj, key, { enumerable: true, configurable: true, writable: true, value }) : obj[key] = value;
  var __spreadValues = (a, b) => {
    for (var prop in b || (b = {}))
      if (__hasOwnProp.call(b, prop))
        __defNormalProp(a, prop, b[prop]);
    if (__getOwnPropSymbols)
      for (var prop of __getOwnPropSymbols(b)) {
        if (__propIsEnum.call(b, prop))
          __defNormalProp(a, prop, b[prop]);
      }
    return a;
  };
  var __export = (target, all) => {
    for (var name in all)
      __defProp(target, name, { get: all[name], enumerable: true });
  };
  var __copyProps = (to, from, except, desc) => {
    if (from && typeof from === "object" || typeof from === "function") {
      for (let key of __getOwnPropNames(from))
        if (!__hasOwnProp.call(to, key) && key !== except)
          __defProp(to, key, { get: () => from[key], enumerable: !(desc = __getOwnPropDesc(from, key)) || desc.enumerable });
    }
    return to;
  };
  var __toCommonJS = (mod2) => __copyProps(__defProp({}, "__esModule", { value: true }), mod2);

  // src/main/frontend/domui-maxgraph.ts
  var domui_maxgraph_exports = {};
  __export(domui_maxgraph_exports, {
    create: () => create,
    destroy: () => destroy,
    graphFor: () => graphFor
  });

  // node_modules/@maxgraph/core/lib/esm/view/image/ImageBox.js
  var ImageBox = class {
    constructor(src, width, height) {
      this.src = src;
      this.width = width;
      this.height = height;
    }
  };
  var ImageBox_default = ImageBox;

  // node_modules/@maxgraph/core/lib/esm/view/event/EventObject.js
  var EventObject = class {
    /**
     * Constructs a new event object with the specified name. An optional
     * sequence of key, value pairs can be appended to define properties.
     *
     * Example:
     *
     * ```javascript
     * new EventObject("eventName", key1, val1, .., keyN, valN)
     * ```
     *
     * @param name
     * @param args
     */
    constructor(name = "", ...args) {
      this.consumed = false;
      this.name = name;
      this.properties = {};
      if (!!args[0] && args[0].constructor === Object) {
        for (const [key, value] of Object.entries(args[0])) {
          this.properties[key] = value;
        }
      } else {
        for (let i = 0; i < args.length; i += 2) {
          if (args[i + 1] !== null) {
            this.properties[args[i]] = args[i + 1];
          }
        }
      }
    }
    /**
     * Returns <name>.
     */
    getName() {
      return this.name;
    }
    /**
     * Returns <properties>.
     */
    getProperties() {
      return this.properties;
    }
    /**
     * Returns the property for the given key.
     */
    getProperty(key) {
      return this.properties[key];
    }
    /**
     * Returns true if the event has been consumed.
     */
    isConsumed() {
      return this.consumed;
    }
    /**
     * Consumes the event.
     */
    consume() {
      this.consumed = true;
    }
  };
  var EventObject_default = EventObject;

  // node_modules/@maxgraph/core/lib/esm/view/event/EventSource.js
  var EventSource = class {
    constructor(eventSource = null) {
      this.eventListeners = [];
      this.eventsEnabled = true;
      this.eventSource = null;
      this.eventSource = eventSource;
    }
    /**
     * Returns {@link eventsEnabled}.
     */
    isEventsEnabled() {
      return this.eventsEnabled;
    }
    /**
     * Sets {@link eventsEnabled}.
     */
    setEventsEnabled(value) {
      this.eventsEnabled = value;
    }
    /**
     * Returns {@link eventSource}.
     */
    getEventSource() {
      return this.eventSource;
    }
    /**
     * Sets {@link eventSource}.
     */
    setEventSource(value) {
      this.eventSource = value;
    }
    /**
     * Binds the specified function to the given event name. If no event name
     * is given, then the listener is registered for all events.
     *
     * The parameters of the listener are the sender and an {@link EventObject}.
     */
    addListener(name, funct) {
      this.eventListeners.push({ name, funct });
    }
    /**
     * Removes all occurrences of the given listener from {@link eventListeners}.
     */
    removeListener(funct) {
      let i = 0;
      while (i < this.eventListeners.length) {
        if (this.eventListeners[i].funct === funct) {
          this.eventListeners.splice(i, 1);
        } else {
          i += 1;
        }
      }
    }
    /**
     * Dispatches the given event to the listeners which are registered for the event.
     * The sender argument is optional.
     * The current execution scope ("this") is used for the listener invocation.
     *
     * Example:
     *
     * ```javascript
     * fireEvent(new EventObject("eventName", key1, val1, .., keyN, valN))
     * ```
     *
     * @param evt {@link EventObject} that represents the event.
     * @param sender Optional sender to be passed to the listener. Default value is the return value of {@link getEventSource}.
     */
    fireEvent(evt, sender = null) {
      if (this.isEventsEnabled()) {
        if (!evt) {
          evt = new EventObject_default("");
        }
        if (!sender) {
          sender = this.getEventSource();
        }
        if (!sender) {
          sender = this;
        }
        for (const eventListener of this.eventListeners) {
          if (eventListener.name === null || eventListener.name === evt.getName()) {
            eventListener.funct.apply(this, [sender, evt]);
          }
        }
      }
    }
    /**
     * Clears all registered event listeners.
     *
     * Subclasses with a `destroy` method should call `super.destroy()` at the end
     * of their own cleanup to ensure no stale listeners remain.
     */
    destroy() {
      this.eventListeners.length = 0;
    }
  };
  var EventSource_default = EventSource;

  // node_modules/@maxgraph/core/lib/esm/util/Constants.js
  var DEFAULT_HOTSPOT = 0.3;
  var MIN_HOTSPOT_SIZE = 8;
  var MAX_HOTSPOT_SIZE = 0;
  var IDENTITY_FIELD_NAME = "mxObjectId";
  var NS_SVG = "http://www.w3.org/2000/svg";
  var NS_XLINK = "http://www.w3.org/1999/xlink";
  var SHADOWCOLOR = "gray";
  var SHADOW_OFFSET_X = 2;
  var SHADOW_OFFSET_Y = 3;
  var SHADOW_OPACITY = 1;
  var TOOLTIP_VERTICAL_OFFSET = 16;
  var DEFAULT_VALID_COLOR = "#00FF00";
  var DEFAULT_INVALID_COLOR = "#FF0000";
  var OUTLINE_HIGHLIGHT_COLOR = "#00FF00";
  var OUTLINE_HIGHLIGHT_STROKEWIDTH = 5;
  var HIGHLIGHT_STROKEWIDTH = 3;
  var HIGHLIGHT_SIZE = 2;
  var HIGHLIGHT_OPACITY = 100;
  var INVALID_CONNECT_TARGET_COLOR = "#FF0000";
  var DROP_TARGET_COLOR = "#0000FF";
  var VALID_COLOR = "#00FF00";
  var INVALID_COLOR = "#FF0000";
  var EDGE_SELECTION_COLOR = "#00FF00";
  var VERTEX_SELECTION_COLOR = "#00FF00";
  var VERTEX_SELECTION_STROKEWIDTH = 1;
  var EDGE_SELECTION_STROKEWIDTH = 1;
  var VERTEX_SELECTION_DASHED = true;
  var EDGE_SELECTION_DASHED = true;
  var GUIDE_COLOR = "#FF0000";
  var GUIDE_STROKEWIDTH = 1;
  var HANDLE_SIZE = 6;
  var LABEL_HANDLE_SIZE = 4;
  var HANDLE_FILLCOLOR = "#00FF00";
  var HANDLE_STROKECOLOR = "black";
  var LABEL_HANDLE_FILLCOLOR = "yellow";
  var CONNECT_HANDLE_FILLCOLOR = "#0000FF";
  var LOCKED_HANDLE_FILLCOLOR = "#FF0000";
  var DEFAULT_FONTFAMILY = "Arial,Helvetica,sans-serif";
  var DEFAULT_FONTSIZE = 11;
  var DEFAULT_TEXT_DIRECTION = "";
  var LINE_HEIGHT = 1.2;
  var WORD_WRAP = "normal";
  var ABSOLUTE_LINE_HEIGHT = false;
  var DEFAULT_FONTSTYLE = 0;
  var DEFAULT_STARTSIZE = 40;
  var DEFAULT_MARKERSIZE = 6;
  var DEFAULT_IMAGESIZE = 24;
  var ENTITY_SEGMENT = 30;
  var RECTANGLE_ROUNDING_FACTOR = 0.15;
  var LINE_ARCSIZE = 20;
  var ARROW_SPACING = 0;
  var ARROW_WIDTH = 30;
  var ARROW_SIZE = 30;
  var PAGE_FORMAT_A4_PORTRAIT = [0, 0, 827, 1169];
  var NONE = "none";
  var FONT_STYLE_MASK = {
    /** for bold fonts. */
    BOLD: 1,
    /** for italic fonts. */
    ITALIC: 2,
    /** for underlined fonts. */
    UNDERLINE: 4,
    /** for strikethrough fonts. */
    STRIKETHROUGH: 8
  };
  var DIRECTION_MASK = {
    /** No direction. */
    NONE: 0,
    WEST: 1,
    NORTH: 2,
    SOUTH: 4,
    EAST: 8,
    /** All directions. */
    ALL: 15
  };
  var NODE_TYPE = {
    ELEMENT: 1,
    ATTRIBUTE: 2,
    TEXT: 3,
    CDATA: 4,
    ENTITY_REFERENCE: 5,
    ENTITY: 6,
    PROCESSING_INSTRUCTION: 7,
    COMMENT: 8,
    DOCUMENT: 9,
    DOCUMENT_TYPE: 10,
    DOCUMENT_FRAGMENT: 11,
    NOTATION: 12
  };

  // node_modules/@maxgraph/core/lib/esm/Client.js
  var isWindowObjectAvailable = () => typeof window !== "undefined";
  var isUserAgentAvailable = () => !!navigator.userAgent;
  var isUserAgentIncludes = (substring) => navigator.userAgent.includes(substring);
  var isAppVersionAvailable = () => !!navigator.appVersion;
  var isAppVersionIncludes = (substring) => navigator.appVersion.includes(substring);
  var Client = class {
  };
  Client.basePath = ".";
  Client.setBasePath = (value) => {
    if (typeof value !== "undefined" && value.length > 0) {
      if (value.substring(value.length - 1) === "/") {
        value = value.substring(0, value.length - 1);
      }
      Client.basePath = value;
    } else {
      Client.basePath = ".";
    }
  };
  Client.imageBasePath = ".";
  Client.setImageBasePath = (value) => {
    if (typeof value !== "undefined" && value.length > 0) {
      if (value.substring(value.length - 1) === "/") {
        value = value.substring(0, value.length - 1);
      }
      Client.imageBasePath = value;
    } else {
      Client.imageBasePath = `${Client.basePath}/images`;
    }
  };
  Client.IS_EDGE = isWindowObjectAvailable() && isUserAgentAvailable() && !!navigator.userAgent.match(/Edge\//);
  Client.IS_NS = isWindowObjectAvailable() && isUserAgentAvailable() && isUserAgentIncludes("Mozilla/") && !isUserAgentIncludes("MSIE") && !isUserAgentIncludes("Edge/");
  Client.IS_SF = isWindowObjectAvailable() && /Apple Computer, Inc/.test(navigator.vendor);
  Client.IS_ANDROID = isWindowObjectAvailable() && isAppVersionAvailable() && isAppVersionIncludes("Android");
  Client.IS_IOS = isWindowObjectAvailable() && /iP(hone|od|ad)/.test(navigator.platform);
  Client.IS_GC = isWindowObjectAvailable() && /Google Inc/.test(navigator.vendor);
  Client.IS_CHROMEAPP = isWindowObjectAvailable() && // @ts-ignore
  window.chrome != null && // @ts-ignore
  chrome.app != null && // @ts-ignore
  chrome.app.runtime != null;
  Client.IS_FF = isUserAgentAvailable() && navigator.userAgent.toLowerCase().includes("firefox");
  Client.IS_MT = isWindowObjectAvailable() && isUserAgentAvailable() && (isUserAgentIncludes("Firefox/") && !isUserAgentIncludes("Firefox/1.") && !isUserAgentIncludes("Firefox/2.") || isUserAgentIncludes("Iceweasel/") && !isUserAgentIncludes("Iceweasel/1.") && !isUserAgentIncludes("Iceweasel/2.") || isUserAgentIncludes("SeaMonkey/") && !isUserAgentIncludes("SeaMonkey/1.") || isUserAgentIncludes("Iceape/") && !isUserAgentIncludes("Iceape/1."));
  var _a;
  Client.IS_SVG = isWindowObjectAvailable() && ((_a = navigator.appName) == null ? void 0 : _a.toUpperCase()) !== "MICROSOFT INTERNET EXPLORER";
  Client.NO_FO = isWindowObjectAvailable() && (!document.createElementNS || document.createElementNS(NS_SVG, "foreignObject").toString() !== "[object SVGForeignObjectElement]" || isUserAgentIncludes("Opera/"));
  Client.IS_WIN = isWindowObjectAvailable() && isAppVersionAvailable() && isAppVersionIncludes("Win");
  Client.IS_MAC = isWindowObjectAvailable() && isAppVersionAvailable() && isAppVersionIncludes("Mac");
  Client.IS_CHROMEOS = isWindowObjectAvailable() && /\bCrOS\b/.test(navigator.appVersion);
  Client.IS_TOUCH = isWindowObjectAvailable() && "ontouchstart" in document.documentElement;
  Client.IS_POINTER = isWindowObjectAvailable() && !!window.PointerEvent && isAppVersionAvailable() && !isAppVersionIncludes("Mac");
  Client.IS_LOCAL = isWindowObjectAvailable() && !document.location.href.includes("http://") && !document.location.href.includes("https://");
  var Client_default = Client;

  // node_modules/@maxgraph/core/lib/esm/util/EventUtils.js
  var getMainEvent = (evt) => {
    let t = evt;
    if ((t.type === "touchstart" || t.type === "touchmove") && t.touches && t.touches[0]) {
      t = t.touches[0];
    } else if (t.type === "touchend" && t.changedTouches && t.changedTouches[0]) {
      t = t.changedTouches[0];
    }
    return t;
  };
  var getClientX = (evt) => {
    return getMainEvent(evt).clientX;
  };
  var getClientY = (evt) => {
    return getMainEvent(evt).clientY;
  };
  var getSource = (evt) => {
    return evt.target;
  };
  var isConsumed = (evt) => {
    const t = evt;
    return t.isConsumed !== void 0 && t.isConsumed;
  };
  var isTouchEvent = (evt) => {
    const t = evt;
    return t.pointerType ? t.pointerType === "touch" || t.pointerType === t.MSPOINTER_TYPE_TOUCH : t.mozInputSource !== void 0 ? t.mozInputSource === 5 : t.type.indexOf("touch") === 0;
  };
  var isPenEvent = (evt) => {
    const t = evt;
    return t.pointerType ? t.pointerType == "pen" || t.pointerType === t.MSPOINTER_TYPE_PEN : t.mozInputSource !== void 0 ? t.mozInputSource === 2 : t.type.indexOf("pen") === 0;
  };
  var isMultiTouchEvent = (evt) => {
    const t = evt;
    return t.type && t.type.indexOf("touch") == 0 && t.touches !== void 0 && t.touches.length > 1;
  };
  var isMouseEvent = (evt) => {
    const t = evt;
    return t.pointerType ? t.pointerType == "mouse" || t.pointerType === t.MSPOINTER_TYPE_MOUSE : t.mozInputSource !== void 0 ? t.mozInputSource === 1 : t.type.indexOf("mouse") === 0;
  };
  var isLeftMouseButton = (evt) => {
    if ("buttons" in evt && (evt.type === "mousedown" || evt.type === "mousemove")) {
      return evt.buttons === 1;
    }
    if (evt.which !== void 0) {
      return evt.which === 1;
    }
    return evt.button === 1;
  };
  var isRightMouseButton = (evt) => {
    return evt.button === 2;
  };
  var isPopupTrigger = (evt) => {
    return isRightMouseButton(evt) || Client_default.IS_MAC && isControlDown(evt) && !isShiftDown(evt) && !isMetaDown(evt) && !isAltDown(evt);
  };
  var isShiftDown = (evt) => {
    return evt.shiftKey;
  };
  var isAltDown = (evt) => {
    return evt.altKey;
  };
  var isControlDown = (evt) => {
    return evt.ctrlKey;
  };
  var isMetaDown = (evt) => {
    return evt.metaKey;
  };

  // node_modules/@maxgraph/core/lib/esm/util/logger.js
  var NoOpLogger = class {
    debug(_message) {
    }
    enter(_message) {
      return void 0;
    }
    error(_message, ..._optionalParams) {
    }
    info(_message) {
    }
    leave(_message, _baseTimestamp) {
    }
    show() {
    }
    trace(_message) {
    }
    warn(_message) {
    }
  };

  // node_modules/@maxgraph/core/lib/esm/internal/clone-utils.js
  var shallowCopy = (source, target) => {
    for (const key in source) {
      if (Object.prototype.hasOwnProperty.call(source, key)) {
        const sourceValue = source[key];
        if (Array.isArray(sourceValue)) {
          target[key] = [...sourceValue];
        } else {
          target[key] = sourceValue;
        }
      }
    }
  };

  // node_modules/@maxgraph/core/lib/esm/i18n/provider.js
  var NoOpI18n = class {
    isEnabled() {
      return false;
    }
    get() {
      return null;
    }
    addResource() {
    }
  };

  // node_modules/@maxgraph/core/lib/esm/util/config.js
  var GlobalConfig = {
    /**
     * Configure the {@link I18nProvider} to use for all translated messages.
     *
     * Available implementations provided by maxGraph are:
     * * {@link NoOpI18n} - Default implementation that does nothing.
     * * {@link TranslationsAsI18n} - Uses {@link Translations} to manage translations.
     *
     * To change the i18n provider, set this property to an instance of the desired provider:
     * ```js
     * // To use the i18n system provided by maxGraph
     * GlobalConfig.i18n = new TranslationsAsI18n();
     * ```
     *
     * @default {@link NoOpI18n}
     * @since 0.17.0
     */
    i18n: new NoOpI18n(),
    /**
     * Configure the logger to use for all log messages.
     *
     * Available implementations provided by maxGraph are:
     * * {@link ConsoleLogger} - Directs logs to the browser console.
     * * {@link NoOpLogger} - Default implementation that does nothing.
     * * {@link MaxLogAsLogger} - Directs logs to {@link MaxLog}.
     *
     * To change the logger, set this property to an instance of the desired logger:
     * ```js
     * // To direct logs to the browser console
     * GlobalConfig.logger = new ConsoleLogger();
     * // To direct logs to MaxLog
     * GlobalConfig.logger = new MaxLogAsLogger();
     * ```
     *
     * @default {@link NoOpLogger}
     */
    logger: new NoOpLogger()
  };
  var defaultGlobalConfig = __spreadValues({}, GlobalConfig);
  var StyleDefaultsConfig = {
    /**
     * Defines the size (in px) of the arrowhead in the arrow shape.
     * @default {@link ARROW_SIZE}
     * @since 0.22.0
     */
    arrowSize: ARROW_SIZE,
    /**
     * Defines the spacing (in px) between the arrow shape and its terminals.
     * @default {@link ARROW_SPACING}
     * @since 0.22.0
     */
    arrowSpacing: ARROW_SPACING,
    /**
     * Defines the width (in px) of the arrow shape.
     * @default {@link ARROW_WIDTH}
     * @since 0.22.0
     */
    arrowWidth: ARROW_WIDTH,
    /**
     * Defines the default family for all fonts.
     * @default {@link DEFAULT_FONTFAMILY}
     * @since 0.22.0
     */
    fontFamily: DEFAULT_FONTFAMILY,
    /**
     * Defines the default size (in px).
     * @default {@link DEFAULT_FONTSIZE}
     * @since 0.22.0
     */
    fontSize: DEFAULT_FONTSIZE,
    /**
     * Defines the default width and height (in px) for images used in the label shape.
     * @default {@link DEFAULT_IMAGESIZE}
     * @since 0.22.0
     */
    imageSize: DEFAULT_IMAGESIZE,
    /**
     * Defines the default size (in px) of the arcs for the rounded edges.
     * See {@link CellStateStyle.arcSize}.
     * @default {@link LINE_ARCSIZE}
     * @since 0.22.0
     */
    lineArcSize: LINE_ARCSIZE,
    /**
     * Defines the default size (in px) for all markers.
     * @default {@link DEFAULT_MARKERSIZE}
     * @since 0.22.0
     */
    markerSize: DEFAULT_MARKERSIZE,
    /**
     * Defines the default rounding factor for the rounded vertices in percent between `0` and `1`.
     * Values should be smaller than `0.5`.
     * See {@link CellStateStyle.arcSize}.
     * @default {@link RECTANGLE_ROUNDING_FACTOR}
     * @since 0.22.0
     */
    roundingFactor: RECTANGLE_ROUNDING_FACTOR,
    /**
     * Defines the color to be used to draw shadows in shapes and windows.
     * @default {@link SHADOWCOLOR}
     */
    shadowColor: SHADOWCOLOR,
    /**
     * Specifies the x-offset of the shadow.
     * @default {@link SHADOW_OFFSET_X}
     */
    shadowOffsetX: SHADOW_OFFSET_X,
    /**
     * Specifies the y-offset of the shadow.
     * @default {@link SHADOW_OFFSET_Y}
     */
    shadowOffsetY: SHADOW_OFFSET_Y,
    /**
     * Defines the opacity for shadow. Possible values are between 1 (opaque) and 0 (transparent).
     * @default {@link SHADOW_OPACITY}
     */
    shadowOpacity: SHADOW_OPACITY,
    /**
     * Defines the default start size (in px) for swimlanes.
     * @default {@link DEFAULT_STARTSIZE}
     * @since 0.22.0
     */
    startSize: DEFAULT_STARTSIZE
  };
  var defaultStyleDefaultsConfig = __spreadValues({}, StyleDefaultsConfig);

  // node_modules/@maxgraph/core/lib/esm/internal/utils.js
  var doEval = (expression) => {
    return eval(expression);
  };
  var isElement = (node) => (node == null ? void 0 : node.nodeType) === NODE_TYPE.ELEMENT;
  var isNullish = (v) => v == void 0;
  var log = () => GlobalConfig.logger;
  var mixInto = (dest) => (mixin) => {
    const keys = Reflect.ownKeys(mixin);
    try {
      for (const key of keys) {
        Object.defineProperty(dest.prototype, key, {
          value: mixin[key],
          writable: true
          // enumerable should probably set to true.
          // For example, when exporting a Graph with Codecs, properties added via mixins are not serialized whereas properties directly defined on the class are.
        });
      }
    } catch (e) {
      log().error("Error while mixing", e);
    }
  };
  var matchBinaryMask = (value, mask) => {
    return (value & mask) === mask;
  };

  // node_modules/@maxgraph/core/lib/esm/util/domUtils.js
  var blocks = /* @__PURE__ */ new Set([
    "BLOCKQUOTE",
    "DIV",
    "H1",
    "H2",
    "H3",
    "H4",
    "H5",
    "H6",
    "OL",
    "P",
    "PRE",
    "TABLE",
    "UL"
  ]);
  var extractTextWithWhitespace = (elems) => {
    const ret = [];
    function doExtract(elts) {
      if (elts.length == 1 && (elts[0].nodeName == "BR" || elts[0].innerHTML == "\n")) {
        return;
      }
      for (let i = 0; i < elts.length; i += 1) {
        const elem = elts[i];
        if (elem.nodeName == "BR" || elem.innerHTML == "\n" || (elts.length == 1 || i == 0) && elem.nodeName == "DIV" && elem.innerHTML.toLowerCase() == "<br>") {
          ret.push("\n");
        } else {
          if (elem.nodeType === 3 || elem.nodeType === 4) {
            if (elem.nodeValue && elem.nodeValue.length > 0) {
              ret.push(elem.nodeValue);
            }
          } else if (elem.nodeType !== 8 && elem.childNodes.length > 0) {
            doExtract(Array.from(elem.childNodes));
          }
          if (i < elts.length - 1 && blocks.has(elts[i + 1].nodeName)) {
            ret.push("\n");
          }
        }
      }
    }
    doExtract(elems);
    return ret.join("");
  };
  var write = (parent, text) => {
    const doc = parent.ownerDocument;
    const node = doc.createTextNode(text);
    if (parent) {
      parent.appendChild(node);
    }
    return node;
  };
  var isNode = (value, nodeName = null, attributeName, attributeValue) => {
    if (!isNullish(value) && typeof value.nodeType === "number" && (isNullish(nodeName) || value.nodeName.toLowerCase() == nodeName.toLowerCase())) {
      return isNullish(attributeName) || value.getAttribute(attributeName) == attributeValue;
    }
    return false;
  };
  var isAncestorNode = (ancestor, child) => {
    let parent = child;
    while (parent != null) {
      if (parent === ancestor) {
        return true;
      }
      parent = parent.parentNode;
    }
    return false;
  };
  var clearSelection = () => {
    const sel = window.getSelection ? window.getSelection() : document.selection;
    if (sel) {
      if (sel.removeAllRanges) {
        sel.removeAllRanges();
      } else if (sel.empty) {
        sel.empty();
      }
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/event/InternalMouseEvent.js
  var InternalMouseEvent = class {
    /**
     * Constructs a new event object for the given arguments.
     *
     * @param evt Native mouse event.
     * @param state Optional {@link CellState} under the mouse.
     */
    constructor(evt, state = null) {
      this.consumed = false;
      this.evt = evt;
      this.state = state;
      this.sourceState = state;
      this.graphX = 0;
      this.graphY = 0;
    }
    /**
     * Returns <evt>.
     */
    getEvent() {
      return this.evt;
    }
    /**
     * Returns the target DOM element using {@link Event#getSource} for <evt>.
     */
    getSource() {
      return getSource(this.evt);
    }
    /**
     * Returns true if the given {@link Shape} is the source of <evt>.
     */
    isSource(shape) {
      return shape ? isAncestorNode(shape.node, this.getSource()) : false;
    }
    /**
     * Returns <evt.clientX>.
     */
    getX() {
      return getClientX(this.getEvent());
    }
    /**
     * Returns <evt.clientY>.
     */
    getY() {
      return getClientY(this.getEvent());
    }
    /**
     * Returns <graphX>.
     */
    getGraphX() {
      return this.graphX;
    }
    /**
     * Returns <graphY>.
     */
    getGraphY() {
      return this.graphY;
    }
    /**
     * Returns <state>.
     */
    getState() {
      return this.state;
    }
    /**
     * Returns the <Cell> in <state> is not null.
     */
    getCell() {
      const state = this.getState();
      return state ? state.cell : null;
    }
    /**
     * Returns true if the event is a popup trigger.
     */
    isPopupTrigger() {
      return isPopupTrigger(this.getEvent());
    }
    /**
     * Returns <consumed>.
     */
    isConsumed() {
      return this.consumed;
    }
    /**
     * Sets <consumed> to true and invokes preventDefault on the native event
     * if such a method is defined. This is used mainly to avoid the cursor from
     * being changed to a text cursor in Webkit. You can use the preventDefault
     * flag to disable this functionality.
     *
     * @param preventDefault Specifies if the native event should be canceled. Default
     * is true.
     */
    consume(preventDefault) {
      preventDefault = preventDefault ? preventDefault : window.TouchEvent && this.evt instanceof TouchEvent || isMouseEvent(this.evt);
      if (preventDefault && this.evt.preventDefault) {
        this.evt.preventDefault();
      }
      this.consumed = true;
    }
  };
  var InternalMouseEvent_default = InternalMouseEvent;

  // node_modules/@maxgraph/core/lib/esm/view/event/InternalEvent.js
  var supportsPassive = false;
  try {
    document.addEventListener("test", () => {
      return;
    }, Object.defineProperty && Object.defineProperty({}, "passive", {
      get: () => {
        supportsPassive = true;
      }
    }));
  } catch (e) {
  }
  var InternalEvent = class _InternalEvent {
    /**
     * Binds the function to the specified event on the given element.
     */
    static addListener(element, eventName, funct) {
      element.addEventListener(eventName, funct, supportsPassive ? { passive: false } : false);
      if (!element.mxListenerList) {
        element.mxListenerList = [];
      }
      const entry = { name: eventName, f: funct };
      element.mxListenerList.push(entry);
    }
    /**
     * Removes the specified listener from the given element.
     */
    static removeListener(element, eventName, funct) {
      element.removeEventListener(eventName, funct, false);
      if (element.mxListenerList) {
        const listenerCount = element.mxListenerList.length;
        for (let i = 0; i < listenerCount; i += 1) {
          const entry = element.mxListenerList[i];
          if (entry.f === funct) {
            element.mxListenerList.splice(i, 1);
            break;
          }
        }
      }
    }
    /**
     * Removes all listeners from the given element.
     */
    static removeAllListeners(element) {
      const list = element.mxListenerList;
      if (list) {
        while (list.length > 0) {
          const entry = list[0];
          _InternalEvent.removeListener(element, entry.name, entry.f);
        }
      }
    }
    /**
     * Adds the given listeners for touch, mouse and/or pointer events. If
     * <Client.IS_POINTER> is true then pointer events will be registered,
     * else the respective mouse events will be registered. If <Client.IS_POINTER>
     * is false and <Client.IS_TOUCH> is true then the respective touch events
     * will be registered as well as the mouse events.
     */
    static addGestureListeners(node, startListener = null, moveListener = null, endListener = null) {
      if (startListener) {
        _InternalEvent.addListener(node, Client_default.IS_POINTER ? "pointerdown" : "mousedown", startListener);
      }
      if (moveListener) {
        _InternalEvent.addListener(node, Client_default.IS_POINTER ? "pointermove" : "mousemove", moveListener);
      }
      if (endListener) {
        _InternalEvent.addListener(node, Client_default.IS_POINTER ? "pointerup" : "mouseup", endListener);
      }
      if (!Client_default.IS_POINTER && Client_default.IS_TOUCH) {
        if (startListener) {
          _InternalEvent.addListener(node, "touchstart", startListener);
        }
        if (moveListener) {
          _InternalEvent.addListener(node, "touchmove", moveListener);
        }
        if (endListener) {
          _InternalEvent.addListener(node, "touchend", endListener);
        }
      }
    }
    /**
     * Removes the given listeners from mousedown, mousemove, mouseup and the
     * respective touch events if <Client.IS_TOUCH> is true.
     */
    static removeGestureListeners(node, startListener, moveListener, endListener) {
      if (startListener) {
        _InternalEvent.removeListener(node, Client_default.IS_POINTER ? "pointerdown" : "mousedown", startListener);
      }
      if (moveListener) {
        _InternalEvent.removeListener(node, Client_default.IS_POINTER ? "pointermove" : "mousemove", moveListener);
      }
      if (endListener) {
        _InternalEvent.removeListener(node, Client_default.IS_POINTER ? "pointerup" : "mouseup", endListener);
      }
      if (!Client_default.IS_POINTER && Client_default.IS_TOUCH) {
        if (startListener) {
          _InternalEvent.removeListener(node, "touchstart", startListener);
        }
        if (moveListener) {
          _InternalEvent.removeListener(node, "touchmove", moveListener);
        }
        if (endListener) {
          _InternalEvent.removeListener(node, "touchend", endListener);
        }
      }
    }
    /**
     * Redirects the mouse events from the given DOM node to the graph dispatch
     * loop using the event and given state as event arguments. State can
     * either be an instance of <CellState> or a function that returns an
     * <CellState>. The down, move, up and dblClick arguments are optional
     * functions that take the trigger event as arguments and replace the
     * default behaviour.
     */
    static redirectMouseEvents(node, graph, state = null, down = null, move = null, up = null, dblClick = null) {
      const getState = (evt) => {
        return typeof state === "function" ? state(evt) : state;
      };
      _InternalEvent.addGestureListeners(node, (evt) => {
        if (down) {
          down(evt);
        } else if (!isConsumed(evt)) {
          graph.fireMouseEvent(_InternalEvent.MOUSE_DOWN, new InternalMouseEvent_default(evt, getState(evt)));
        }
      }, (evt) => {
        if (move) {
          move(evt);
        } else if (!isConsumed(evt)) {
          graph.fireMouseEvent(_InternalEvent.MOUSE_MOVE, new InternalMouseEvent_default(evt, getState(evt)));
        }
      }, (evt) => {
        if (up) {
          up(evt);
        } else if (!isConsumed(evt)) {
          graph.fireMouseEvent(_InternalEvent.MOUSE_UP, new InternalMouseEvent_default(evt, getState(evt)));
        }
      });
      _InternalEvent.addListener(node, "dblclick", (evt) => {
        if (dblClick) {
          dblClick(evt);
        } else if (!isConsumed(evt)) {
          const tmp = getState(evt);
          graph.dblClick(evt, tmp == null ? void 0 : tmp.cell);
        }
      });
    }
    /**
     * Removes the known listeners from the given DOM node and its descendants.
     *
     * @param element DOM node to remove the listeners from.
     */
    static release(element) {
      try {
        _InternalEvent.removeAllListeners(element);
        const children = element.childNodes;
        if (children !== void 0) {
          const childCount = children.length;
          for (let i = 0; i < childCount; i += 1) {
            _InternalEvent.release(children[i]);
          }
        }
      } catch (e) {
      }
    }
    /**
     * Installs the given function as a handler for mouse wheel events. The
     * function has two arguments: the mouse event and a boolean that specifies
     * if the wheel was moved up or down.
     *
     * This has been tested with IE 6 and 7, Firefox (all versions), Opera and
     * Safari. It does currently not work on Safari for Mac.
     *
     * ### Example
     *
     * @example
     * ```javascript
     * mxEvent.addMouseWheelListener(function (evt, up)
     * {
     *   GlobalConfig.logger.show();
     *   GlobalConfig.logger.debug('mouseWheel: up='+up);
     * });
     * ```
     *
     * @param funct Handler function that takes the event argument and a boolean up
     * argument for the mousewheel direction.
     * @param target Target for installing the listener in Google Chrome. See
     * https://www.chromestatus.com/features/6662647093133312.
     */
    static addMouseWheelListener(funct, target) {
      if (funct != null) {
        const wheelHandler = (evt) => {
          if (evt.ctrlKey) {
            evt.preventDefault();
          }
          if (Math.abs(evt.deltaX) > 0.5 || Math.abs(evt.deltaY) > 0.5) {
            funct(evt, evt.deltaY == 0 ? -evt.deltaX > 0 : -evt.deltaY > 0);
          }
        };
        target = target != null ? target : window;
        if (Client_default.IS_SF && !Client_default.IS_TOUCH) {
          let scale = 1;
          _InternalEvent.addListener(target, "gesturestart", (evt) => {
            _InternalEvent.consume(evt);
            scale = 1;
          });
          _InternalEvent.addListener(target, "gesturechange", ((evt) => {
            _InternalEvent.consume(evt);
            if (typeof evt.scale === "number") {
              const diff = scale - evt.scale;
              if (Math.abs(diff) > 0.2) {
                funct(evt, diff < 0, true);
                scale = evt.scale;
              }
            }
          }));
          _InternalEvent.addListener(target, "gestureend", (evt) => {
            _InternalEvent.consume(evt);
          });
        } else {
          let evtCache = [];
          let dx0 = 0;
          let dy0 = 0;
          _InternalEvent.addGestureListeners(target, ((evt) => {
            if (!isMouseEvent(evt) && evt.pointerId != null) {
              evtCache.push(evt);
            }
          }), ((evt) => {
            if (!isMouseEvent(evt) && evtCache.length == 2) {
              for (let i = 0; i < evtCache.length; i += 1) {
                if (evt.pointerId == evtCache[i].pointerId) {
                  evtCache[i] = evt;
                  break;
                }
              }
              const dx = Math.abs(evtCache[0].clientX - evtCache[1].clientX);
              const dy = Math.abs(evtCache[0].clientY - evtCache[1].clientY);
              const tx = Math.abs(dx - dx0);
              const ty = Math.abs(dy - dy0);
              if (tx > _InternalEvent.PINCH_THRESHOLD || ty > _InternalEvent.PINCH_THRESHOLD) {
                const cx = evtCache[0].clientX + (evtCache[1].clientX - evtCache[0].clientX) / 2;
                const cy = evtCache[0].clientY + (evtCache[1].clientY - evtCache[0].clientY) / 2;
                funct(evtCache[0], tx > ty ? dx > dx0 : dy > dy0, true, cx, cy);
                dx0 = dx;
                dy0 = dy;
              }
            }
          }), (evt) => {
            evtCache = [];
            dx0 = 0;
            dy0 = 0;
          });
        }
        _InternalEvent.addListener(target, "wheel", wheelHandler);
      }
    }
    /**
     * Disables the context menu for the given element.
     */
    static disableContextMenu(element) {
      _InternalEvent.addListener(element, "contextmenu", (evt) => {
        if (evt.preventDefault) {
          evt.preventDefault();
        }
        return false;
      });
    }
    /**
     * Consumes the given event.
     *
     * @param evt Native event to be consumed.
     * @param {boolean} [preventDefault=true] Optional boolean to prevent the default for the event.
     * Default is true.
     * @param {boolean} [stopPropagation=true] Option boolean to stop event propagation. Default is
     * true.
     */
    static consume(evt, preventDefault = true, stopPropagation = true) {
      if (preventDefault) {
        if (evt.preventDefault) {
          if (stopPropagation) {
            evt.stopPropagation();
          }
          evt.preventDefault();
        } else if (stopPropagation) {
          evt.cancelBubble = true;
        }
      }
      evt.isConsumed = true;
      if (!evt.preventDefault) {
        evt.returnValue = false;
      }
    }
  };
  InternalEvent.LABEL_HANDLE = -1;
  InternalEvent.ROTATION_HANDLE = -2;
  InternalEvent.CUSTOM_HANDLE = -100;
  InternalEvent.VIRTUAL_HANDLE = -1e5;
  InternalEvent.MOUSE_DOWN = "mouseDown";
  InternalEvent.MOUSE_MOVE = "mouseMove";
  InternalEvent.MOUSE_UP = "mouseUp";
  InternalEvent.ACTIVATE = "activate";
  InternalEvent.RESIZE_START = "resizeStart";
  InternalEvent.RESIZE = "resize";
  InternalEvent.RESIZE_END = "resizeEnd";
  InternalEvent.MOVE_START = "moveStart";
  InternalEvent.MOVE = "move";
  InternalEvent.MOVE_END = "moveEnd";
  InternalEvent.PAN_START = "panStart";
  InternalEvent.PAN = "pan";
  InternalEvent.PAN_END = "panEnd";
  InternalEvent.MINIMIZE = "minimize";
  InternalEvent.NORMALIZE = "normalize";
  InternalEvent.MAXIMIZE = "maximize";
  InternalEvent.HIDE = "hide";
  InternalEvent.SHOW = "show";
  InternalEvent.CLOSE = "close";
  InternalEvent.DESTROY = "destroy";
  InternalEvent.REFRESH = "refresh";
  InternalEvent.SIZE = "size";
  InternalEvent.SELECT = "select";
  InternalEvent.FIRED = "fired";
  InternalEvent.FIRE_MOUSE_EVENT = "fireMouseEvent";
  InternalEvent.GESTURE = "gesture";
  InternalEvent.TAP_AND_HOLD = "tapAndHold";
  InternalEvent.GET = "get";
  InternalEvent.RECEIVE = "receive";
  InternalEvent.CONNECT = "connect";
  InternalEvent.DISCONNECT = "disconnect";
  InternalEvent.SUSPEND = "suspend";
  InternalEvent.RESUME = "resume";
  InternalEvent.MARK = "mark";
  InternalEvent.ROOT = "root";
  InternalEvent.POST = "post";
  InternalEvent.OPEN = "open";
  InternalEvent.SAVE = "save";
  InternalEvent.BEFORE_ADD_VERTEX = "beforeAddVertex";
  InternalEvent.ADD_VERTEX = "addVertex";
  InternalEvent.AFTER_ADD_VERTEX = "afterAddVertex";
  InternalEvent.DONE = "done";
  InternalEvent.EXECUTE = "execute";
  InternalEvent.EXECUTED = "executed";
  InternalEvent.BEGIN_UPDATE = "beginUpdate";
  InternalEvent.START_EDIT = "startEdit";
  InternalEvent.END_UPDATE = "endUpdate";
  InternalEvent.END_EDIT = "endEdit";
  InternalEvent.BEFORE_UNDO = "beforeUndo";
  InternalEvent.UNDO = "undo";
  InternalEvent.REDO = "redo";
  InternalEvent.CHANGE = "change";
  InternalEvent.NOTIFY = "notify";
  InternalEvent.LAYOUT_CELLS = "layoutCells";
  InternalEvent.CLICK = "click";
  InternalEvent.SCALE = "scale";
  InternalEvent.TRANSLATE = "translate";
  InternalEvent.SCALE_AND_TRANSLATE = "scaleAndTranslate";
  InternalEvent.UP = "up";
  InternalEvent.DOWN = "down";
  InternalEvent.ADD = "add";
  InternalEvent.REMOVE = "remove";
  InternalEvent.CLEAR = "clear";
  InternalEvent.ADD_CELLS = "addCells";
  InternalEvent.CELLS_ADDED = "cellsAdded";
  InternalEvent.MOVE_CELLS = "moveCells";
  InternalEvent.CELLS_MOVED = "cellsMoved";
  InternalEvent.RESIZE_CELLS = "resizeCells";
  InternalEvent.CELLS_RESIZED = "cellsResized";
  InternalEvent.TOGGLE_CELLS = "toggleCells";
  InternalEvent.CELLS_TOGGLED = "cellsToggled";
  InternalEvent.ORDER_CELLS = "orderCells";
  InternalEvent.CELLS_ORDERED = "cellsOrdered";
  InternalEvent.REMOVE_CELLS = "removeCells";
  InternalEvent.CELLS_REMOVED = "cellsRemoved";
  InternalEvent.GROUP_CELLS = "groupCells";
  InternalEvent.UNGROUP_CELLS = "ungroupCells";
  InternalEvent.REMOVE_CELLS_FROM_PARENT = "removeCellsFromParent";
  InternalEvent.FOLD_CELLS = "foldCells";
  InternalEvent.CELLS_FOLDED = "cellsFolded";
  InternalEvent.ALIGN_CELLS = "alignCells";
  InternalEvent.LABEL_CHANGED = "labelChanged";
  InternalEvent.CONNECT_CELL = "connectCell";
  InternalEvent.CELL_CONNECTED = "cellConnected";
  InternalEvent.SPLIT_EDGE = "splitEdge";
  InternalEvent.FLIP_EDGE = "flipEdge";
  InternalEvent.START_EDITING = "startEditing";
  InternalEvent.EDITING_STARTED = "editingStarted";
  InternalEvent.EDITING_STOPPED = "editingStopped";
  InternalEvent.ADD_OVERLAY = "addOverlay";
  InternalEvent.REMOVE_OVERLAY = "removeOverlay";
  InternalEvent.UPDATE_CELL_SIZE = "updateCellSize";
  InternalEvent.ESCAPE = "escape";
  InternalEvent.DOUBLE_CLICK = "doubleClick";
  InternalEvent.START = "start";
  InternalEvent.RESET = "reset";
  InternalEvent.PINCH_THRESHOLD = 10;
  var InternalEvent_default = InternalEvent;

  // node_modules/@maxgraph/core/lib/esm/view/geometry/Point.js
  var Point = class _Point {
    /**
     * Constructs a new point for the optional x and y coordinates.
     *
     * @param x - The x-coordinate (default is 0).
     * @param y - The y-coordinate (default is 0).
     */
    constructor(x = 0, y = 0) {
      this._x = 0;
      this._y = 0;
      this.x = x;
      this.y = y;
    }
    get x() {
      return this._x;
    }
    set x(x) {
      if (Number.isNaN(x))
        throw new Error("Invalid x supplied.");
      this._x = x;
    }
    get y() {
      return this._y;
    }
    set y(y) {
      if (Number.isNaN(y))
        throw new Error("Invalid y supplied.");
      this._y = y;
    }
    /**
     * Returns true if the given object equals this point.
     */
    equals(p) {
      if (!p)
        return false;
      return p.x === this.x && p.y === this.y;
    }
    /**
     * Returns a clone of this {@link Point}.
     */
    clone() {
      return new _Point(this.x, this.y);
    }
  };
  var Point_default = Point;

  // node_modules/@maxgraph/core/lib/esm/view/geometry/Rectangle.js
  var Rectangle = class _Rectangle extends Point_default {
    /**
     * Constructs a new rectangle for the optional parameters.
     */
    constructor(x = 0, y = 0, width = 0, height = 0) {
      super(x, y);
      this._width = 0;
      this._height = 0;
      this.width = width;
      this.height = height;
    }
    get width() {
      return this._width;
    }
    set width(width) {
      if (Number.isNaN(width))
        throw new Error("Invalid width supplied.");
      this._width = width;
    }
    get height() {
      return this._height;
    }
    set height(height) {
      if (Number.isNaN(height))
        throw new Error("Invalid height supplied.");
      this._height = height;
    }
    /**
     * Sets this rectangle to the specified values
     */
    setRect(x, y, width, height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
    }
    /**
     * Returns the x-coordinate of the center point.
     */
    getCenterX() {
      return this.x + this.width / 2;
    }
    /**
     * Returns the y-coordinate of the center point.
     */
    getCenterY() {
      return this.y + this.height / 2;
    }
    /**
     * Adds the given rectangle to this rectangle.
     */
    add(rect) {
      const minX = Math.min(this.x, rect.x);
      const minY = Math.min(this.y, rect.y);
      const maxX = Math.max(this.x + this.width, rect.x + rect.width);
      const maxY = Math.max(this.y + this.height, rect.y + rect.height);
      this.x = minX;
      this.y = minY;
      this.width = maxX - minX;
      this.height = maxY - minY;
    }
    /**
     * Changes this rectangle to where it overlaps with the given rectangle.
     */
    intersect(rect) {
      const r1 = this.x + this.width;
      const r2 = rect.x + rect.width;
      const b1 = this.y + this.height;
      const b2 = rect.y + rect.height;
      this.x = Math.max(this.x, rect.x);
      this.y = Math.max(this.y, rect.y);
      this.width = Math.min(r1, r2) - this.x;
      this.height = Math.min(b1, b2) - this.y;
    }
    /**
     * Grows the rectangle by the given amount, that is, this method subtracts
     * the given amount from the x- and y-coordinates and adds twice the amount
     * to the width and height.
     */
    grow(amount) {
      this.x -= amount;
      this.y -= amount;
      this.width += 2 * amount;
      this.height += 2 * amount;
    }
    /**
     * Returns the top, left corner as a new {@link Point}.
     */
    getPoint() {
      return new Point_default(this.x, this.y);
    }
    /**
     * Rotates this rectangle by 90 degree around its center point.
     */
    rotate90() {
      const t = (this.width - this.height) / 2;
      this.x += t;
      this.y -= t;
      const tmp = this.width;
      this.width = this.height;
      this.height = tmp;
    }
    /**
     * Returns true if the given object equals this rectangle.
     */
    equals(rect) {
      if (!rect)
        return false;
      return rect.x === this.x && rect.y === this.y && rect.width === this.width && rect.height === this.height;
    }
    clone() {
      return new _Rectangle(this.x, this.y, this.width, this.height);
    }
  };
  Rectangle.fromRectangle = (rect) => {
    return new Rectangle(rect.x, rect.y, rect.width, rect.height);
  };
  var Rectangle_default = Rectangle;

  // node_modules/@maxgraph/core/lib/esm/view/cell/CellPath.js
  var CellPath = class _CellPath {
    constructor() {
      throw new Error("Static class can't be instantiated!");
    }
    /**
     * Creates the cell path for the given cell. The cell path is a
     * concatenation of the indices of all ancestors on the (finite) path to
     * the root, eg. "0.0.0.1".
     *
     * @param cell Cell whose path should be returned.
     */
    static create(cell) {
      let result = "";
      let parent = cell.getParent();
      while (parent) {
        const index = parent.getIndex(cell);
        result = index + _CellPath.PATH_SEPARATOR + result;
        cell = parent;
        parent = cell.getParent();
      }
      const n = result.length;
      if (n > 1) {
        result = result.substring(0, n - 1);
      }
      return result;
    }
    /**
     * Returns the path for the parent of the cell represented by the given
     * path. Returns null if the given path has no parent.
     *
     * @param path Path whose parent path should be returned.
     */
    static getParentPath(path) {
      const index = path.lastIndexOf(_CellPath.PATH_SEPARATOR);
      if (index >= 0) {
        return path.substring(0, index);
      }
      if (path.length > 0) {
        return "";
      }
      return null;
    }
    /**
     * Returns the cell for the specified cell path using the given root as the
     * root of the path.
     *
     * @param root Root cell of the path to be resolved.
     * @param path String that defines the path.
     */
    static resolve(root, path) {
      let parent = root;
      const tokens = path.split(_CellPath.PATH_SEPARATOR);
      for (let i = 0; i < tokens.length; i += 1) {
        parent = parent.getChildAt(Number.parseInt(tokens[i]));
      }
      return parent;
    }
    /**
     * Compares the given cell paths and returns -1 if p1 is smaller, 0 if
     * p1 is equal and 1 if p1 is greater than p2.
     */
    static compare(p1, p2) {
      const min = Math.min(p1.length, p2.length);
      let comp = 0;
      for (let i = 0; i < min; i += 1) {
        if (p1[i] !== p2[i]) {
          if (p1[i].length === 0 || p2[i].length === 0) {
            comp = p1[i] === p2[i] ? 0 : p1[i] > p2[i] ? 1 : -1;
          } else {
            const t1 = Number.parseInt(p1[i]);
            const t2 = Number.parseInt(p2[i]);
            comp = t1 === t2 ? 0 : t1 > t2 ? 1 : -1;
          }
          break;
        }
      }
      if (comp === 0) {
        const t1 = p1.length;
        const t2 = p2.length;
        if (t1 !== t2) {
          comp = t1 > t2 ? 1 : -1;
        }
      }
      return comp;
    }
  };
  CellPath.PATH_SEPARATOR = ".";
  var CellPath_default = CellPath;

  // node_modules/@maxgraph/core/lib/esm/util/styleUtils.js
  var getCurrentStyle = (element) => {
    return !element || element.toString() === "[object ShadowRoot]" ? null : window.getComputedStyle(element, "");
  };
  var parseCssNumber = (value) => {
    switch (value) {
      case "thin": {
        value = "2";
        break;
      }
      case "medium": {
        value = "4";
        break;
      }
      case "thick": {
        value = "6";
        break;
      }
    }
    let n = Number.parseFloat(value);
    if (Number.isNaN(n)) {
      n = 0;
    }
    return n;
  };
  var setPrefixedStyle = (style, name, value) => {
    let prefix = null;
    if (Client_default.IS_SF || Client_default.IS_GC) {
      prefix = "Webkit";
    } else if (Client_default.IS_MT) {
      prefix = "Moz";
    }
    style.setProperty(name, value);
    if (prefix !== null && name.length > 0) {
      name = prefix + name.substring(0, 1).toUpperCase() + name.substring(1);
      style.setProperty(name, value);
    }
  };
  var hasScrollbars = (node) => {
    const style = getCurrentStyle(node);
    return !!style && (style.overflow === "scroll" || style.overflow === "auto");
  };
  var getDocumentSize = () => {
    var _a2, _b;
    const b = document.body;
    const d = document.documentElement;
    try {
      return new Rectangle_default(0, 0, (_a2 = b.clientWidth) != null ? _a2 : d.clientWidth, Math.max((_b = b.clientHeight) != null ? _b : 0, d.clientHeight));
    } catch (e) {
      return new Rectangle_default();
    }
  };
  var fit = (node) => {
    const ds = getDocumentSize();
    const left = node.offsetLeft;
    const width = node.offsetWidth;
    const offset = getDocumentScrollOrigin(node.ownerDocument);
    const sl = offset.x;
    const st = offset.y;
    const right = sl + ds.width;
    if (left + width > right) {
      node.style.left = `${Math.max(sl, right - width)}px`;
    }
    const top = node.offsetTop;
    const height = node.offsetHeight;
    const bottom = st + ds.height;
    if (top + height > bottom) {
      node.style.top = `${Math.max(st, bottom - height)}px`;
    }
  };
  var getOffset = (container, scrollOffset = false) => {
    let offsetLeft = 0;
    let offsetTop = 0;
    let fixed = false;
    let node = container;
    const b = document.body;
    const d = document.documentElement;
    while (node != null && node != b && node != d && !fixed) {
      const style = getCurrentStyle(node);
      if (style != null) {
        fixed = fixed || style.position == "fixed";
      }
      node = node.parentNode;
    }
    if (!scrollOffset && !fixed) {
      const offset = getDocumentScrollOrigin(container.ownerDocument);
      offsetLeft += offset.x;
      offsetTop += offset.y;
    }
    const r = container.getBoundingClientRect();
    if (r != null) {
      offsetLeft += r.left;
      offsetTop += r.top;
    }
    return new Point_default(offsetLeft, offsetTop);
  };
  var getDocumentScrollOrigin = (doc) => {
    const wnd = doc.defaultView || doc.parentWindow;
    const x = wnd != null && window.pageXOffset !== void 0 ? window.pageXOffset : (document.documentElement || document.body.parentNode || document.body).scrollLeft;
    const y = wnd != null && window.pageYOffset !== void 0 ? window.pageYOffset : (document.documentElement || document.body.parentNode || document.body).scrollTop;
    return new Point_default(x, y);
  };
  var getScrollOrigin = (node = null, includeAncestors = false, includeDocument = true) => {
    const doc = node != null ? node.ownerDocument : document;
    const b = doc.body;
    const d = doc.documentElement;
    const result = new Point_default();
    let fixed = false;
    while (node != null && node != b && node != d) {
      if (!Number.isNaN(node.scrollLeft) && !Number.isNaN(node.scrollTop)) {
        result.x += node.scrollLeft;
        result.y += node.scrollTop;
      }
      const style = getCurrentStyle(node);
      if (style != null) {
        fixed = fixed || style.position == "fixed";
      }
      node = includeAncestors ? node.parentNode : null;
    }
    if (!fixed && includeDocument) {
      const origin = getDocumentScrollOrigin(doc);
      result.x += origin.x;
      result.y += origin.y;
    }
    return result;
  };
  var convertPoint = (container, x, y) => {
    const origin = getScrollOrigin(container, false);
    const offset = getOffset(container);
    offset.x -= origin.x;
    offset.y -= origin.y;
    return new Point_default(x - offset.x, y - offset.y);
  };
  var setCellStyles = (model, cells, key, value) => {
    if (cells.length > 0) {
      model.batchUpdate(() => {
        for (let i = 0; i < cells.length; i += 1) {
          const cell = cells[i];
          if (cell) {
            const style = cell.getClonedStyle();
            style[key] = value;
            model.setStyle(cell, style);
          }
        }
      });
    }
  };
  var setCellStyleFlags = (model, cells, key, flag, value) => {
    if (cells.length > 0) {
      model.batchUpdate(() => {
        for (let i = 0; i < cells.length; i += 1) {
          const cell = cells[i];
          if (cell) {
            const style = setStyleFlag(cell.getClonedStyle(), key, flag, value);
            model.setStyle(cell, style);
          }
        }
      });
    }
  };
  var setStyleFlag = (style, key, flag, value) => {
    const v = style[key];
    if (v === void 0) {
      style[key] = value === void 0 || value ? flag : 0;
    } else {
      if (value === void 0) {
        style[key] = v ^ flag;
      } else if (value) {
        style[key] = v | flag;
      } else {
        style[key] = v & ~flag;
      }
    }
    return style;
  };
  var setOpacity = (node, value) => {
    node.style.opacity = String(value / 100);
  };
  var getSizeForString = (text, fontSize = StyleDefaultsConfig.fontSize, fontFamily = StyleDefaultsConfig.fontFamily, textWidth = null, fontStyle = null) => {
    const div = document.createElement("div");
    div.style.fontFamily = fontFamily;
    div.style.fontSize = `${Math.round(fontSize)}px`;
    div.style.lineHeight = `${Math.round(fontSize * LINE_HEIGHT)}px`;
    if (fontStyle !== null) {
      matchBinaryMask(fontStyle, FONT_STYLE_MASK.BOLD) && (div.style.fontWeight = "bold");
      matchBinaryMask(fontStyle, FONT_STYLE_MASK.ITALIC) && (div.style.fontStyle = "italic");
      const txtDecor = [];
      matchBinaryMask(fontStyle, FONT_STYLE_MASK.UNDERLINE) && txtDecor.push("underline");
      matchBinaryMask(fontStyle, FONT_STYLE_MASK.STRIKETHROUGH) && txtDecor.push("line-through");
      txtDecor.length > 0 && (div.style.textDecoration = txtDecor.join(" "));
    }
    div.style.position = "absolute";
    div.style.visibility = "hidden";
    div.style.display = "inline-block";
    if (textWidth !== null) {
      div.style.width = `${textWidth}px`;
      div.style.whiteSpace = "normal";
    } else {
      div.style.whiteSpace = "nowrap";
    }
    div.innerHTML = text;
    document.body.appendChild(div);
    const size = new Rectangle_default(0, 0, div.offsetWidth, div.offsetHeight);
    document.body.removeChild(div);
    return size;
  };
  var sortCells = (cells, ascending = true) => {
    const lookup = /* @__PURE__ */ new Map();
    cells.sort((o1, o2) => {
      let p1 = lookup.get(o1);
      if (p1 == null) {
        p1 = CellPath_default.create(o1).split(CellPath_default.PATH_SEPARATOR);
        lookup.set(o1, p1);
      }
      let p2 = lookup.get(o2);
      if (p2 == null) {
        p2 = CellPath_default.create(o2).split(CellPath_default.PATH_SEPARATOR);
        lookup.set(o2, p2);
      }
      const comp = CellPath_default.compare(p1, p2);
      return comp == 0 ? 0 : comp > 0 == ascending ? 1 : -1;
    });
    return cells;
  };
  var getAlignmentAsPoint = (align, valign) => {
    let dx = -0.5;
    let dy = -0.5;
    if (align === "left") {
      dx = 0;
    } else if (align === "right") {
      dx = -1;
    }
    if (valign === "top") {
      dy = 0;
    } else if (valign === "bottom") {
      dy = -1;
    }
    return new Point_default(dx, dy);
  };

  // node_modules/@maxgraph/core/lib/esm/util/StringUtils.js
  var ltrim = (str, chars = "\\s") => str != null ? str.replace(new RegExp(`^[${chars}]+`, "g"), "") : null;
  var rtrim = (str, chars = "\\s") => str != null ? str.replace(new RegExp(`[${chars}]+$`, "g"), "") : null;
  var trim = (str, chars) => ltrim(rtrim(str, chars), chars);
  var getFunctionName = (f) => {
    let str = null;
    if (f != null) {
      if (f.name != null) {
        str = f.name;
      } else {
        str = trim(f.toString());
        if (str !== null && /^function\s/.test(str)) {
          str = ltrim(str.substring(9));
          if (str !== null) {
            const idx2 = str.indexOf("(");
            if (idx2 > 0) {
              str = str.substring(0, idx2);
            }
          }
        }
      }
    }
    return str;
  };
  var replaceTrailingNewlines = (str, pattern) => {
    let postfix = "";
    while (str.length > 0 && str.charAt(str.length - 1) == "\n") {
      str = str.substring(0, str.length - 1);
      postfix += pattern;
    }
    return str + postfix;
  };
  var htmlEntities = (s, newline = true) => {
    s = String(s || "");
    s = s.replace(/&/g, "&amp;");
    s = s.replace(/"/g, "&quot;");
    s = s.replace(/'/g, "&#39;");
    s = s.replace(/</g, "&lt;");
    s = s.replace(/>/g, "&gt;");
    if (newline) {
      s = s.replace(/\n/g, "&#xa;");
    }
    return s;
  };

  // node_modules/@maxgraph/core/lib/esm/util/ObjectIdentity.js
  var ObjectIdentity = class _ObjectIdentity {
    /**
     * Returns the ID for the given object or function.
     */
    static get(obj) {
      if (obj) {
        if (obj[IDENTITY_FIELD_NAME] === null || obj[IDENTITY_FIELD_NAME] === void 0) {
          if (typeof obj === "object") {
            const ctor = getFunctionName(obj.constructor);
            obj[IDENTITY_FIELD_NAME] = `${ctor}#${_ObjectIdentity.counter++}`;
          } else if (typeof obj === "function") {
            obj[IDENTITY_FIELD_NAME] = `Function#${_ObjectIdentity.counter++}`;
          }
        }
        return obj[IDENTITY_FIELD_NAME];
      }
      return null;
    }
    /**
     * Deletes the ID from the given object or function.
     */
    static clear(obj) {
      delete obj[IDENTITY_FIELD_NAME];
    }
  };
  ObjectIdentity.FIELD_NAME = IDENTITY_FIELD_NAME;
  ObjectIdentity.counter = 0;
  var ObjectIdentity_default = ObjectIdentity;

  // node_modules/@maxgraph/core/lib/esm/util/cloneUtils.js
  var clone = function _clone(obj, transients = null, shallow = false) {
    shallow = shallow != null ? shallow : false;
    let clone2 = null;
    if (obj != null && typeof obj.constructor === "function") {
      clone2 = new obj.constructor();
      for (const i in obj) {
        if (i != ObjectIdentity_default.FIELD_NAME && (transients == null || !transients.includes(i))) {
          if (!shallow && typeof obj[i] === "object") {
            clone2[i] = _clone(obj[i]);
          } else {
            clone2[i] = obj[i];
          }
        }
      }
    }
    return clone2;
  };

  // node_modules/@maxgraph/core/lib/esm/view/cell/Cell.js
  var Cell = class {
    constructor(value = null, geometry = null, style = {}) {
      this.invalidating = false;
      this.onInit = null;
      this.overlays = [];
      this.id = null;
      this.value = null;
      this.geometry = null;
      this.style = {};
      this.vertex = false;
      this.edge = false;
      this.connectable = true;
      this.visible = true;
      this.collapsed = false;
      this.parent = null;
      this.source = null;
      this.target = null;
      this.children = [];
      this.edges = [];
      this.mxTransient = [
        "id",
        "value",
        "parent",
        "source",
        "target",
        "children",
        "edges"
      ];
      this.value = value;
      this.setGeometry(geometry);
      this.setStyle(style);
      if (this.onInit) {
        this.onInit();
      }
    }
    // TODO: Document me!!!
    getChildren() {
      return this.children || [];
    }
    /**
     * Returns the Id of the cell as a string.
     */
    getId() {
      return this.id;
    }
    /**
     * Sets the Id of the cell to the given string.
     */
    setId(id) {
      this.id = id;
    }
    /**
     * Returns the user object of the cell. The user object is stored in {@link value}.
     */
    getValue() {
      return this.value;
    }
    /**
     * Sets the user object of the cell. The user object is stored in {@link value}.
     */
    setValue(value) {
      this.value = value;
    }
    /**
     * Changes the user object after an in-place edit
     * and returns the previous value. This implementation
     * replaces the user object with the given value and
     * returns the old user object.
     */
    valueChanged(newValue) {
      const previous = this.getValue();
      this.setValue(newValue);
      return previous;
    }
    /**
     * Returns the {@link Geometry} that describes the {@link geometry}.
     */
    getGeometry() {
      return this.geometry;
    }
    /**
     * Sets the {@link Geometry} to be used as the {@link geometry}.
     */
    setGeometry(geometry) {
      this.geometry = geometry;
    }
    /**
     * Returns a string that describes the {@link style}.
     *
     * **IMPORTANT**: if you want to get the style object to later update it and propagate changes to the view, use {@link getClonedStyle} instead.
     */
    getStyle() {
      return this.style;
    }
    /**
     * Use this method to get the style object to later update it and propagate changes to the view.
     *
     * See {@link GraphDataModel.setStyle} for more details.
     */
    getClonedStyle() {
      return clone(this.getStyle());
    }
    /**
     * Sets the string to be used as the {@link style}.
     */
    setStyle(style) {
      this.style = style;
    }
    /**
     * Returns true if the cell is a vertex.
     */
    isVertex() {
      return this.vertex;
    }
    /**
     * Specifies if the cell is a vertex. This should only be assigned at
     * construction of the cell and not be changed during its lifecycle.
     *
     * @param vertex Boolean that specifies if the cell is a vertex.
     */
    setVertex(vertex) {
      this.vertex = vertex;
    }
    /**
     * Returns true if the cell is an edge.
     */
    isEdge() {
      return this.edge;
    }
    /**
     * Specifies if the cell is an edge. This should only be assigned at
     * construction of the cell and not be changed during its lifecycle.
     *
     * @param edge Boolean that specifies if the cell is an edge.
     */
    setEdge(edge) {
      this.edge = edge;
    }
    /**
     * Returns true if the cell is connectable.
     */
    isConnectable() {
      return this.connectable;
    }
    /**
     * Sets the connectable state.
     *
     * @param connectable Boolean that specifies the new connectable state.
     */
    setConnectable(connectable) {
      this.connectable = connectable;
    }
    /**
     * Returns true if the cell is visibile.
     */
    isVisible() {
      return this.visible;
    }
    /**
     * Specifies if the cell is visible.
     *
     * @param visible Boolean that specifies the new visible state.
     */
    setVisible(visible) {
      this.visible = visible;
    }
    /**
     * Returns true if the cell is collapsed.
     */
    isCollapsed() {
      return this.collapsed;
    }
    /**
     * Sets the collapsed state.
     *
     * @param collapsed Boolean that specifies the new collapsed state.
     */
    setCollapsed(collapsed) {
      this.collapsed = collapsed;
    }
    /**
     * Returns the cell's parent.
     */
    getParent() {
      return this.parent;
    }
    /**
     * Sets the parent cell.
     *
     * @param parent<Cell> that represents the new parent.
     */
    setParent(parent) {
      this.parent = parent;
    }
    /**
     * Returns the source or target terminal.
     *
     * @param source Boolean that specifies if the source terminal should be
     * returned.
     */
    getTerminal(source = false) {
      return source ? this.source : this.target;
    }
    /**
     * Sets the source or target terminal and returns the new terminal.
     *
     * @param terminal  Cell that represents the new source or target terminal.
     * @param isSource  boolean that specifies if the source or target terminal should be set.
     */
    setTerminal(terminal, isSource) {
      if (isSource) {
        this.source = terminal;
      } else {
        this.target = terminal;
      }
      return terminal;
    }
    /**
     * Returns the number of child cells.
     */
    getChildCount() {
      return this.children.length;
    }
    /**
     * Returns the index of the specified child in the child array.
     *
     * @param child Child whose index should be returned.
     */
    getIndex(child) {
      if (child === null)
        return -1;
      return this.children.indexOf(child);
    }
    /**
     * Returns the child at the specified index.
     *
     * @param index Integer that specifies the child to be returned.
     */
    getChildAt(index) {
      return this.children[index];
    }
    /**
     * Inserts the specified child into the child array at the specified index and updates the parent reference of the child.
     * If not index is specified, then the child is appended to the child array.
     * Returns the inserted child.
     *
     * @param child {@link Cell} to be inserted or appended to the child array.
     * @param index Optional integer that specifies the index at which the child should be inserted into the child array.
     */
    insert(child, index) {
      if (index === void 0) {
        index = this.getChildCount();
        if (child.getParent() === this) {
          index--;
        }
      }
      child.removeFromParent();
      child.setParent(this);
      this.children.splice(index, 0, child);
      return child;
    }
    /**
     * Removes the child at the specified index from the child array and returns the child that was removed.
     * Will remove the parent reference of the child.
     *
     * @param index Integer that specifies the index of the child to be removed.
     */
    remove(index) {
      let child = null;
      if (index >= 0) {
        child = this.getChildAt(index);
        if (child) {
          this.children.splice(index, 1);
          child.setParent(null);
        }
      }
      return child;
    }
    /**
     * Removes the cell from its parent.
     */
    removeFromParent() {
      if (this.parent) {
        const index = this.parent.getIndex(this);
        this.parent.remove(index);
      }
    }
    /**
     * Returns the number of edges in the edge array.
     */
    getEdgeCount() {
      return this.edges.length;
    }
    /**
     * Returns the index of the specified edge in {@link edges}.
     *
     * @param edge {@link Cell} whose index in {@link edges} should be returned.
     */
    getEdgeIndex(edge) {
      return this.edges.indexOf(edge);
    }
    /**
     * Returns the edge at the specified index in {@link edges}.
     *
     * @param index Integer that specifies the index of the edge to be returned.
     */
    getEdgeAt(index) {
      return this.edges[index];
    }
    /**
     * Inserts the specified edge into the edge array and returns the edge.
     * Will update the respective terminal reference of the edge.
     *
     * @param edge {@link Cell} to be inserted into the edge array.
     * @param isOutgoing Boolean that specifies if the edge is outgoing.
     */
    insertEdge(edge, isOutgoing = false) {
      edge.removeFromTerminal(isOutgoing);
      edge.setTerminal(this, isOutgoing);
      if (this.edges.length === 0 || edge.getTerminal(!isOutgoing) !== this || !this.edges.includes(edge)) {
        this.edges.push(edge);
      }
      return edge;
    }
    /**
     * Removes the specified edge from the edge array and returns the edge.
     * Will remove the respective terminal reference from the edge.
     *
     * @param edge {@link Cell} to be removed from the edge array.
     * @param isOutgoing Boolean that specifies if the edge is outgoing.
     */
    removeEdge(edge, isOutgoing = false) {
      if (edge != null) {
        if (edge.getTerminal(!isOutgoing) !== this && this.edges != null) {
          const index = this.getEdgeIndex(edge);
          if (index >= 0) {
            this.edges.splice(index, 1);
          }
        }
        edge.setTerminal(null, isOutgoing);
      }
      return edge;
    }
    /**
     * Removes the edge from its source or target terminal.
     *
     * @param isSource Boolean that specifies if the edge should be removed from its source or target terminal.
     */
    removeFromTerminal(isSource) {
      const terminal = this.getTerminal(isSource);
      if (terminal) {
        terminal.removeEdge(this, isSource);
      }
    }
    /**
     * Returns true if the user object is an XML node that contains the given attribute.
     *
     * @param name Name nameName of the attribute.
     */
    hasAttribute(name) {
      var _a2;
      const userObject = this.getValue();
      return isElement(userObject) && userObject.hasAttribute ? userObject.hasAttribute(name) : !isNullish((_a2 = userObject.getAttribute) == null ? void 0 : _a2.call(userObject, name));
    }
    /**
     * Returns the specified attribute from the user object if it is an XML node.
     *
     * @param name Name of the attribute whose value should be returned.
     * @param defaultValue Optional default value to use if the attribute has no
     * value.
     */
    getAttribute(name, defaultValue) {
      var _a2;
      const userObject = this.getValue();
      const val = isElement(userObject) ? (_a2 = userObject.getAttribute) == null ? void 0 : _a2.call(userObject, name) : null;
      return val != null ? val : defaultValue;
    }
    /**
     * Sets the specified attribute on the user object if it is an XML node.
     *
     * @param name Name of the attribute whose value should be set.
     * @param value New value of the attribute.
     */
    setAttribute(name, value) {
      var _a2;
      const userObject = this.getValue();
      if (isElement(userObject)) {
        (_a2 = userObject.setAttribute) == null ? void 0 : _a2.call(userObject, name, value);
      }
    }
    /**
     * Returns a clone of the cell.
     *
     * Uses {@link cloneValue} to clone the user object.
     *
     * All fields in {@link mxTransient} are ignored during the cloning.
     */
    clone() {
      const c = clone(this, this.mxTransient);
      c.setValue(this.cloneValue());
      return c;
    }
    /**
     * Returns a clone of the cell's user object.
     */
    cloneValue() {
      let value = this.getValue();
      if (!isNullish(value)) {
        if (typeof value.clone === "function") {
          value = value.clone();
        } else if (!isNullish(value.nodeType) && value.cloneNode) {
          value = value.cloneNode(true);
        }
      }
      return value;
    }
    /**
     * Returns the nearest common ancestor for the specified cells to `this`.
     *
     * @param {Cell} cell2 that specifies the second cell in the tree.
     */
    getNearestCommonAncestor(cell2) {
      let path = CellPath_default.create(cell2);
      if (path.length > 0) {
        let cell = this;
        let current = CellPath_default.create(cell);
        if (path.length < current.length) {
          cell = cell2;
          const tmp = current;
          current = path;
          path = tmp;
        }
        while (cell && current) {
          const parent = cell.getParent();
          if (path.indexOf(current + CellPath_default.PATH_SEPARATOR) === 0 && parent) {
            return cell;
          }
          current = CellPath_default.getParentPath(current);
          cell = parent;
        }
      }
      return null;
    }
    /**
     * Returns true if the given parent is an ancestor of the given child. Note
     * returns true if child == parent.
     *
     * @param {Cell} child  that specifies the child.
     */
    isAncestor(child) {
      while (child && child !== this) {
        child = child.getParent();
      }
      return child === this;
    }
    /**
     * Returns the child vertices of the given parent.
     */
    getChildVertices() {
      return this.getChildCells(true, false);
    }
    /**
     * Returns the child edges of the given parent.
     */
    getChildEdges() {
      return this.getChildCells(false, true);
    }
    /**
     * Returns the children of the given cell that are vertices and/or edges
     * depending on the arguments.
     *
     * @param vertices  Boolean indicating if child vertices should be returned.
     * Default is false.
     * @param edges  Boolean indicating if child edges should be returned.
     * Default is false.
     */
    getChildCells(vertices = false, edges = false) {
      const childCount = this.getChildCount();
      const result = [];
      for (let i = 0; i < childCount; i += 1) {
        const child = this.getChildAt(i);
        if (!edges && !vertices || edges && child.isEdge() || vertices && child.isVertex()) {
          result.push(child);
        }
      }
      return result;
    }
    /**
     * Returns the number of incoming or outgoing edges, ignoring the given
     * edge.
     *
     * @param outgoing  Boolean that specifies if the number of outgoing or
     * incoming edges should be returned.
     * @param {Cell} ignoredEdge  that represents an edge to be ignored.
     */
    getDirectedEdgeCount(outgoing, ignoredEdge = null) {
      let count = 0;
      const edgeCount = this.getEdgeCount();
      for (let i = 0; i < edgeCount; i += 1) {
        const edge = this.getEdgeAt(i);
        if (edge !== ignoredEdge && edge && edge.getTerminal(outgoing) === this) {
          count += 1;
        }
      }
      return count;
    }
    /**
     * Returns all edges of the given cell without loops.
     */
    getConnections() {
      return this.getEdges(true, true, false);
    }
    /**
     * Returns the incoming edges of the given cell without loops.
     */
    getIncomingEdges() {
      return this.getEdges(true, false, false);
    }
    /**
     * Returns the outgoing edges of the given cell without loops.
     */
    getOutgoingEdges() {
      return this.getEdges(false, true, false);
    }
    /**
     * Returns all distinct edges connected to this cell as a new array of
     * {@link Cell}. If at least one of incoming or outgoing is true, then loops
     * are ignored, otherwise if both are false, then all edges connected to
     * the given cell are returned including loops.
     *
     * @param incoming  Optional boolean that specifies if incoming edges should be
     * returned. Default is true.
     * @param outgoing  Optional boolean that specifies if outgoing edges should be
     * returned. Default is true.
     * @param includeLoops  Optional boolean that specifies if loops should be returned.
     * Default is true.
     */
    getEdges(incoming = true, outgoing = true, includeLoops = true) {
      const edgeCount = this.getEdgeCount();
      const result = [];
      for (let i = 0; i < edgeCount; i += 1) {
        const edge = this.getEdgeAt(i);
        const source = edge.getTerminal(true);
        const target = edge.getTerminal(false);
        if (includeLoops && source === target || source !== target && (incoming && target === this || outgoing && source === this)) {
          result.push(edge);
        }
      }
      return result;
    }
    /**
     * Returns the absolute, accumulated origin for the children inside the
     * given parent as an {@link Point}.
     */
    getOrigin() {
      let result = new Point_default();
      const parent = this.getParent();
      if (parent) {
        result = parent.getOrigin();
        if (!this.isEdge()) {
          const geo = this.getGeometry();
          if (geo) {
            result.x += geo.x;
            result.y += geo.y;
          }
        }
      }
      return result;
    }
    /**
     * Returns all descendants of the given cell and the cell itself in an array.
     */
    getDescendants() {
      return this.filterDescendants(null);
    }
    /**
     * Visits all cells recursively and applies the specified filter function
     * to each cell. If the function returns true then the cell is added
     * to the resulting array. The parent and result paramters are optional.
     * If parent is not specified then the recursion starts at {@link root}.
     *
     * Example:
     * The following example extracts all vertices from a given model:
     * ```javascript
     * var filter(cell)
     * {
     * 	return model.isVertex(cell);
     * }
     * var vertices = model.filterDescendants(filter);
     * ```
     *
     * @param filter  JavaScript function that takes an {@link Cell} as an argument
     * and returns a boolean.
     */
    filterDescendants(filter) {
      let result = [];
      if (filter === null || filter(this)) {
        result.push(this);
      }
      const childCount = this.getChildCount();
      for (let i = 0; i < childCount; i += 1) {
        const child = this.getChildAt(i);
        result = result.concat(child.filterDescendants(filter));
      }
      return result;
    }
    /**
     * Returns the root of the model or the topmost parent of the given cell.
     */
    getRoot() {
      let cell = this;
      let root = cell;
      while (cell) {
        root = cell;
        cell = cell.getParent();
      }
      return root;
    }
  };
  var Cell_default = Cell;

  // node_modules/@maxgraph/core/lib/esm/view/undoable-change/ChildChange.js
  var ChildChange = class {
    constructor(model, parent, child, index = 0) {
      this.model = model;
      this.parent = parent;
      this.previous = parent;
      this.child = child;
      this.index = index;
      this.previousIndex = index;
    }
    /**
     * Changes the parent of {@link child} using {@link GraphDataModel.parentForCellChanged} and removes or restores the cell's connections.
     */
    execute() {
      let tmp = this.child.getParent();
      const tmp2 = tmp ? tmp.getIndex(this.child) : 0;
      if (!this.previous) {
        this.connect(this.child, false);
      }
      tmp = this.model.parentForCellChanged(this.child, this.previous, this.previousIndex);
      if (this.previous) {
        this.connect(this.child, true);
      }
      this.parent = this.previous;
      this.previous = tmp;
      this.index = this.previousIndex;
      this.previousIndex = tmp2;
    }
    /**
     * Connects the source and the target of the given cell.
     *
     * If {@link isConnect} is true, the source and target terminals are referenced  as such in the model. Otherwise, they are removed.
     */
    connect(cell, isConnect = true) {
      const source = cell.getTerminal(true);
      const target = cell.getTerminal(false);
      if (source) {
        if (isConnect) {
          this.model.terminalForCellChanged(cell, source, true);
        } else {
          this.model.terminalForCellChanged(cell, null, true);
        }
      }
      if (target) {
        if (isConnect) {
          this.model.terminalForCellChanged(cell, target, false);
        } else {
          this.model.terminalForCellChanged(cell, null, false);
        }
      }
      cell.setTerminal(source, true);
      cell.setTerminal(target, false);
      const childCount = cell.getChildCount();
      for (let i = 0; i < childCount; i += 1) {
        this.connect(cell.getChildAt(i), isConnect);
      }
    }
  };
  var ChildChange_default = ChildChange;

  // node_modules/@maxgraph/core/lib/esm/view/undoable-change/GeometryChange.js
  var GeometryChange = class {
    constructor(model, cell, geometry) {
      this.model = model;
      this.cell = cell;
      this.geometry = geometry;
      this.previous = geometry;
    }
    /**
     * Changes the geometry of {@link cell} to {@link previous} using{@link GraphDataModel.geometryForCellChanged}.
     */
    execute() {
      this.geometry = this.previous;
      this.previous = this.model.geometryForCellChanged(this.cell, this.previous);
    }
  };
  var GeometryChange_default = GeometryChange;

  // node_modules/@maxgraph/core/lib/esm/view/undoable-change/RootChange.js
  var RootChange = class {
    constructor(model, root) {
      this.model = model;
      this.root = root;
      this.previous = root;
    }
    /**
     * Carries out a change of the root using {@link GraphDataModel.rootChanged}.
     */
    execute() {
      this.root = this.previous;
      this.previous = this.model.rootChanged(this.previous);
    }
  };
  var RootChange_default = RootChange;

  // node_modules/@maxgraph/core/lib/esm/view/undoable-change/StyleChange.js
  var StyleChange = class {
    constructor(model, cell, style) {
      this.model = model;
      this.cell = cell;
      this.style = style;
      this.previous = style;
    }
    /**
     * Changes the style of {@link cell} to {@link previous} using {@link GraphDataModel.styleForCellChanged}.
     */
    execute() {
      this.style = this.previous;
      this.previous = this.model.styleForCellChanged(this.cell, this.previous);
    }
  };
  var StyleChange_default = StyleChange;

  // node_modules/@maxgraph/core/lib/esm/view/undoable-change/TerminalChange.js
  var TerminalChange = class {
    constructor(model, cell, terminal, source) {
      this.model = model;
      this.cell = cell;
      this.terminal = terminal;
      this.previous = terminal;
      this.source = source;
    }
    /**
     * Changes the terminal of {@link cell} to {@link previous} using {@link GraphDataModel.terminalForCellChanged}.
     */
    execute() {
      this.terminal = this.previous;
      this.previous = this.model.terminalForCellChanged(this.cell, this.previous, this.source);
    }
  };
  var TerminalChange_default = TerminalChange;

  // node_modules/@maxgraph/core/lib/esm/view/undoable-change/ValueChange.js
  var ValueChange = class {
    constructor(model, cell, value) {
      this.model = model;
      this.cell = cell;
      this.value = value;
      this.previous = value;
    }
    /**
     * Changes the value of {@link cell} to {@link previous} using {@link GraphDataModel.valueForCellChanged}.
     */
    execute() {
      this.value = this.previous;
      this.previous = this.model.valueForCellChanged(this.cell, this.previous);
    }
  };
  var ValueChange_default = ValueChange;

  // node_modules/@maxgraph/core/lib/esm/view/style/edge/index.js
  var edge_exports = {};
  __export(edge_exports, {
    ElbowConnector: () => ElbowConnector,
    EntityRelation: () => EntityRelation,
    Loop: () => Loop,
    ManhattanConnector: () => ManhattanConnector,
    OrthConnector: () => OrthogonalConnector,
    SegmentConnector: () => SegmentConnector,
    SideToSide: () => SideToSide,
    TopToBottom: () => TopToBottom
  });

  // node_modules/@maxgraph/core/lib/esm/view/cell/CellState.js
  var CellState = class _CellState extends Rectangle_default {
    /**
     * Constructs a new object that represents the current state of the given Cell in the specified view.
     *
     * @param view {@link GraphView} that contains the state.
     * @param cell {@link Cell} that this state represents.
     * @param style the style of the Cell.
     */
    constructor(view = null, cell = null, style = null) {
      super();
      this.node = null;
      this.cellBounds = null;
      this.paintBounds = null;
      this.boundingBox = null;
      this.control = null;
      this.overlays = /* @__PURE__ */ new Map();
      this.invalidStyle = false;
      this.invalid = true;
      this.absolutePoints = [];
      this.visibleSourceState = null;
      this.visibleTargetState = null;
      this.terminalDistance = 0;
      this.length = 0;
      this.segments = [];
      this.shape = null;
      this.text = null;
      this.unscaledWidth = 0;
      this.unscaledHeight = 0;
      this.parentHighlight = null;
      this.point = null;
      if (view) {
        this.view = view;
      }
      if (cell) {
        this.cell = cell;
      }
      this.style = style != null ? style : {};
      this.origin = new Point_default();
      this.absoluteOffset = new Point_default();
    }
    /**
     * Returns the {@link Rectangle} that should be used as the perimeter of the cell.
     *
     * @param border Optional border to be added around the perimeter bounds.
     * @param bounds Optional {@link Rectangle} to be used as the initial bounds.
     */
    getPerimeterBounds(border = 0, bounds = new Rectangle_default(this.x, this.y, this.width, this.height)) {
      var _a2, _b;
      if (((_b = (_a2 = this.shape) == null ? void 0 : _a2.stencil) == null ? void 0 : _b.aspect) === "fixed") {
        const aspect = this.shape.stencil.computeAspect(this.shape, bounds.x, bounds.y, bounds.width, bounds.height);
        bounds.x = aspect.x;
        bounds.y = aspect.y;
        bounds.width = this.shape.stencil.w0 * aspect.width;
        bounds.height = this.shape.stencil.h0 * aspect.height;
      }
      if (border !== 0) {
        bounds.grow(border);
      }
      return bounds;
    }
    /**
     * Sets the first or last point in <absolutePoints> depending on isSource.
     *
     * @param point {@link Point} that represents the terminal point.
     * @param isSource Boolean that specifies if the first or last point should be assigned.
     */
    setAbsoluteTerminalPoint(point, isSource = false) {
      if (isSource) {
        if (this.absolutePoints.length === 0) {
          this.absolutePoints.push(point);
        } else {
          this.absolutePoints[0] = point;
        }
      } else if (this.absolutePoints.length === 0) {
        this.absolutePoints.push(null);
        this.absolutePoints.push(point);
      } else if (this.absolutePoints.length === 1) {
        this.absolutePoints.push(point);
      } else {
        this.absolutePoints[this.absolutePoints.length - 1] = point;
      }
    }
    /**
     * Sets the given cursor on the shape and text shape.
     */
    setCursor(cursor) {
      if (this.shape) {
        this.shape.setCursor(cursor);
      }
      if (this.text) {
        this.text.setCursor(cursor);
      }
    }
    /**
     * Returns the visible source or target terminal cell.
     *
     * @param source Boolean that specifies if the source or target cell should be returned.
     */
    getVisibleTerminal(source = false) {
      var _a2, _b;
      return (_b = (_a2 = this.getVisibleTerminalState(source)) == null ? void 0 : _a2.cell) != null ? _b : null;
    }
    /**
     * Returns the visible source or target terminal state.
     *
     * @param source Boolean that specifies if the source or target state should be returned.
     */
    getVisibleTerminalState(source = false) {
      return source ? this.visibleSourceState : this.visibleTargetState;
    }
    /**
     * Sets the visible source or target terminal state.
     *
     * @param terminalState {@link CellState} that represents the terminal.
     * @param source Boolean that specifies if the source or target state should be set.
     */
    setVisibleTerminalState(terminalState, source = false) {
      if (source) {
        this.visibleSourceState = terminalState;
      } else {
        this.visibleTargetState = terminalState;
      }
    }
    /**
     * Returns the unscaled, untranslated bounds.
     */
    getCellBounds() {
      return this.cellBounds;
    }
    /**
     * Returns the unscaled, untranslated paint bounds.
     *
     * This is the same as {@link getCellBounds} but with a 90-degrees rotation if the  {@link Shape.isPaintBoundsInverted} returns `true`.
     */
    getPaintBounds() {
      return this.paintBounds;
    }
    /**
     * Updates the {@link cellBounds} and {@link paintBounds}.
     */
    updateCachedBounds() {
      const view = this.view;
      const tr = view.translate;
      const s = view.scale;
      this.cellBounds = new Rectangle_default(this.x / s - tr.x, this.y / s - tr.y, this.width / s, this.height / s);
      this.paintBounds = Rectangle_default.fromRectangle(this.cellBounds);
      if (this.shape && this.shape.isPaintBoundsInverted()) {
        this.paintBounds.rotate90();
      }
    }
    /**
     * Copies all fields from the given state to this state.
     */
    setState(state) {
      this.view = state.view;
      this.cell = state.cell;
      this.style = state.style;
      this.absolutePoints = state.absolutePoints;
      this.origin = state.origin;
      this.absoluteOffset = state.absoluteOffset;
      this.boundingBox = state.boundingBox;
      this.terminalDistance = state.terminalDistance;
      this.segments = state.segments;
      this.length = state.length;
      this.x = state.x;
      this.y = state.y;
      this.width = state.width;
      this.height = state.height;
      this.unscaledWidth = state.unscaledWidth;
      this.unscaledHeight = state.unscaledHeight;
    }
    clone() {
      const clone2 = new _CellState(this.view, this.cell, this.style);
      for (let i = 0; i < this.absolutePoints.length; i += 1) {
        const p = this.absolutePoints[i];
        clone2.absolutePoints[i] = p ? p.clone() : null;
      }
      if (this.origin) {
        clone2.origin = this.origin.clone();
      }
      if (this.absoluteOffset) {
        clone2.absoluteOffset = this.absoluteOffset.clone();
      }
      if (this.boundingBox) {
        clone2.boundingBox = this.boundingBox.clone();
      }
      clone2.terminalDistance = this.terminalDistance;
      clone2.segments = this.segments;
      clone2.length = this.length;
      clone2.x = this.x;
      clone2.y = this.y;
      clone2.width = this.width;
      clone2.height = this.height;
      clone2.unscaledWidth = this.unscaledWidth;
      clone2.unscaledHeight = this.unscaledHeight;
      return clone2;
    }
    /**
     * Destroys the state and all associated resources.
     */
    destroy() {
      this.view.graph.cellRenderer.destroy(this);
    }
    /**
     * Returns `true` if the given cell state is a loop.
     *
     * @param state {@link CellState} that represents a potential loop.
     */
    isLoop(state) {
      const src = this.getVisibleTerminalState(true);
      return src && src === this.getVisibleTerminalState(false);
    }
    /*****************************************************************************
     * Group: Graph appearance
     *****************************************************************************/
    /**
     * Returns the vertical alignment for the given cell state.
     * This implementation returns the value stored in the {@link CellStateStyle.verticalAlign}
     * property of {@link style}.
     */
    getVerticalAlign() {
      var _a2;
      return (_a2 = this.style.verticalAlign) != null ? _a2 : "middle";
    }
    /**
     * Returns `true` if the given state has no stroke, no fill color and no image.
     */
    isTransparentState() {
      var _a2, _b;
      return ((_a2 = this.style.strokeColor) != null ? _a2 : NONE) === NONE && ((_b = this.style.fillColor) != null ? _b : NONE) === NONE && !this.getImageSrc();
    }
    /**
     * Returns the image URL for the given cell state.
     * This implementation returns the value stored in the {@link CellStateStyle.image} property
     * of {@link style}.
     */
    getImageSrc() {
      return this.style.image || null;
    }
    /**
     * Returns the indicator color for the given cell state.
     * This implementation returns the value stored in the {@link CellStateStyle.indicatorColor}
     * property of {@link style}.
     */
    getIndicatorColor() {
      return this.style.indicatorColor || null;
    }
    /**
     * Returns the indicator gradient color for the given cell state.
     * This implementation returns the value stored in the {@link CellStateStyle.gradientColor}
     * property of {@link style}.
     */
    getIndicatorGradientColor() {
      return this.style.gradientColor || null;
    }
    /**
     * Returns the indicator shape for the given cell state.
     * This implementation returns the value stored in the {@link CellStateStyle.indicatorShape}
     * property of {@link style}.
     */
    getIndicatorShape() {
      return this.style.indicatorShape || null;
    }
    /**
     * Returns the indicator image for the given cell state.
     * This implementation returns the value stored in the {@link CellStateStyle.indicatorImage}
     * property of {@link style}.
     */
    getIndicatorImageSrc() {
      return this.style.indicatorImage || null;
    }
  };
  var CellState_default = CellState;

  // node_modules/@maxgraph/core/lib/esm/util/mathUtils.js
  var toRadians = (deg) => {
    return Math.PI * deg / 180;
  };
  var arcToCurves = (x0, y0, r1, r2, angle, largeArcFlag, sweepFlag, x, y) => {
    x -= x0;
    y -= y0;
    if (r1 === 0 || r2 === 0) {
      return [];
    }
    const fS = sweepFlag;
    const psai = angle;
    r1 = Math.abs(r1);
    r2 = Math.abs(r2);
    const ctx = -x / 2;
    const cty = -y / 2;
    const cpsi = Math.cos(psai * Math.PI / 180);
    const spsi = Math.sin(psai * Math.PI / 180);
    const rxd = cpsi * ctx + spsi * cty;
    const ryd = -1 * spsi * ctx + cpsi * cty;
    const rxdd = rxd * rxd;
    const rydd = ryd * ryd;
    const r1x = r1 * r1;
    const r2y = r2 * r2;
    const lamda = rxdd / r1x + rydd / r2y;
    let sds;
    if (lamda > 1) {
      r1 = Math.sqrt(lamda) * r1;
      r2 = Math.sqrt(lamda) * r2;
      sds = 0;
    } else {
      let seif = 1;
      if (largeArcFlag === fS) {
        seif = -1;
      }
      sds = seif * Math.sqrt((r1x * r2y - r1x * rydd - r2y * rxdd) / (r1x * rydd + r2y * rxdd));
    }
    const txd = sds * r1 * ryd / r2;
    const tyd = -1 * sds * r2 * rxd / r1;
    const tx = cpsi * txd - spsi * tyd + x / 2;
    const ty = spsi * txd + cpsi * tyd + y / 2;
    let rad = Math.atan2((ryd - tyd) / r2, (rxd - txd) / r1) - Math.atan2(0, 1);
    let s1 = rad >= 0 ? rad : 2 * Math.PI + rad;
    rad = Math.atan2((-ryd - tyd) / r2, (-rxd - txd) / r1) - Math.atan2((ryd - tyd) / r2, (rxd - txd) / r1);
    let dr = rad >= 0 ? rad : 2 * Math.PI + rad;
    if (!fS && dr > 0) {
      dr -= 2 * Math.PI;
    } else if (fS && dr < 0) {
      dr += 2 * Math.PI;
    }
    const sse = dr * 2 / Math.PI;
    const seg = Math.ceil(sse < 0 ? -1 * sse : sse);
    const segr = dr / seg;
    const t = 8 / 3 * Math.sin(segr / 4) * Math.sin(segr / 4) / Math.sin(segr / 2);
    const cpsir1 = cpsi * r1;
    const cpsir2 = cpsi * r2;
    const spsir1 = spsi * r1;
    const spsir2 = spsi * r2;
    let mc = Math.cos(s1);
    let ms = Math.sin(s1);
    let x2 = -t * (cpsir1 * ms + spsir2 * mc);
    let y2 = -t * (spsir1 * ms - cpsir2 * mc);
    let x3 = 0;
    let y3 = 0;
    const result = [];
    for (let n = 0; n < seg; ++n) {
      s1 += segr;
      mc = Math.cos(s1);
      ms = Math.sin(s1);
      x3 = cpsir1 * mc - spsir2 * ms + tx;
      y3 = spsir1 * mc + cpsir2 * ms + ty;
      const dx = -t * (cpsir1 * ms + spsir2 * mc);
      const dy = -t * (spsir1 * ms - cpsir2 * mc);
      const index = n * 6;
      result[index] = Number(x2 + x0);
      result[index + 1] = Number(y2 + y0);
      result[index + 2] = Number(x3 - dx + x0);
      result[index + 3] = Number(y3 - dy + y0);
      result[index + 4] = Number(x3 + x0);
      result[index + 5] = Number(y3 + y0);
      x2 = x3 + dx;
      y2 = y3 + dy;
    }
    return result;
  };
  var getBoundingBox = (rect, rotation, cx = null) => {
    let result = null;
    if (rect && rotation !== 0) {
      const rad = toRadians(rotation);
      const cos = Math.cos(rad);
      const sin = Math.sin(rad);
      cx = cx != null ? cx : new Point_default(rect.x + rect.width / 2, rect.y + rect.height / 2);
      let p1 = new Point_default(rect.x, rect.y);
      let p2 = new Point_default(rect.x + rect.width, rect.y);
      let p3 = new Point_default(p2.x, rect.y + rect.height);
      let p4 = new Point_default(rect.x, p3.y);
      p1 = getRotatedPoint(p1, cos, sin, cx);
      p2 = getRotatedPoint(p2, cos, sin, cx);
      p3 = getRotatedPoint(p3, cos, sin, cx);
      p4 = getRotatedPoint(p4, cos, sin, cx);
      result = new Rectangle_default(p1.x, p1.y, 0, 0);
      result.add(new Rectangle_default(p2.x, p2.y, 0, 0));
      result.add(new Rectangle_default(p3.x, p3.y, 0, 0));
      result.add(new Rectangle_default(p4.x, p4.y, 0, 0));
    }
    return result;
  };
  var getRotatedPoint = (pt, cos, sin, c = new Point_default()) => {
    const x = pt.x - c.x;
    const y = pt.y - c.y;
    const x1 = x * cos - y * sin;
    const y1 = y * cos + x * sin;
    return new Point_default(x1 + c.x, y1 + c.y);
  };
  var getPortConstraints = (terminal, edge, source, defaultValue) => {
    var _a2, _b, _c;
    const value = (_a2 = terminal.style.portConstraint) != null ? _a2 : source ? edge.style.sourcePortConstraint : edge.style.targetPortConstraint;
    if (isNullish(value)) {
      return defaultValue;
    }
    const directions = value.toString();
    let returnValue = DIRECTION_MASK.NONE;
    const constraintRotationEnabled = (_b = terminal.style.portConstraintRotation) != null ? _b : false;
    const rotation = constraintRotationEnabled ? (_c = terminal.style.rotation) != null ? _c : 0 : 0;
    let quad = 0;
    if (rotation > 45) {
      quad = 1;
      if (rotation >= 135) {
        quad = 2;
      }
    } else if (rotation < -45) {
      quad = 3;
      if (rotation <= -135) {
        quad = 2;
      }
    }
    if (directions.includes("north")) {
      switch (quad) {
        case 0:
          returnValue |= DIRECTION_MASK.NORTH;
          break;
        case 1:
          returnValue |= DIRECTION_MASK.EAST;
          break;
        case 2:
          returnValue |= DIRECTION_MASK.SOUTH;
          break;
        case 3:
          returnValue |= DIRECTION_MASK.WEST;
          break;
      }
    }
    if (directions.includes("west")) {
      switch (quad) {
        case 0:
          returnValue |= DIRECTION_MASK.WEST;
          break;
        case 1:
          returnValue |= DIRECTION_MASK.NORTH;
          break;
        case 2:
          returnValue |= DIRECTION_MASK.EAST;
          break;
        case 3:
          returnValue |= DIRECTION_MASK.SOUTH;
          break;
      }
    }
    if (directions.includes("south")) {
      switch (quad) {
        case 0:
          returnValue |= DIRECTION_MASK.SOUTH;
          break;
        case 1:
          returnValue |= DIRECTION_MASK.WEST;
          break;
        case 2:
          returnValue |= DIRECTION_MASK.NORTH;
          break;
        case 3:
          returnValue |= DIRECTION_MASK.EAST;
          break;
      }
    }
    if (directions.includes("east")) {
      switch (quad) {
        case 0:
          returnValue |= DIRECTION_MASK.EAST;
          break;
        case 1:
          returnValue |= DIRECTION_MASK.SOUTH;
          break;
        case 2:
          returnValue |= DIRECTION_MASK.WEST;
          break;
        case 3:
          returnValue |= DIRECTION_MASK.NORTH;
          break;
      }
    }
    return returnValue;
  };
  var reversePortConstraints = (constraint) => {
    let result = 0;
    result = (constraint & DIRECTION_MASK.WEST) << 3;
    result |= (constraint & DIRECTION_MASK.NORTH) << 1;
    result |= (constraint & DIRECTION_MASK.SOUTH) >> 1;
    result |= (constraint & DIRECTION_MASK.EAST) >> 3;
    return result;
  };
  var findNearestSegment = (state, x, y) => {
    let index = -1;
    if (state.absolutePoints.length > 0) {
      let last = state.absolutePoints[0];
      let min = null;
      for (let i = 1; i < state.absolutePoints.length; i += 1) {
        const current = state.absolutePoints[i];
        if (!last || !current)
          continue;
        const dist = ptSegDistSq(last.x, last.y, current.x, current.y, x, y);
        if (min == null || dist < min) {
          min = dist;
          index = i - 1;
        }
        last = current;
      }
    }
    return index;
  };
  var getDirectedBounds = (rect, m, style, flipH, flipV) => {
    var _a2, _b, _c;
    const d = (_a2 = style == null ? void 0 : style.direction) != null ? _a2 : "east";
    flipH != null ? flipH : flipH = (_b = style == null ? void 0 : style.flipH) != null ? _b : false;
    flipV != null ? flipV : flipV = (_c = style == null ? void 0 : style.flipV) != null ? _c : false;
    m.x = Math.round(Math.max(0, Math.min(rect.width, m.x)));
    m.y = Math.round(Math.max(0, Math.min(rect.height, m.y)));
    m.width = Math.round(Math.max(0, Math.min(rect.width, m.width)));
    m.height = Math.round(Math.max(0, Math.min(rect.height, m.height)));
    if (flipV && (d === "south" || d === "north") || flipH && (d === "east" || d === "west")) {
      const tmp = m.x;
      m.x = m.width;
      m.width = tmp;
    }
    if (flipH && (d === "south" || d === "north") || flipV && (d === "east" || d === "west")) {
      const tmp = m.y;
      m.y = m.height;
      m.height = tmp;
    }
    const m2 = Rectangle_default.fromRectangle(m);
    switch (d) {
      case "south": {
        m2.y = m.x;
        m2.x = m.height;
        m2.width = m.y;
        m2.height = m.width;
        break;
      }
      case "west": {
        m2.y = m.height;
        m2.x = m.width;
        m2.width = m.x;
        m2.height = m.y;
        break;
      }
      case "north": {
        m2.y = m.width;
        m2.x = m.y;
        m2.width = m.height;
        m2.height = m.x;
        break;
      }
    }
    return new Rectangle_default(rect.x + m2.x, rect.y + m2.y, rect.width - m2.width - m2.x, rect.height - m2.height - m2.y);
  };
  var contains = (bounds, x, y) => {
    return bounds.x <= x && bounds.x + bounds.width >= x && bounds.y <= y && bounds.y + bounds.height >= y;
  };
  var intersects = (a, b) => {
    let tw = a.width;
    let th = a.height;
    let rw = b.width;
    let rh = b.height;
    if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
      return false;
    }
    const tx = a.x;
    const ty = a.y;
    const rx = b.x;
    const ry = b.y;
    rw += rx;
    rh += ry;
    tw += tx;
    th += ty;
    return (rw < rx || rw > tx) && (rh < ry || rh > ty) && (tw < tx || tw > rx) && (th < ty || th > ry);
  };
  var intersectsHotspot = (state, x, y, hotspot, min, max) => {
    var _a2, _b, _c;
    hotspot = hotspot != null ? hotspot : 1;
    min = min != null ? min : 0;
    max = max != null ? max : 0;
    if (hotspot > 0) {
      let cx = state.getCenterX();
      let cy = state.getCenterY();
      let w = state.width;
      let h = state.height;
      const style = state.style;
      const start = ((_a2 = style == null ? void 0 : style.startSize) != null ? _a2 : 0) * state.view.scale;
      if (start > 0) {
        if ((_b = style == null ? void 0 : style.horizontal) != null ? _b : true) {
          cy = state.y + start / 2;
          h = start;
        } else {
          cx = state.x + start / 2;
          w = start;
        }
      }
      w = Math.max(min, w * hotspot);
      h = Math.max(min, h * hotspot);
      if (max > 0) {
        w = Math.min(w, max);
        h = Math.min(h, max);
      }
      const rect = new Rectangle_default(cx - w / 2, cy - h / 2, w, h);
      const alpha = toRadians((_c = style == null ? void 0 : style.rotation) != null ? _c : 0);
      if (alpha != 0) {
        const cos = Math.cos(-alpha);
        const sin = Math.sin(-alpha);
        const cx2 = new Point_default(state.getCenterX(), state.getCenterY());
        const pt = getRotatedPoint(new Point_default(x, y), cos, sin, cx2);
        x = pt.x;
        y = pt.y;
      }
      return contains(rect, x, y);
    }
    return true;
  };
  var isNumeric = (n) => {
    return !Number.isNaN(Number.parseFloat(n)) && Number.isFinite(+n) && (typeof n !== "string" || !n.toLowerCase().includes("0x"));
  };
  var mod = (n, m) => {
    return (n % m + m) % m;
  };
  var intersection = (x0, y0, x1, y1, x2, y2, x3, y3) => {
    const denom = (y3 - y2) * (x1 - x0) - (x3 - x2) * (y1 - y0);
    const nume_a = (x3 - x2) * (y0 - y2) - (y3 - y2) * (x0 - x2);
    const nume_b = (x1 - x0) * (y0 - y2) - (y1 - y0) * (x0 - x2);
    const ua = nume_a / denom;
    const ub = nume_b / denom;
    if (ua >= 0 && ua <= 1 && ub >= 0 && ub <= 1) {
      const x = x0 + ua * (x1 - x0);
      const y = y0 + ua * (y1 - y0);
      return new Point_default(x, y);
    }
    return null;
  };
  var ptSegDistSq = (x1, y1, x2, y2, px, py) => {
    x2 -= x1;
    y2 -= y1;
    px -= x1;
    py -= y1;
    let dotprod = px * x2 + py * y2;
    let projlenSq;
    if (dotprod <= 0) {
      projlenSq = 0;
    } else {
      px = x2 - px;
      py = y2 - py;
      dotprod = px * x2 + py * y2;
      if (dotprod <= 0) {
        projlenSq = 0;
      } else {
        projlenSq = dotprod * dotprod / (x2 * x2 + y2 * y2);
      }
    }
    let lenSq = px * px + py * py - projlenSq;
    if (lenSq < 0) {
      lenSq = 0;
    }
    return lenSq;
  };
  var relativeCcw = (x1, y1, x2, y2, px, py) => {
    x2 -= x1;
    y2 -= y1;
    px -= x1;
    py -= y1;
    let ccw = px * y2 - py * x2;
    if (ccw == 0) {
      ccw = px * x2 + py * y2;
      if (ccw > 0) {
        px -= x2;
        py -= y2;
        ccw = px * x2 + py * y2;
        if (ccw < 0) {
          ccw = 0;
        }
      }
    }
    return ccw < 0 ? -1 : ccw > 0 ? 1 : 0;
  };

  // node_modules/@maxgraph/core/lib/esm/view/style/edge/SideToSide.js
  var SideToSide = (state, source, target, points, result) => {
    const { view } = state;
    let pt = points != null && points.length > 0 ? points[0] : null;
    const pts = state.absolutePoints;
    const p0 = pts[0];
    const pe = pts[pts.length - 1];
    if (pt != null) {
      pt = view.transformControlPoint(state, pt);
    }
    if (p0 != null) {
      source = new CellState_default();
      source.x = p0.x;
      source.y = p0.y;
    }
    if (pe != null) {
      target = new CellState_default();
      target.x = pe.x;
      target.y = pe.y;
    }
    if (source != null && target != null) {
      const l = Math.max(source.x, target.x);
      const r = Math.min(source.x + source.width, target.x + target.width);
      const x = pt != null ? pt.x : Math.round(r + (l - r) / 2);
      let y1 = view.getRoutingCenterY(source);
      let y2 = view.getRoutingCenterY(target);
      if (pt != null) {
        if (pt.y >= source.y && pt.y <= source.y + source.height) {
          y1 = pt.y;
        }
        if (pt.y >= target.y && pt.y <= target.y + target.height) {
          y2 = pt.y;
        }
      }
      if (!contains(target, x, y1) && !contains(source, x, y1)) {
        result.push(new Point_default(x, y1));
      }
      if (!contains(target, x, y2) && !contains(source, x, y2)) {
        result.push(new Point_default(x, y2));
      }
      if (result.length === 1) {
        if (pt != null) {
          if (!contains(target, x, pt.y) && !contains(source, x, pt.y)) {
            result.push(new Point_default(x, pt.y));
          }
        } else {
          const t = Math.max(source.y, target.y);
          const b = Math.min(source.y + source.height, target.y + target.height);
          result.push(new Point_default(x, t + (b - t) / 2));
        }
      }
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/style/edge/TopToBottom.js
  var TopToBottom = (state, source, target, points, result) => {
    const { view } = state;
    let pt = points != null && points.length > 0 ? points[0] : null;
    const pts = state.absolutePoints;
    const p0 = pts[0];
    const pe = pts[pts.length - 1];
    if (pt != null) {
      pt = view.transformControlPoint(state, pt);
    }
    if (p0 != null) {
      source = new CellState_default();
      source.x = p0.x;
      source.y = p0.y;
    }
    if (pe != null) {
      target = new CellState_default();
      target.x = pe.x;
      target.y = pe.y;
    }
    if (source != null && target != null) {
      const t = Math.max(source.y, target.y);
      const b = Math.min(source.y + source.height, target.y + target.height);
      let x = view.getRoutingCenterX(source);
      if (pt != null && pt.x >= source.x && pt.x <= source.x + source.width) {
        x = pt.x;
      }
      const y = pt != null ? pt.y : Math.round(b + (t - b) / 2);
      if (!contains(target, x, y) && !contains(source, x, y)) {
        result.push(new Point_default(x, y));
      }
      if (pt != null && pt.x >= target.x && pt.x <= target.x + target.width) {
        x = pt.x;
      } else {
        x = view.getRoutingCenterX(target);
      }
      if (!contains(target, x, y) && !contains(source, x, y)) {
        result.push(new Point_default(x, y));
      }
      if (result.length === 1) {
        if (pt != null && result.length === 1) {
          if (!contains(target, pt.x, y) && !contains(source, pt.x, y)) {
            result.push(new Point_default(pt.x, y));
          }
        } else {
          const l = Math.max(source.x, target.x);
          const r = Math.min(source.x + source.width, target.x + target.width);
          result.push(new Point_default(l + (r - l) / 2, y));
        }
      }
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/style/edge/Elbow.js
  var ElbowConnector = (state, source, target, points, result) => {
    let pt = points != null && points.length > 0 ? points[0] : null;
    let vertical = false;
    let horizontal = false;
    if (source != null && target != null) {
      if (pt != null) {
        const left = Math.min(source.x, target.x);
        const right = Math.max(source.x + source.width, target.x + target.width);
        const top = Math.min(source.y, target.y);
        const bottom = Math.max(source.y + source.height, target.y + target.height);
        pt = state.view.transformControlPoint(state, pt);
        vertical = pt.y < top || pt.y > bottom;
        horizontal = pt.x < left || pt.x > right;
      } else {
        const left = Math.max(source.x, target.x);
        const right = Math.min(source.x + source.width, target.x + target.width);
        vertical = left === right;
        if (!vertical) {
          const top = Math.max(source.y, target.y);
          const bottom = Math.min(source.y + source.height, target.y + target.height);
          horizontal = top === bottom;
        }
      }
    }
    if (!horizontal && (vertical || state.style.elbow === "vertical")) {
      TopToBottom(state, source, target, points, result);
    } else {
      SideToSide(state, source, target, points, result);
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/style/config.js
  var EntityRelationConnectorConfig = {
    /**
     * Defines the length of the horizontal segment of an `Entity Relation`.
     * This can be overridden using {@link CellStateStyle.segment} style.
     * @default {@link ENTITY_SEGMENT}
     */
    segment: ENTITY_SEGMENT
  };
  var OrthogonalConnectorConfig = {
    /**
     * If the value is not set in {@link CellStateStyle.jettySize}, defines the jetty size of the connector.
     *
     * If the computed value of the jetty size coming from {@link CellStateStyle} is 'auto', it is used in the computation of the automatic jetty size.
     * See the implementation of {@link OrthConnector} for more details.
     *
     * @default 10
     */
    buffer: 10,
    /**
     * See the implementation of {@link OrthConnector} for more details.
     * @default true
     */
    pointsFallback: true
  };
  var originalOrthogonalConnectorConfig = __spreadValues({}, OrthogonalConnectorConfig);
  var allDirections = () => {
    return ["north", "south", "east", "west"];
  };
  var ManhattanConnectorConfig = {
    maxAllowedDirectionChange: 90,
    maxLoops: 2e3,
    endDirections: allDirections(),
    startDirections: allDirections(),
    step: 12
  };
  var originalManhattanConnectorConfig = {};
  shallowCopy(ManhattanConnectorConfig, originalManhattanConnectorConfig);

  // node_modules/@maxgraph/core/lib/esm/view/style/edge/EntityRelation.js
  var EntityRelation = (state, source, target, _points, result) => {
    var _a2, _b;
    const { view } = state;
    const segment = ((_b = (_a2 = state.style) == null ? void 0 : _a2.segment) != null ? _b : EntityRelationConnectorConfig.segment) * view.scale;
    const pts = state.absolutePoints;
    const p0 = pts[0];
    const pe = pts[pts.length - 1];
    let isSourceLeft = false;
    if (source != null) {
      const sourceGeometry = source.cell.getGeometry();
      if (sourceGeometry.relative) {
        isSourceLeft = sourceGeometry.x <= 0.5;
      } else if (target != null) {
        isSourceLeft = (pe != null ? pe.x : target.x + target.width) < (p0 != null ? p0.x : source.x);
      }
    }
    if (p0 != null) {
      source = new CellState_default();
      source.x = p0.x;
      source.y = p0.y;
    } else if (source != null) {
      const constraint = getPortConstraints(source, state, true, DIRECTION_MASK.NONE);
      if (constraint !== DIRECTION_MASK.NONE && constraint !== DIRECTION_MASK.WEST + DIRECTION_MASK.EAST) {
        isSourceLeft = constraint === DIRECTION_MASK.WEST;
      }
    } else {
      return;
    }
    let isTargetLeft = true;
    if (target != null) {
      const targetGeometry = target.cell.getGeometry();
      if (targetGeometry.relative) {
        isTargetLeft = targetGeometry.x <= 0.5;
      } else if (source != null) {
        isTargetLeft = (p0 != null ? p0.x : source.x + source.width) < (pe != null ? pe.x : target.x);
      }
    }
    if (pe != null) {
      target = new CellState_default();
      target.x = pe.x;
      target.y = pe.y;
    } else if (target != null) {
      const constraint = getPortConstraints(target, state, false, DIRECTION_MASK.NONE);
      if (constraint !== DIRECTION_MASK.NONE && constraint != DIRECTION_MASK.WEST + DIRECTION_MASK.EAST) {
        isTargetLeft = constraint === DIRECTION_MASK.WEST;
      }
    }
    if (source != null && target != null) {
      const x0 = isSourceLeft ? source.x : source.x + source.width;
      const y0 = view.getRoutingCenterY(source);
      const xe = isTargetLeft ? target.x : target.x + target.width;
      const ye = view.getRoutingCenterY(target);
      const seg = segment;
      let dx = isSourceLeft ? -seg : seg;
      const dep = new Point_default(x0 + dx, y0);
      dx = isTargetLeft ? -seg : seg;
      const arr = new Point_default(xe + dx, ye);
      if (isSourceLeft === isTargetLeft) {
        const x = isSourceLeft ? Math.min(x0, xe) - segment : Math.max(x0, xe) + segment;
        result.push(new Point_default(x, y0));
        result.push(new Point_default(x, ye));
      } else if (dep.x < arr.x === isSourceLeft) {
        const midY = y0 + (ye - y0) / 2;
        result.push(dep);
        result.push(new Point_default(dep.x, midY));
        result.push(new Point_default(arr.x, midY));
        result.push(arr);
      } else {
        result.push(dep);
        result.push(arr);
      }
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/style/edge/Loop.js
  var Loop = (state, source, _target, points, result) => {
    var _a2, _b, _c;
    const pts = state.absolutePoints;
    const p0 = pts[0];
    const pe = pts[pts.length - 1];
    if (p0 != null && pe != null) {
      if (points != null && points.length > 0) {
        for (let i = 0; i < points.length; i += 1) {
          let pt = points[i];
          pt = state.view.transformControlPoint(state, pt);
          result.push(new Point_default(pt.x, pt.y));
        }
      }
      return;
    }
    if (source != null) {
      const { view } = state;
      const { graph } = view;
      let pt = points != null && points.length > 0 ? points[0] : null;
      if (pt != null) {
        pt = view.transformControlPoint(state, pt);
        if (contains(source, pt.x, pt.y)) {
          pt = null;
        }
      }
      let x = 0;
      let dx = 0;
      let y = 0;
      let dy = 0;
      const seg = ((_a2 = state.style.segment) != null ? _a2 : graph.gridSize) * view.scale;
      const dir = (_c = (_b = state.style) == null ? void 0 : _b.direction) != null ? _c : "west";
      if (dir === "north" || dir === "south") {
        x = view.getRoutingCenterX(source);
        dx = seg;
      } else {
        y = view.getRoutingCenterY(source);
        dy = seg;
      }
      if (pt == null || pt.x < source.x || pt.x > source.x + source.width) {
        if (pt != null) {
          x = pt.x;
          dy = Math.max(Math.abs(y - pt.y), dy);
        } else
          switch (dir) {
            case "north": {
              y = source.y - 2 * dx;
              break;
            }
            case "south": {
              y = source.y + source.height + 2 * dx;
              break;
            }
            case "east": {
              x = source.x - 2 * dy;
              break;
            }
            default: {
              x = source.x + source.width + 2 * dy;
            }
          }
      } else if (pt !== null) {
        x = view.getRoutingCenterX(source);
        dx = Math.max(Math.abs(x - pt.x), dy);
        y = pt.y;
        dy = 0;
      }
      result.push(new Point_default(x - dx, y - dy));
      result.push(new Point_default(x + dx, y + dy));
    }
  };

  // node_modules/@maxgraph/core/lib/esm/util/arrayUtils.js
  var equalPoints = (a, b) => {
    if (!a && b || a && !b || a && b && a.length != b.length) {
      return false;
    }
    if (a && b) {
      for (let i = 0; i < a.length; i += 1) {
        const p = a[i];
        if (!p || p && !p.equals(b[i]))
          return false;
      }
    }
    return true;
  };
  var equalEntries = (a, b) => {
    let count = 0;
    if (!a && b || a && !b || a && b && a.length != b.length) {
      return false;
    }
    if (a && b) {
      for (const key in b) {
        count++;
      }
      for (const key in a) {
        count--;
        if ((!Number.isNaN(a[key]) || !Number.isNaN(b[key])) && a[key] !== b[key]) {
          return false;
        }
      }
    }
    return count === 0;
  };
  var removeDuplicates = (arr) => {
    const coveredEntries = /* @__PURE__ */ new Map();
    const result = [];
    for (let i = 0; i < arr.length; i += 1) {
      if (!coveredEntries.get(arr[i])) {
        result.push(arr[i]);
        coveredEntries.set(arr[i], true);
      }
    }
    return result;
  };

  // node_modules/@maxgraph/core/lib/esm/view/geometry/Geometry.js
  var Geometry = class extends Rectangle_default {
    constructor(x = 0, y = 0, width = 0, height = 0) {
      super(x, y, width, height);
      this.TRANSLATE_CONTROL_POINTS = true;
      this.alternateBounds = null;
      this.sourcePoint = null;
      this.targetPoint = null;
      this.points = null;
      this.offset = null;
      this.relative = false;
    }
    setRelative(isRelative) {
      this.relative = isRelative;
    }
    /**
     * Swaps the x, y, width and height with the values stored in
     * {@link alternateBounds} and puts the previous values into {@link alternateBounds} as
     * a rectangle. This operation is carried-out in-place, that is, using the
     * existing geometry instance. If this operation is called during a graph
     * model transactional change, then the geometry should be cloned before
     * calling this method and setting the geometry of the cell using
     * {@link GraphDataModel.setGeometry}.
     */
    swap() {
      if (this.alternateBounds) {
        const old = new Rectangle_default(this.x, this.y, this.width, this.height);
        this.x = this.alternateBounds.x;
        this.y = this.alternateBounds.y;
        this.width = this.alternateBounds.width;
        this.height = this.alternateBounds.height;
        this.alternateBounds = old;
      }
    }
    /**
     * Returns the {@link Point} representing the source or target point of this
     * edge. This is only used if the edge has no source or target vertex.
     *
     * @param {Boolean} isSource that specifies if the source or target point should be returned.
     */
    getTerminalPoint(isSource) {
      return isSource ? this.sourcePoint : this.targetPoint;
    }
    /**
     * Sets the {@link sourcePoint} or {@link targetPoint} to the given {@link Point} and
     * returns the new point.
     *
     * @param {Point} point to be used as the new source or target point.
     * @param {Boolean} isSource that specifies if the source or target point should be set.
     */
    setTerminalPoint(point, isSource) {
      if (isSource) {
        this.sourcePoint = point;
      } else {
        this.targetPoint = point;
      }
      return point;
    }
    /**
     * Rotates the geometry by the given angle around the given center. That is,
     * {@link x} and {@link y} of the geometry, the {@link sourcePoint}, {@link targetPoint} and all
     * {@link points} are translated by the given amount. {@link x} and {@link y} are only
     * translated if {@link relative} is false.
     *
     * @param {Number} angle that specifies the rotation angle in degrees.
     * @param {Point} cx   that specifies the center of the rotation.
     */
    rotate(angle, cx) {
      const rad = toRadians(angle);
      const cos = Math.cos(rad);
      const sin = Math.sin(rad);
      if (!this.relative) {
        const ct = new Point_default(this.getCenterX(), this.getCenterY());
        const pt = getRotatedPoint(ct, cos, sin, cx);
        this.x = Math.round(pt.x - this.width / 2);
        this.y = Math.round(pt.y - this.height / 2);
      }
      if (this.sourcePoint) {
        const pt = getRotatedPoint(this.sourcePoint, cos, sin, cx);
        this.sourcePoint.x = Math.round(pt.x);
        this.sourcePoint.y = Math.round(pt.y);
      }
      if (this.targetPoint) {
        const pt = getRotatedPoint(this.targetPoint, cos, sin, cx);
        this.targetPoint.x = Math.round(pt.x);
        this.targetPoint.y = Math.round(pt.y);
      }
      if (this.points) {
        for (let i = 0; i < this.points.length; i += 1) {
          if (this.points[i]) {
            const pt = getRotatedPoint(this.points[i], cos, sin, cx);
            this.points[i].x = Math.round(pt.x);
            this.points[i].y = Math.round(pt.y);
          }
        }
      }
    }
    /**
     * Translates the geometry by the specified amount. That is, {@link x} and {@link y} of the
     * geometry, the {@link sourcePoint}, {@link targetPoint} and all {@link points} are translated
     * by the given amount. {@link x} and {@link y} are only translated if {@link relative} is false.
     * If {@link TRANSLATE_CONTROL_POINTS} is false, then {@link points} are not modified by
     * this function.
     *
     * @param {Number} dx that specifies the x-coordinate of the translation.
     * @param {Number} dy that specifies the y-coordinate of the translation.
     */
    translate(dx, dy) {
      if (!this.relative) {
        this.x += dx;
        this.y += dy;
      }
      if (this.sourcePoint) {
        this.sourcePoint.x = this.sourcePoint.x + dx;
        this.sourcePoint.y = this.sourcePoint.y + dy;
      }
      if (this.targetPoint) {
        this.targetPoint.x = this.targetPoint.x + dx;
        this.targetPoint.y = this.targetPoint.y + dy;
      }
      if (this.TRANSLATE_CONTROL_POINTS && this.points) {
        for (let i = 0; i < this.points.length; i += 1) {
          if (this.points[i]) {
            this.points[i].x = this.points[i].x + dx;
            this.points[i].y = this.points[i].y + dy;
          }
        }
      }
    }
    /**
     * Scales the geometry by the given amount. That is, {@link x} and {@link y} of the
     * geometry, the {@link sourcePoint}, {@link targetPoint} and all {@link points} are scaled
     * by the given amount. {@link x}, {@link y}, {@link width} and {@link height} are only scaled if
     * {@link relative} is false. If {@link fixedAspect} is true, then the smaller value
     * is used to scale the width and the height.
     *
     * @param {Number} sx that specifies the horizontal scale factor.
     * @param {Number} sy that specifies the vertical scale factor.
     * @param {Optional} fixedAspect boolean to keep the aspect ratio fixed.
     */
    scale(sx, sy, fixedAspect) {
      if (this.sourcePoint) {
        this.sourcePoint.x = this.sourcePoint.x * sx;
        this.sourcePoint.y = this.sourcePoint.y * sy;
      }
      if (this.targetPoint) {
        this.targetPoint.x = this.targetPoint.x * sx;
        this.targetPoint.y = this.targetPoint.y * sy;
      }
      if (this.points) {
        for (let i = 0; i < this.points.length; i += 1) {
          if (this.points[i]) {
            this.points[i].x = this.points[i].x * sx;
            this.points[i].y = this.points[i].y * sy;
          }
        }
      }
      if (!this.relative) {
        this.x *= sx;
        this.y *= sy;
        if (fixedAspect) {
          sy = sx = Math.min(sx, sy);
        }
        this.width *= sx;
        this.height *= sy;
      }
    }
    /**
     * Returns true if the given object equals this geometry.
     */
    equals(geom) {
      var _a2, _b, _c, _d;
      if (!geom)
        return false;
      return super.equals(geom) && this.relative === geom.relative && (this.sourcePoint === null && geom.sourcePoint === null || !!((_a2 = this.sourcePoint) == null ? void 0 : _a2.equals(geom.sourcePoint))) && (this.targetPoint === null && geom.targetPoint === null || !!((_b = this.targetPoint) == null ? void 0 : _b.equals(geom.targetPoint))) && equalPoints(this.points, geom.points) && (this.alternateBounds === null && geom.alternateBounds === null || !!((_c = this.alternateBounds) == null ? void 0 : _c.equals(geom.alternateBounds))) && (this.offset === null && geom.offset === null || !!((_d = this.offset) == null ? void 0 : _d.equals(geom.offset)));
    }
    clone() {
      return clone(this);
    }
  };
  var Geometry_default = Geometry;

  // node_modules/@maxgraph/core/lib/esm/view/style/edge/shared.js
  function scalePointArray(points, scale) {
    let result = [];
    if (points != null) {
      for (let i = 0; i < points.length; i += 1) {
        if (points[i] != null) {
          result[i] = new Point_default(Math.round(points[i].x / scale * 10) / 10, Math.round(points[i].y / scale * 10) / 10);
        } else {
          result[i] = null;
        }
      }
    } else {
      result = null;
    }
    return result;
  }
  function scaleCellState(state, scale) {
    let result = null;
    if (state != null) {
      result = state.clone();
      result.setRect(Math.round(state.x / scale * 10) / 10, Math.round(state.y / scale * 10) / 10, Math.round(state.width / scale * 10) / 10, Math.round(state.height / scale * 10) / 10);
    }
    return result;
  }

  // node_modules/@maxgraph/core/lib/esm/view/style/edge/Segment.js
  var SegmentConnector = (state, sourceScaled, targetScaled, controlHints, result) => {
    const pts = scalePointArray(state.absolutePoints, state.view.scale);
    const source = scaleCellState(sourceScaled, state.view.scale);
    const target = scaleCellState(targetScaled, state.view.scale);
    const tol = 1;
    let lastPushed = result.length > 0 ? result[0] : null;
    let horizontal = true;
    let hint = null;
    function pushPoint(pt2) {
      pt2.x = Math.round(pt2.x * state.view.scale * 10) / 10;
      pt2.y = Math.round(pt2.y * state.view.scale * 10) / 10;
      if (lastPushed == null || Math.abs(lastPushed.x - pt2.x) >= tol || Math.abs(lastPushed.y - pt2.y) >= Math.max(1, state.view.scale)) {
        result.push(pt2);
        lastPushed = pt2;
      }
      return lastPushed;
    }
    let pt = pts[0];
    if (pt == null && source != null) {
      pt = new Point_default(state.view.getRoutingCenterX(source), state.view.getRoutingCenterY(source));
    } else if (pt != null) {
      pt = pt.clone();
    }
    const lastInx = pts.length - 1;
    let pe = null;
    if (controlHints != null && controlHints.length > 0) {
      let hints = [];
      for (let i = 0; i < controlHints.length; i += 1) {
        const tmp = state.view.transformControlPoint(state, controlHints[i], true);
        if (tmp != null) {
          hints.push(tmp);
        }
      }
      if (hints.length === 0) {
        return;
      }
      if (pt != null && hints[0] != null) {
        if (Math.abs(hints[0].x - pt.x) < tol) {
          hints[0].x = pt.x;
        }
        if (Math.abs(hints[0].y - pt.y) < tol) {
          hints[0].y = pt.y;
        }
      }
      pe = pts[lastInx];
      if (pe != null && hints[hints.length - 1] != null) {
        if (Math.abs(hints[hints.length - 1].x - pe.x) < tol) {
          hints[hints.length - 1].x = pe.x;
        }
        if (Math.abs(hints[hints.length - 1].y - pe.y) < tol) {
          hints[hints.length - 1].y = pe.y;
        }
      }
      hint = hints[0];
      let currentTerm = source;
      let currentPt = pts[0];
      let hozChan = false;
      let vertChan = false;
      let currentHint = hint;
      if (currentPt != null) {
        currentTerm = null;
      }
      for (let i = 0; i < 2; i += 1) {
        const fixedVertAlign = currentPt != null && currentPt.x === currentHint.x;
        const fixedHozAlign = currentPt != null && currentPt.y === currentHint.y;
        const inHozChan = currentTerm != null && currentHint.y >= currentTerm.y && currentHint.y <= currentTerm.y + currentTerm.height;
        const inVertChan = currentTerm != null && currentHint.x >= currentTerm.x && currentHint.x <= currentTerm.x + currentTerm.width;
        hozChan = fixedHozAlign || currentPt == null && inHozChan;
        vertChan = fixedVertAlign || currentPt == null && inVertChan;
        if (!(i == 0 && (hozChan && vertChan || fixedVertAlign && fixedHozAlign))) {
          if (currentPt != null && !fixedHozAlign && !fixedVertAlign && (inHozChan || inVertChan)) {
            horizontal = !inHozChan;
            break;
          }
          if (vertChan || hozChan) {
            horizontal = hozChan;
            if (i === 1) {
              horizontal = hints.length % 2 === 0 ? hozChan : vertChan;
            }
            break;
          }
        }
        currentTerm = target;
        currentPt = pts[lastInx];
        if (currentPt != null) {
          currentTerm = null;
        }
        currentHint = hints[hints.length - 1];
        if (fixedVertAlign && fixedHozAlign) {
          hints = hints.slice(1);
        }
      }
      if (horizontal && (pts[0] != null && pts[0].y !== hint.y || pts[0] == null && source != null && (hint.y < source.y || hint.y > source.y + source.height))) {
        pushPoint(new Point_default(pt.x, hint.y));
      } else if (!horizontal && (pts[0] != null && pts[0].x !== hint.x || pts[0] == null && source != null && (hint.x < source.x || hint.x > source.x + source.width))) {
        pushPoint(new Point_default(hint.x, pt.y));
      }
      if (horizontal) {
        pt.y = hint.y;
      } else {
        pt.x = hint.x;
      }
      for (let i = 0; i < hints.length; i += 1) {
        horizontal = !horizontal;
        hint = hints[i];
        if (horizontal) {
          pt.y = hint.y;
        } else {
          pt.x = hint.x;
        }
        pushPoint(pt.clone());
      }
    } else {
      hint = pt;
      horizontal = true;
    }
    pt = pts[lastInx];
    if (pt == null && target != null) {
      pt = new Point_default(state.view.getRoutingCenterX(target), state.view.getRoutingCenterY(target));
    }
    if (pt != null) {
      if (hint != null) {
        if (horizontal && (pts[lastInx] != null && pts[lastInx].y !== hint.y || pts[lastInx] == null && target != null && (hint.y < target.y || hint.y > target.y + target.height))) {
          pushPoint(new Point_default(pt.x, hint.y));
        } else if (!horizontal && (pts[lastInx] != null && pts[lastInx].x !== hint.x || pts[lastInx] == null && target != null && (hint.x < target.x || hint.x > target.x + target.width))) {
          pushPoint(new Point_default(hint.x, pt.y));
        }
      }
    }
    if (pts[0] == null && source != null) {
      while (result.length > 1 && result[1] != null && contains(source, result[1].x, result[1].y)) {
        result.splice(1, 1);
      }
    }
    if (pts[lastInx] == null && target != null) {
      while (result.length > 1 && result[result.length - 1] != null && contains(target, result[result.length - 1].x, result[result.length - 1].y)) {
        result.splice(result.length - 1, 1);
      }
    }
    if (pe != null && result[result.length - 1] != null && Math.abs(pe.x - result[result.length - 1].x) <= tol && Math.abs(pe.y - result[result.length - 1].y) <= tol) {
      result.splice(result.length - 1, 1);
      if (result[result.length - 1] != null) {
        if (Math.abs(result[result.length - 1].x - pe.x) < tol) {
          result[result.length - 1].x = pe.x;
        }
        if (Math.abs(result[result.length - 1].y - pe.y) < tol) {
          result[result.length - 1].y = pe.y;
        }
      }
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/style/edge/Orthogonal.js
  var dirVectors = [
    [-1, 0],
    [0, -1],
    [1, 0],
    [0, 1],
    [-1, 0],
    [0, -1],
    [1, 0]
  ];
  var wayPoints1 = [
    [0, 0],
    [0, 0],
    [0, 0],
    [0, 0],
    [0, 0],
    [0, 0],
    [0, 0],
    [0, 0],
    [0, 0],
    [0, 0],
    [0, 0],
    [0, 0]
  ];
  var routePatterns = [
    [
      [513, 2308, 2081, 2562],
      [513, 1090, 514, 2184, 2114, 2561],
      [513, 1090, 514, 2564, 2184, 2562],
      [513, 2308, 2561, 1090, 514, 2568, 2308]
    ],
    [
      [514, 1057, 513, 2308, 2081, 2562],
      [514, 2184, 2114, 2561],
      [514, 2184, 2562, 1057, 513, 2564, 2184],
      [514, 1057, 513, 2568, 2308, 2561]
    ],
    [
      [1090, 514, 1057, 513, 2308, 2081, 2562],
      [2114, 2561],
      [1090, 2562, 1057, 513, 2564, 2184],
      [1090, 514, 1057, 513, 2308, 2561, 2568]
    ],
    [
      [2081, 2562],
      [1057, 513, 1090, 514, 2184, 2114, 2561],
      [1057, 513, 1090, 514, 2184, 2562, 2564],
      [1057, 2561, 1090, 514, 2568, 2308]
    ]
  ];
  var vertexSeparations = [];
  var limits = [
    [0, 0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0]
  ];
  var SIDE_MASK = 480;
  var CENTER_MASK = 512;
  var SOURCE_MASK = 1024;
  var TARGET_MASK = 2048;
  function getJettySize(state, isSource) {
    var _a2, _b, _c, _d;
    const buffer = OrthogonalConnectorConfig.buffer;
    let value = (_b = (_a2 = isSource ? state.style.sourceJettySize : state.style.targetJettySize) != null ? _a2 : state.style.jettySize) != null ? _b : buffer;
    if (value === "auto") {
      const type = (_c = isSource ? state.style.startArrow : state.style.endArrow) != null ? _c : NONE;
      if (type !== NONE) {
        const size = (_d = isSource ? state.style.startSize : state.style.endSize) != null ? _d : StyleDefaultsConfig.markerSize;
        value = Math.max(2, Math.ceil((size + buffer) / buffer)) * buffer;
      } else {
        value = 2 * buffer;
      }
    }
    return value;
  }
  var OrthogonalConnector = (state, sourceScaled, targetScaled, controlHints, result) => {
    var _a2, _b;
    const pts = scalePointArray(state.absolutePoints, state.view.scale);
    const source = scaleCellState(sourceScaled, state.view.scale);
    const target = scaleCellState(targetScaled, state.view.scale);
    const sourceEdge = source == null ? false : source.cell.isEdge();
    const targetEdge = target == null ? false : target.cell.isEdge();
    const p0 = pts[0];
    const pe = pts[pts.length - 1];
    let sourceX = source != null ? source.x : p0.x;
    let sourceY = source != null ? source.y : p0.y;
    let sourceWidth = source != null ? source.width : 0;
    let sourceHeight = source != null ? source.height : 0;
    let targetX = target != null ? target.x : pe.x;
    let targetY = target != null ? target.y : pe.y;
    let targetWidth = target != null ? target.width : 0;
    let targetHeight = target != null ? target.height : 0;
    let sourceBuffer = getJettySize(state, true);
    let targetBuffer = getJettySize(state, false);
    if (source != null && target === source) {
      targetBuffer = Math.max(sourceBuffer, targetBuffer);
      sourceBuffer = targetBuffer;
    }
    const totalBuffer = targetBuffer + sourceBuffer;
    let tooShort = false;
    if (p0 != null && pe != null) {
      const dx2 = pe.x - p0.x;
      const dy2 = pe.y - p0.y;
      tooShort = dx2 * dx2 + dy2 * dy2 < totalBuffer * totalBuffer;
    }
    if (tooShort || OrthogonalConnectorConfig.pointsFallback && controlHints != null && controlHints.length > 0 || sourceEdge || targetEdge) {
      SegmentConnector(state, sourceScaled, targetScaled, controlHints, result);
      return;
    }
    const portConstraint = [DIRECTION_MASK.ALL, DIRECTION_MASK.ALL];
    let rotation = 0;
    if (source != null) {
      portConstraint[0] = getPortConstraints(source, state, true, DIRECTION_MASK.ALL);
      rotation = (_a2 = source.style.rotation) != null ? _a2 : 0;
      if (rotation !== 0) {
        const newRect = getBoundingBox(new Rectangle_default(sourceX, sourceY, sourceWidth, sourceHeight), rotation);
        sourceX = newRect.x;
        sourceY = newRect.y;
        sourceWidth = newRect.width;
        sourceHeight = newRect.height;
      }
    }
    if (target != null) {
      portConstraint[1] = getPortConstraints(target, state, false, DIRECTION_MASK.ALL);
      rotation = (_b = target.style.rotation) != null ? _b : 0;
      if (rotation !== 0) {
        const newRect = getBoundingBox(new Rectangle_default(targetX, targetY, targetWidth, targetHeight), rotation);
        targetX = newRect.x;
        targetY = newRect.y;
        targetWidth = newRect.width;
        targetHeight = newRect.height;
      }
    }
    const dir = [0, 0];
    const geo = [
      [sourceX, sourceY, sourceWidth, sourceHeight],
      [targetX, targetY, targetWidth, targetHeight]
    ];
    const buffer = [sourceBuffer, targetBuffer];
    for (let i = 0; i < 2; i += 1) {
      limits[i][1] = geo[i][0] - buffer[i];
      limits[i][2] = geo[i][1] - buffer[i];
      limits[i][4] = geo[i][0] + geo[i][2] + buffer[i];
      limits[i][8] = geo[i][1] + geo[i][3] + buffer[i];
    }
    const sourceCenX = geo[0][0] + geo[0][2] / 2;
    const sourceCenY = geo[0][1] + geo[0][3] / 2;
    const targetCenX = geo[1][0] + geo[1][2] / 2;
    const targetCenY = geo[1][1] + geo[1][3] / 2;
    const dx = sourceCenX - targetCenX;
    const dy = sourceCenY - targetCenY;
    let quad = 0;
    if (dx < 0) {
      if (dy < 0) {
        quad = 2;
      } else {
        quad = 1;
      }
    } else if (dy <= 0) {
      quad = 3;
      if (dx === 0) {
        quad = 2;
      }
    }
    let currentTerm = null;
    if (source != null) {
      currentTerm = p0;
    }
    const constraint = [
      [0.5, 0.5],
      [0.5, 0.5]
    ];
    for (let i = 0; i < 2; i += 1) {
      if (currentTerm != null) {
        constraint[i][0] = (currentTerm.x - geo[i][0]) / geo[i][2];
        if (Math.abs(currentTerm.x - geo[i][0]) <= 1) {
          dir[i] = DIRECTION_MASK.WEST;
        } else if (Math.abs(currentTerm.x - geo[i][0] - geo[i][2]) <= 1) {
          dir[i] = DIRECTION_MASK.EAST;
        }
        constraint[i][1] = (currentTerm.y - geo[i][1]) / geo[i][3];
        if (Math.abs(currentTerm.y - geo[i][1]) <= 1) {
          dir[i] = DIRECTION_MASK.NORTH;
        } else if (Math.abs(currentTerm.y - geo[i][1] - geo[i][3]) <= 1) {
          dir[i] = DIRECTION_MASK.SOUTH;
        }
      }
      currentTerm = null;
      if (target != null) {
        currentTerm = pe;
      }
    }
    const sourceTopDist = geo[0][1] - (geo[1][1] + geo[1][3]);
    const sourceLeftDist = geo[0][0] - (geo[1][0] + geo[1][2]);
    const sourceBottomDist = geo[1][1] - (geo[0][1] + geo[0][3]);
    const sourceRightDist = geo[1][0] - (geo[0][0] + geo[0][2]);
    vertexSeparations[1] = Math.max(sourceLeftDist - totalBuffer, 0);
    vertexSeparations[2] = Math.max(sourceTopDist - totalBuffer, 0);
    vertexSeparations[4] = Math.max(sourceBottomDist - totalBuffer, 0);
    vertexSeparations[3] = Math.max(sourceRightDist - totalBuffer, 0);
    const dirPref = [];
    const horPref = [];
    const vertPref = [];
    horPref[0] = sourceLeftDist >= sourceRightDist ? DIRECTION_MASK.WEST : DIRECTION_MASK.EAST;
    vertPref[0] = sourceTopDist >= sourceBottomDist ? DIRECTION_MASK.NORTH : DIRECTION_MASK.SOUTH;
    horPref[1] = reversePortConstraints(horPref[0]);
    vertPref[1] = reversePortConstraints(vertPref[0]);
    const preferredHorizDist = sourceLeftDist >= sourceRightDist ? sourceLeftDist : sourceRightDist;
    const preferredVertDist = sourceTopDist >= sourceBottomDist ? sourceTopDist : sourceBottomDist;
    const prefOrdering = [
      [0, 0],
      [0, 0]
    ];
    let preferredOrderSet = false;
    for (let i = 0; i < 2; i += 1) {
      if (dir[i] !== 0) {
        continue;
      }
      if ((horPref[i] & portConstraint[i]) === 0) {
        horPref[i] = reversePortConstraints(horPref[i]);
      }
      if ((vertPref[i] & portConstraint[i]) === 0) {
        vertPref[i] = reversePortConstraints(vertPref[i]);
      }
      prefOrdering[i][0] = vertPref[i];
      prefOrdering[i][1] = horPref[i];
    }
    if (preferredVertDist > 0 && preferredHorizDist > 0) {
      if ((horPref[0] & portConstraint[0]) > 0 && (vertPref[1] & portConstraint[1]) > 0) {
        prefOrdering[0][0] = horPref[0];
        prefOrdering[0][1] = vertPref[0];
        prefOrdering[1][0] = vertPref[1];
        prefOrdering[1][1] = horPref[1];
        preferredOrderSet = true;
      } else if ((vertPref[0] & portConstraint[0]) > 0 && (horPref[1] & portConstraint[1]) > 0) {
        prefOrdering[0][0] = vertPref[0];
        prefOrdering[0][1] = horPref[0];
        prefOrdering[1][0] = horPref[1];
        prefOrdering[1][1] = vertPref[1];
        preferredOrderSet = true;
      }
    }
    if (preferredVertDist > 0 && !preferredOrderSet) {
      prefOrdering[0][0] = vertPref[0];
      prefOrdering[0][1] = horPref[0];
      prefOrdering[1][0] = vertPref[1];
      prefOrdering[1][1] = horPref[1];
      preferredOrderSet = true;
    }
    if (preferredHorizDist > 0 && !preferredOrderSet) {
      prefOrdering[0][0] = horPref[0];
      prefOrdering[0][1] = vertPref[0];
      prefOrdering[1][0] = horPref[1];
      prefOrdering[1][1] = vertPref[1];
      preferredOrderSet = true;
    }
    for (let i = 0; i < 2; i += 1) {
      if (dir[i] !== 0) {
        continue;
      }
      if ((prefOrdering[i][0] & portConstraint[i]) === 0) {
        prefOrdering[i][0] = prefOrdering[i][1];
      }
      dirPref[i] = prefOrdering[i][0] & portConstraint[i];
      dirPref[i] |= (prefOrdering[i][1] & portConstraint[i]) << 8;
      dirPref[i] |= (prefOrdering[1 - i][i] & portConstraint[i]) << 16;
      dirPref[i] |= (prefOrdering[1 - i][1 - i] & portConstraint[i]) << 24;
      if ((dirPref[i] & 15) === 0) {
        dirPref[i] = dirPref[i] << 8;
      }
      if ((dirPref[i] & 3840) === 0) {
        dirPref[i] = dirPref[i] & 15 | dirPref[i] >> 8;
      }
      if ((dirPref[i] & 983040) === 0) {
        dirPref[i] = dirPref[i] & 65535 | (dirPref[i] & 251658240) >> 8;
      }
      dir[i] = dirPref[i] & 15;
      if (portConstraint[i] === DIRECTION_MASK.WEST || portConstraint[i] === DIRECTION_MASK.NORTH || portConstraint[i] === DIRECTION_MASK.EAST || portConstraint[i] === DIRECTION_MASK.SOUTH) {
        dir[i] = portConstraint[i];
      }
    }
    let sourceIndex = dir[0] === DIRECTION_MASK.EAST ? 3 : dir[0];
    let targetIndex = dir[1] === DIRECTION_MASK.EAST ? 3 : dir[1];
    sourceIndex -= quad;
    targetIndex -= quad;
    if (sourceIndex < 1) {
      sourceIndex += 4;
    }
    if (targetIndex < 1) {
      targetIndex += 4;
    }
    const routePattern = routePatterns[sourceIndex - 1][targetIndex - 1];
    wayPoints1[0][0] = geo[0][0];
    wayPoints1[0][1] = geo[0][1];
    switch (dir[0]) {
      case DIRECTION_MASK.WEST:
        wayPoints1[0][0] -= sourceBuffer;
        wayPoints1[0][1] += constraint[0][1] * geo[0][3];
        break;
      case DIRECTION_MASK.SOUTH:
        wayPoints1[0][0] += constraint[0][0] * geo[0][2];
        wayPoints1[0][1] += geo[0][3] + sourceBuffer;
        break;
      case DIRECTION_MASK.EAST:
        wayPoints1[0][0] += geo[0][2] + sourceBuffer;
        wayPoints1[0][1] += constraint[0][1] * geo[0][3];
        break;
      case DIRECTION_MASK.NORTH:
        wayPoints1[0][0] += constraint[0][0] * geo[0][2];
        wayPoints1[0][1] -= sourceBuffer;
        break;
    }
    let currentIndex = 0;
    let lastOrientation = (dir[0] & (DIRECTION_MASK.EAST | DIRECTION_MASK.WEST)) > 0 ? 0 : 1;
    const initialOrientation = lastOrientation;
    let currentOrientation = 0;
    for (let i = 0; i < routePattern.length; i += 1) {
      const nextDirection = routePattern[i] & 15;
      let directionIndex = nextDirection === DIRECTION_MASK.EAST ? 3 : nextDirection;
      directionIndex += quad;
      if (directionIndex > 4) {
        directionIndex -= 4;
      }
      const direction = dirVectors[directionIndex - 1];
      currentOrientation = directionIndex % 2 > 0 ? 0 : 1;
      if (currentOrientation !== lastOrientation) {
        currentIndex++;
        wayPoints1[currentIndex][0] = wayPoints1[currentIndex - 1][0];
        wayPoints1[currentIndex][1] = wayPoints1[currentIndex - 1][1];
      }
      const tar = (routePattern[i] & TARGET_MASK) > 0;
      const sou = (routePattern[i] & SOURCE_MASK) > 0;
      let side = (routePattern[i] & SIDE_MASK) >> 5;
      side <<= quad;
      if (side > 15) {
        side >>= 4;
      }
      const center = (routePattern[i] & CENTER_MASK) > 0;
      if ((sou || tar) && side < 9) {
        let limit = 0;
        const souTar = sou ? 0 : 1;
        if (center && currentOrientation === 0) {
          limit = geo[souTar][0] + constraint[souTar][0] * geo[souTar][2];
        } else if (center) {
          limit = geo[souTar][1] + constraint[souTar][1] * geo[souTar][3];
        } else {
          limit = limits[souTar][side];
        }
        if (currentOrientation === 0) {
          const lastX = wayPoints1[currentIndex][0];
          const deltaX = (limit - lastX) * direction[0];
          if (deltaX > 0) {
            wayPoints1[currentIndex][0] += direction[0] * deltaX;
          }
        } else {
          const lastY = wayPoints1[currentIndex][1];
          const deltaY = (limit - lastY) * direction[1];
          if (deltaY > 0) {
            wayPoints1[currentIndex][1] += direction[1] * deltaY;
          }
        }
      } else if (center) {
        wayPoints1[currentIndex][0] += direction[0] * Math.abs(vertexSeparations[directionIndex] / 2);
        wayPoints1[currentIndex][1] += direction[1] * Math.abs(vertexSeparations[directionIndex] / 2);
      }
      if (currentIndex > 0 && wayPoints1[currentIndex][currentOrientation] === wayPoints1[currentIndex - 1][currentOrientation]) {
        currentIndex--;
      } else {
        lastOrientation = currentOrientation;
      }
    }
    for (let i = 0; i <= currentIndex; i += 1) {
      if (i === currentIndex) {
        const targetOrientation = (dir[1] & (DIRECTION_MASK.EAST | DIRECTION_MASK.WEST)) > 0 ? 0 : 1;
        const sameOrient = targetOrientation === initialOrientation ? 0 : 1;
        if (sameOrient !== (currentIndex + 1) % 2) {
          break;
        }
      }
      result.push(new Point_default(Math.round(wayPoints1[i][0] * state.view.scale * 10) / 10, Math.round(wayPoints1[i][1] * state.view.scale * 10) / 10));
    }
    let index = 1;
    while (index < result.length) {
      if (result[index - 1] == null || result[index] == null || result[index - 1].x !== result[index].x || result[index - 1].y !== result[index].y) {
        index++;
      } else {
        result.splice(index, 1);
      }
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/style/edge/Manhattan.js
  var ManhattanConnector = (state, source, target, points, result) => {
    function moveAndExpand(target2, source2) {
      target2.x += source2.x || 0;
      target2.y += source2.y || 0;
      target2.width += source2.width || 0;
      target2.height += source2.height || 0;
      return target2;
    }
    function snapCoordinateToGrid(value, gridSize) {
      return gridSize * Math.round(value / gridSize);
    }
    function snapPointToGrid(p, gx, gy) {
      p.x = snapCoordinateToGrid(p.x, gx);
      p.y = snapCoordinateToGrid(p.y, gy || gx);
      return p;
    }
    function isPointInRectangle(rect, p) {
      return p.x >= rect.x && p.x <= rect.x + rect.width && p.y >= rect.y && p.y <= rect.y + rect.height;
    }
    function getRectangleCenter(rect) {
      return new Point_default(rect.x + rect.width / 2, rect.y + rect.height / 2);
    }
    function getDifferencePoint(p1, p2) {
      return new Point_default(p1.x - p2.x, p1.y - p2.y);
    }
    function movePoint(p, moveX, moveY) {
      p.x += moveX || 0;
      p.y += moveY || 0;
      return p;
    }
    function getPointTheta(p1, p2) {
      const p = p2.clone();
      const y = -(p.y - p1.y);
      const x = p.x - p1.x;
      const PRECISION = 10;
      const rad = y.toFixed(PRECISION) == "0" && x.toFixed(PRECISION) == "0" ? 0 : Math.atan2(y, x);
      return 180 * rad / Math.PI;
    }
    function normalizePoint(point) {
      return new Point_default(point.x === 0 ? 0 : Math.abs(point.x) / point.x, point.y === 0 ? 0 : Math.abs(point.y) / point.y);
    }
    function getManhattanDistance(p1, p2) {
      return Math.abs(p2.x - p1.x) + Math.abs(p2.y - p1.y);
    }
    function toPointFromString(pointString) {
      const xy = pointString.split(!pointString.includes("@") ? " " : "@");
      return new Point_default(Number.parseInt(xy[0], 10), Number.parseInt(xy[1], 10));
    }
    function pointToString(point) {
      return `${point.x}@${point.y}`;
    }
    function getCellAbsoluteBounds(cellState) {
      var _a2;
      const graph = cellState.view.graph;
      const cellBounds = (_a2 = graph.getCellBounds(cellState.cell, false, false)) == null ? void 0 : _a2.clone();
      if (!cellBounds)
        return void 0;
      const view = graph.view;
      const { scale, translate: translate2 } = view;
      const { x, y } = translate2;
      const round = (v) => Math.round(v * 10) / 10;
      const res = new Rectangle_default(round(cellBounds.x / scale - x), round(cellBounds.y / scale - y), round(cellBounds.width / scale), round(cellBounds.height / scale));
      return res;
    }
    const mStep = ManhattanConnectorConfig.step;
    const config = {
      // Padding applied on the element bounding boxes
      paddingBox: new Geometry_default(-mStep, -mStep, mStep * 2, mStep * 2),
      // An array of directions to find next points on the route
      directions: [
        {
          offsetX: mStep,
          offsetY: 0,
          cost: mStep,
          angle: normalizeAngle(getPointTheta(new Point_default(0, 0), new Point_default(mStep, 0)))
        },
        {
          offsetX: 0,
          offsetY: mStep,
          cost: mStep,
          angle: normalizeAngle(getPointTheta(new Point_default(0, 0), new Point_default(0, mStep)))
        },
        {
          offsetX: -mStep,
          offsetY: 0,
          cost: mStep,
          angle: normalizeAngle(getPointTheta(new Point_default(0, 0), new Point_default(-mStep, 0)))
        },
        {
          offsetX: 0,
          offsetY: -mStep,
          cost: mStep,
          angle: normalizeAngle(getPointTheta(new Point_default(0, 0), new Point_default(0, -mStep)))
        }
      ],
      directionMap: {
        east: { x: 1, y: 0 },
        south: { x: 0, y: 1 },
        west: { x: -1, y: 0 },
        north: { x: 0, y: -1 }
      },
      // A penalty received for direction change
      penaltiesGenerator: (angle) => {
        if (angle == 45 || angle == 90 || angle == 180)
          return mStep / 2;
        return 0;
      },
      // If a function is provided, it's used to route the link while dragging an end
      // i.e. function(from, to, opts) { return []; }
      draggingRoute: null,
      previousDirAngle: 0
    };
    class ObstacleMap {
      constructor(opt) {
        this.options = opt;
        this.mapGridSize = 100;
        this.map = /* @__PURE__ */ new Map();
      }
      // Builds a map of all elements for quicker obstacle queries
      // The svg is divided to  cells, where each of them holds an information which
      // elements belong to it. When we query whether a point is in an obstacle we don't need
      // to go through all obstacles, we check only those in a particular cell.
      build(source2, target2) {
        const graph = (source2 == null ? void 0 : source2.view.graph) || (target2 == null ? void 0 : target2.view.graph);
        if (!graph)
          return;
        return Array.from(graph.getView().getCellStates()).filter((s) => s.cell && s.cell.isVertex() && !s.cell.isEdge()).map((s) => getCellAbsoluteBounds(s)).map((bbox) => bbox ? moveAndExpand(bbox, this.options.paddingBox) : null).forEach((bbox) => {
          if (!bbox)
            return;
          const origin = snapPointToGrid(new Point_default(bbox.x, bbox.y), this.mapGridSize);
          const corner = snapPointToGrid(new Point_default(bbox.x + bbox.width, bbox.y + bbox.height), this.mapGridSize);
          for (let x = origin.x; x <= corner.x; x += this.mapGridSize) {
            for (let y = origin.y; y <= corner.y; y += this.mapGridSize) {
              const gridKey = x + "@" + y;
              const rectArr = this.map.get(gridKey) || [];
              if (!this.map.has(gridKey))
                this.map.set(gridKey, rectArr);
              rectArr.push(bbox);
            }
          }
        });
      }
      isPointAccessible(point) {
        const mapKey = pointToString(snapPointToGrid(point.clone(), this.mapGridSize));
        const obstacles = this.map.get(mapKey);
        if (obstacles) {
          return obstacles.every((obstacle) => !isPointInRectangle(obstacle, point));
        }
        return true;
      }
    }
    class SortedSet {
      constructor() {
        this.items = [];
        this.hash = /* @__PURE__ */ new Map();
      }
      add(key, value) {
        const hashItem = this.hash.get(key);
        if (hashItem) {
          hashItem.value = value;
          this.items.splice(this.items.indexOf(key), 1);
        } else {
          this.hash.set(key, {
            value,
            open: true
          });
        }
        this.items.push(key);
        this.items.sort((i1, i2) => {
          const hashItem1 = this.hash.get(i1);
          const hashItem2 = this.hash.get(i2);
          if (!hashItem1 || !hashItem2)
            return 0;
          return hashItem1.value - hashItem2.value;
        });
      }
      remove(key) {
        const hashItem = this.hash.get(key);
        if (hashItem)
          hashItem.open = false;
      }
      isOpen(key) {
        const hashItem = this.hash.get(key);
        return hashItem && hashItem.open == true;
      }
      isClose(key) {
        const hashItem = this.hash.get(key);
        return hashItem && hashItem.open == false;
      }
      isEmpty() {
        return this.items.length == 0;
      }
      pop() {
        const key = this.items.shift();
        if (key)
          this.remove(key);
        return key;
      }
    }
    function reconstructRoute(parents, endPoint, startCenter, endCenter) {
      const route = [];
      let previousDirection = normalizePoint(getDifferencePoint(endCenter, endPoint));
      let current = endPoint;
      let parent;
      while (parents[pointToString(current)]) {
        parent = parents[pointToString(current)];
        if (!parent)
          continue;
        const direction = normalizePoint(getDifferencePoint(current, parent));
        if (!direction.equals(previousDirection)) {
          route.unshift(current);
          previousDirection = direction;
        }
        current = parent;
      }
      const startDirection = normalizePoint(getDifferencePoint(current, startCenter));
      if (!startDirection.equals(previousDirection)) {
        route.unshift(current);
      }
      return route;
    }
    function getRectPoints(bbox, directions, opt) {
      const step = ManhattanConnectorConfig.step;
      const center = getRectangleCenter(bbox);
      const res = [];
      for (const direction of directions) {
        const directionPoint = opt.directionMap[direction];
        const x = directionPoint.x * bbox.width / 2;
        const y = directionPoint.y * bbox.height / 2;
        const point = movePoint(center.clone(), x, y);
        if (isPointInRectangle(bbox, point)) {
          movePoint(point, directionPoint.x * step, directionPoint.y * step);
        }
        res.push(snapPointToGrid(point, step));
      }
      return res;
    }
    function normalizeAngle(angle) {
      return angle % 360 + (angle < 0 ? 360 : 0);
    }
    function getDirectionAngle(start, end, directionLength) {
      const q = 360 / directionLength;
      return Math.floor(normalizeAngle(getPointTheta(start, end) + q / 2) / q) * q;
    }
    function getDirectionChange(angle1, angle2) {
      const dirChange = Math.abs(angle1 - angle2);
      return dirChange > 180 ? 360 - dirChange : dirChange;
    }
    function estimateCost(from, endPoints) {
      let min = Infinity;
      for (let i = 0, len = endPoints.length; i < len; i++) {
        const cost = getManhattanDistance(from, endPoints[i]);
        if (cost < min)
          min = cost;
      }
      return min;
    }
    function alignPointToCell(point, edgeState, cellState, isSourceCell) {
      const cellBounds = getCellAbsoluteBounds(cellState);
      const y = isSourceCell ? edgeState.style.exitY : edgeState.style.entryY;
      const onlyHorizontalDirections = isSourceCell ? ManhattanConnectorConfig.startDirections.every((d) => d != "north" && d != "south") : ManhattanConnectorConfig.endDirections.every((d) => d != "north" && d != "south");
      if (y != void 0 && onlyHorizontalDirections) {
        const cellHeight = (cellBounds == null ? void 0 : cellBounds.height) || 0;
        point.y = (cellBounds == null ? void 0 : cellBounds.y) != void 0 ? (cellBounds == null ? void 0 : cellBounds.y) + cellHeight * y : point.y - cellHeight / 2 + cellHeight * y;
      }
      const x = isSourceCell ? edgeState.style.exitX : edgeState.style.entryX;
      const onlyVerticalDirections = isSourceCell ? ManhattanConnectorConfig.startDirections.every((d) => d != "west" && d != "east") : ManhattanConnectorConfig.endDirections.every((d) => d != "west" && d != "east");
      if (x != void 0 && onlyVerticalDirections) {
        const cellWidth = (cellBounds == null ? void 0 : cellBounds.width) || 0;
        point.x = (cellBounds == null ? void 0 : cellBounds.x) != void 0 ? (cellBounds == null ? void 0 : cellBounds.x) + cellWidth * x : point.x - cellWidth / 2 + cellWidth * (x || 0);
      }
    }
    function findRoute(start, end, obstacleMap, opt) {
      const step = ManhattanConnectorConfig.step;
      const startPoints = getRectPoints(start, ManhattanConnectorConfig.startDirections, opt).filter((p) => obstacleMap.isPointAccessible(p));
      const startCenter = snapPointToGrid(getRectangleCenter(start), step);
      const endPoints = getRectPoints(end, ManhattanConnectorConfig.endDirections, opt).filter((p) => obstacleMap.isPointAccessible(p));
      const endCenter = snapPointToGrid(getRectangleCenter(end), step);
      if (startPoints.length > 0 && endPoints.length > 0) {
        const openSet = new SortedSet();
        const parents = {};
        const costs = {};
        startPoints.forEach((p) => {
          const key = pointToString(p);
          openSet.add(key, estimateCost(p, endPoints));
          costs[key] = 0;
        });
        let loopsRemain = ManhattanConnectorConfig.maxLoops;
        const endPointsKeys = endPoints.map((p) => pointToString(p));
        let currentDirectionAngle;
        let previousDirectionAngle;
        while (!openSet.isEmpty() && loopsRemain > 0) {
          const currentKey = openSet.pop();
          if (currentKey == void 0) {
            continue;
          }
          const currentPoint = toPointFromString(currentKey);
          const currentCost = costs[currentKey];
          previousDirectionAngle = currentDirectionAngle;
          currentDirectionAngle = parents[currentKey] ? getDirectionAngle(parents[currentKey], currentPoint, opt.directions.length) : opt.previousDirAngle != 0 ? opt.previousDirAngle : getDirectionAngle(startCenter, currentPoint, opt.directions.length);
          if (endPointsKeys.includes(currentKey)) {
            const directionChangedAngle = getDirectionChange(currentDirectionAngle, getDirectionAngle(currentPoint, endCenter, opt.directions.length));
            if (currentPoint.equals(endCenter) || directionChangedAngle < 180) {
              opt.previousDirAngle = currentDirectionAngle;
              return reconstructRoute(parents, currentPoint, startCenter, endCenter);
            }
          }
          for (let i = 0; i < opt.directions.length; i++) {
            const direction = opt.directions[i];
            const directionChangedAngle = getDirectionChange(currentDirectionAngle, direction.angle);
            if (previousDirectionAngle && directionChangedAngle > ManhattanConnectorConfig.maxAllowedDirectionChange) {
              continue;
            }
            const neighborPoint = movePoint(currentPoint.clone(), direction.offsetX, direction.offsetY);
            const neighborKey = pointToString(neighborPoint);
            if (openSet.isClose(neighborKey) || !obstacleMap.isPointAccessible(neighborPoint)) {
              continue;
            }
            const costFromStart = currentCost + direction.cost + opt.penaltiesGenerator(directionChangedAngle);
            if (!openSet.isOpen(neighborKey) || costFromStart < costs[neighborKey]) {
              parents[neighborKey] = currentPoint;
              costs[neighborKey] = costFromStart;
              openSet.add(neighborKey, costFromStart + estimateCost(neighborPoint, endPoints));
            }
          }
          loopsRemain--;
        }
        return null;
      }
      return null;
    }
    function router(state2, source2, target2, points2, result2, opt) {
      if (points2 != null && points2.length > 0 || source2 == null || target2 == null) {
        SegmentConnector(state2, source2, target2, points2, result2);
        return;
      }
      let sourceBBox = getCellAbsoluteBounds(source2);
      sourceBBox = sourceBBox ? moveAndExpand(sourceBBox, opt.paddingBox) : void 0;
      let targetBBox = getCellAbsoluteBounds(target2);
      targetBBox = targetBBox ? moveAndExpand(targetBBox, opt.paddingBox) : void 0;
      const obstacleMap = new ObstacleMap(opt);
      obstacleMap.build(source2, target2);
      if (!sourceBBox || !targetBBox) {
        return OrthogonalConnector(state2, source2, target2, points2, result2);
      }
      const routePoints = findRoute(sourceBBox, targetBBox, obstacleMap, opt);
      if (routePoints == null || routePoints.length == 0) {
        return OrthogonalConnector(state2, source2, target2, points2, result2);
      }
      if (state2.style) {
        if (state2.visibleSourceState && routePoints.length > 0) {
          alignPointToCell(routePoints[0], state2, state2.visibleSourceState, true);
        }
        if (state2.visibleTargetState && routePoints.length > 1) {
          alignPointToCell(routePoints[routePoints.length - 1], state2, state2.visibleTargetState, false);
        }
      }
      const scale = state2.view.scale;
      routePoints.forEach((pt) => result2.push(new Point_default(Math.round((pt.x + state2.view.translate.x) * scale * 10) / 10, Math.round((pt.y + state2.view.translate.y) * scale * 10) / 10)));
    }
    router(state, source, target, points, result, config);
  };

  // node_modules/@maxgraph/core/lib/esm/view/style/perimeter/index.js
  var perimeter_exports = {};
  __export(perimeter_exports, {
    EllipsePerimeter: () => EllipsePerimeter,
    HexagonPerimeter: () => HexagonPerimeter,
    RectanglePerimeter: () => RectanglePerimeter,
    RhombusPerimeter: () => RhombusPerimeter,
    TrianglePerimeter: () => TrianglePerimeter
  });

  // node_modules/@maxgraph/core/lib/esm/view/style/perimeter/EllipsePerimeter.js
  var EllipsePerimeter = (bounds, _vertex, next, orthogonal = false) => {
    const { x } = bounds;
    const { y } = bounds;
    const a = bounds.width / 2;
    const b = bounds.height / 2;
    const cx = x + a;
    const cy = y + b;
    const px = next.x;
    const py = next.y;
    const dx = Number.parseInt(String(px - cx));
    const dy = Number.parseInt(String(py - cy));
    if (dx === 0 && dy !== 0) {
      return new Point_default(cx, cy + b * dy / Math.abs(dy));
    }
    if (dx === 0 && dy === 0) {
      return new Point_default(px, py);
    }
    if (orthogonal) {
      if (py >= y && py <= y + bounds.height) {
        const ty = py - cy;
        let tx = Math.sqrt(a * a * (1 - ty * ty / (b * b))) || 0;
        if (px <= x) {
          tx = -tx;
        }
        return new Point_default(cx + tx, py);
      }
      if (px >= x && px <= x + bounds.width) {
        const tx = px - cx;
        let ty = Math.sqrt(b * b * (1 - tx * tx / (a * a))) || 0;
        if (py <= y) {
          ty = -ty;
        }
        return new Point_default(px, cy + ty);
      }
    }
    const d = dy / dx;
    const h = cy - d * cx;
    const e = a * a * d * d + b * b;
    const f = -2 * cx * e;
    const g = a * a * d * d * cx * cx + b * b * cx * cx - a * a * b * b;
    const det = Math.sqrt(f * f - 4 * e * g);
    const xout1 = (-f + det) / (2 * e);
    const xout2 = (-f - det) / (2 * e);
    const yout1 = d * xout1 + h;
    const yout2 = d * xout2 + h;
    const dist1 = Math.sqrt(Math.pow(xout1 - px, 2) + Math.pow(yout1 - py, 2));
    const dist2 = Math.sqrt(Math.pow(xout2 - px, 2) + Math.pow(yout2 - py, 2));
    let xout = 0;
    let yout = 0;
    if (dist1 < dist2) {
      xout = xout1;
      yout = yout1;
    } else {
      xout = xout2;
      yout = yout2;
    }
    return new Point_default(xout, yout);
  };

  // node_modules/@maxgraph/core/lib/esm/view/style/perimeter/HexagonPerimeter.js
  var HexagonPerimeter = (bounds, vertex, next, orthogonal = false) => {
    var _a2, _b;
    const { x } = bounds;
    const { y } = bounds;
    const w = bounds.width;
    const h = bounds.height;
    const cx = bounds.getCenterX();
    const cy = bounds.getCenterY();
    const px = next.x;
    const py = next.y;
    const dx = px - cx;
    const dy = py - cy;
    const alpha = -Math.atan2(dy, dx);
    const pi = Math.PI;
    const pi2 = Math.PI / 2;
    let result = new Point_default(cx, cy);
    const direction = (_b = (_a2 = vertex == null ? void 0 : vertex.style) == null ? void 0 : _a2.direction) != null ? _b : "east";
    const vertical = direction === "north" || direction === "south";
    let a = new Point_default();
    let b = new Point_default();
    if (px < x && py < y || px < x && py > y + h || px > x + w && py < y || px > x + w && py > y + h) {
      orthogonal = false;
    }
    if (orthogonal) {
      if (vertical) {
        if (px === cx) {
          if (py <= y) {
            return new Point_default(cx, y);
          }
          if (py >= y + h) {
            return new Point_default(cx, y + h);
          }
        } else if (px < x) {
          if (py === y + h / 4) {
            return new Point_default(x, y + h / 4);
          }
          if (py === y + 3 * h / 4) {
            return new Point_default(x, y + 3 * h / 4);
          }
        } else if (px > x + w) {
          if (py === y + h / 4) {
            return new Point_default(x + w, y + h / 4);
          }
          if (py === y + 3 * h / 4) {
            return new Point_default(x + w, y + 3 * h / 4);
          }
        } else if (px === x) {
          if (py < cy) {
            return new Point_default(x, y + h / 4);
          }
          if (py > cy) {
            return new Point_default(x, y + 3 * h / 4);
          }
        } else if (px === x + w) {
          if (py < cy) {
            return new Point_default(x + w, y + h / 4);
          }
          if (py > cy) {
            return new Point_default(x + w, y + 3 * h / 4);
          }
        }
        if (py === y) {
          return new Point_default(cx, y);
        }
        if (py === y + h) {
          return new Point_default(cx, y + h);
        }
        if (px < cx) {
          if (py > y + h / 4 && py < y + 3 * h / 4) {
            a = new Point_default(x, y);
            b = new Point_default(x, y + h);
          } else if (py < y + h / 4) {
            a = new Point_default(x - Math.floor(0.5 * w), y + Math.floor(0.5 * h));
            b = new Point_default(x + w, y - Math.floor(0.25 * h));
          } else if (py > y + 3 * h / 4) {
            a = new Point_default(x - Math.floor(0.5 * w), y + Math.floor(0.5 * h));
            b = new Point_default(x + w, y + Math.floor(1.25 * h));
          }
        } else if (px > cx) {
          if (py > y + h / 4 && py < y + 3 * h / 4) {
            a = new Point_default(x + w, y);
            b = new Point_default(x + w, y + h);
          } else if (py < y + h / 4) {
            a = new Point_default(x, y - Math.floor(0.25 * h));
            b = new Point_default(x + Math.floor(1.5 * w), y + Math.floor(0.5 * h));
          } else if (py > y + 3 * h / 4) {
            a = new Point_default(x + Math.floor(1.5 * w), y + Math.floor(0.5 * h));
            b = new Point_default(x, y + Math.floor(1.25 * h));
          }
        }
      } else {
        if (py === cy) {
          if (px <= x) {
            return new Point_default(x, y + h / 2);
          }
          if (px >= x + w) {
            return new Point_default(x + w, y + h / 2);
          }
        } else if (py < y) {
          if (px === x + w / 4) {
            return new Point_default(x + w / 4, y);
          }
          if (px === x + 3 * w / 4) {
            return new Point_default(x + 3 * w / 4, y);
          }
        } else if (py > y + h) {
          if (px === x + w / 4) {
            return new Point_default(x + w / 4, y + h);
          }
          if (px === x + 3 * w / 4) {
            return new Point_default(x + 3 * w / 4, y + h);
          }
        } else if (py === y) {
          if (px < cx) {
            return new Point_default(x + w / 4, y);
          }
          if (px > cx) {
            return new Point_default(x + 3 * w / 4, y);
          }
        } else if (py === y + h) {
          if (px < cx) {
            return new Point_default(x + w / 4, y + h);
          }
          if (py > cy) {
            return new Point_default(x + 3 * w / 4, y + h);
          }
        }
        if (px === x) {
          return new Point_default(x, cy);
        }
        if (px === x + w) {
          return new Point_default(x + w, cy);
        }
        if (py < cy) {
          if (px > x + w / 4 && px < x + 3 * w / 4) {
            a = new Point_default(x, y);
            b = new Point_default(x + w, y);
          } else if (px < x + w / 4) {
            a = new Point_default(x - Math.floor(0.25 * w), y + h);
            b = new Point_default(x + Math.floor(0.5 * w), y - Math.floor(0.5 * h));
          } else if (px > x + 3 * w / 4) {
            a = new Point_default(x + Math.floor(0.5 * w), y - Math.floor(0.5 * h));
            b = new Point_default(x + Math.floor(1.25 * w), y + h);
          }
        } else if (py > cy) {
          if (px > x + w / 4 && px < x + 3 * w / 4) {
            a = new Point_default(x, y + h);
            b = new Point_default(x + w, y + h);
          } else if (px < x + w / 4) {
            a = new Point_default(x - Math.floor(0.25 * w), y);
            b = new Point_default(x + Math.floor(0.5 * w), y + Math.floor(1.5 * h));
          } else if (px > x + 3 * w / 4) {
            a = new Point_default(x + Math.floor(0.5 * w), y + Math.floor(1.5 * h));
            b = new Point_default(x + Math.floor(1.25 * w), y);
          }
        }
      }
      let tx = cx;
      let ty = cy;
      if (px >= x && px <= x + w) {
        tx = px;
        if (py < cy) {
          ty = y + h;
        } else {
          ty = y;
        }
      } else if (py >= y && py <= y + h) {
        ty = py;
        if (px < cx) {
          tx = x + w;
        } else {
          tx = x;
        }
      }
      result = intersection(tx, ty, next.x, next.y, a.x, a.y, b.x, b.y);
    } else {
      if (vertical) {
        const beta = Math.atan2(h / 4, w / 2);
        if (alpha === beta) {
          return new Point_default(x + w, y + Math.floor(0.25 * h));
        }
        if (alpha === pi2) {
          return new Point_default(x + Math.floor(0.5 * w), y);
        }
        if (alpha === pi - beta) {
          return new Point_default(x, y + Math.floor(0.25 * h));
        }
        if (alpha === -beta) {
          return new Point_default(x + w, y + Math.floor(0.75 * h));
        }
        if (alpha === -pi2) {
          return new Point_default(x + Math.floor(0.5 * w), y + h);
        }
        if (alpha === -pi + beta) {
          return new Point_default(x, y + Math.floor(0.75 * h));
        }
        if (alpha < beta && alpha > -beta) {
          a = new Point_default(x + w, y);
          b = new Point_default(x + w, y + h);
        } else if (alpha > beta && alpha < pi2) {
          a = new Point_default(x, y - Math.floor(0.25 * h));
          b = new Point_default(x + Math.floor(1.5 * w), y + Math.floor(0.5 * h));
        } else if (alpha > pi2 && alpha < pi - beta) {
          a = new Point_default(x - Math.floor(0.5 * w), y + Math.floor(0.5 * h));
          b = new Point_default(x + w, y - Math.floor(0.25 * h));
        } else if (alpha > pi - beta && alpha <= pi || alpha < -pi + beta && alpha >= -pi) {
          a = new Point_default(x, y);
          b = new Point_default(x, y + h);
        } else if (alpha < -beta && alpha > -pi2) {
          a = new Point_default(x + Math.floor(1.5 * w), y + Math.floor(0.5 * h));
          b = new Point_default(x, y + Math.floor(1.25 * h));
        } else if (alpha < -pi2 && alpha > -pi + beta) {
          a = new Point_default(x - Math.floor(0.5 * w), y + Math.floor(0.5 * h));
          b = new Point_default(x + w, y + Math.floor(1.25 * h));
        }
      } else {
        const beta = Math.atan2(h / 2, w / 4);
        if (alpha === beta) {
          return new Point_default(x + Math.floor(0.75 * w), y);
        }
        if (alpha === pi - beta) {
          return new Point_default(x + Math.floor(0.25 * w), y);
        }
        if (alpha === pi || alpha === -pi) {
          return new Point_default(x, y + Math.floor(0.5 * h));
        }
        if (alpha === 0) {
          return new Point_default(x + w, y + Math.floor(0.5 * h));
        }
        if (alpha === -beta) {
          return new Point_default(x + Math.floor(0.75 * w), y + h);
        }
        if (alpha === -pi + beta) {
          return new Point_default(x + Math.floor(0.25 * w), y + h);
        }
        if (alpha > 0 && alpha < beta) {
          a = new Point_default(x + Math.floor(0.5 * w), y - Math.floor(0.5 * h));
          b = new Point_default(x + Math.floor(1.25 * w), y + h);
        } else if (alpha > beta && alpha < pi - beta) {
          a = new Point_default(x, y);
          b = new Point_default(x + w, y);
        } else if (alpha > pi - beta && alpha < pi) {
          a = new Point_default(x - Math.floor(0.25 * w), y + h);
          b = new Point_default(x + Math.floor(0.5 * w), y - Math.floor(0.5 * h));
        } else if (alpha < 0 && alpha > -beta) {
          a = new Point_default(x + Math.floor(0.5 * w), y + Math.floor(1.5 * h));
          b = new Point_default(x + Math.floor(1.25 * w), y);
        } else if (alpha < -beta && alpha > -pi + beta) {
          a = new Point_default(x, y + h);
          b = new Point_default(x + w, y + h);
        } else if (alpha < -pi + beta && alpha > -pi) {
          a = new Point_default(x - Math.floor(0.25 * w), y);
          b = new Point_default(x + Math.floor(0.5 * w), y + Math.floor(1.5 * h));
        }
      }
      result = intersection(cx, cy, next.x, next.y, a.x, a.y, b.x, b.y);
    }
    if (result == null) {
      return new Point_default(cx, cy);
    }
    return result;
  };

  // node_modules/@maxgraph/core/lib/esm/view/style/perimeter/RectanglePerimeter.js
  var RectanglePerimeter = (bounds, _vertex, next, orthogonal = false) => {
    const cx = bounds.getCenterX();
    const cy = bounds.getCenterY();
    const dx = next.x - cx;
    const dy = next.y - cy;
    const alpha = Math.atan2(dy, dx);
    const p = new Point_default(0, 0);
    const pi = Math.PI;
    const pi2 = Math.PI / 2;
    const beta = pi2 - alpha;
    const t = Math.atan2(bounds.height, bounds.width);
    if (alpha < -pi + t || alpha > pi - t) {
      p.x = bounds.x;
      p.y = cy - bounds.width * Math.tan(alpha) / 2;
    } else if (alpha < -t) {
      p.y = bounds.y;
      p.x = cx - bounds.height * Math.tan(beta) / 2;
    } else if (alpha < t) {
      p.x = bounds.x + bounds.width;
      p.y = cy + bounds.width * Math.tan(alpha) / 2;
    } else {
      p.y = bounds.y + bounds.height;
      p.x = cx + bounds.height * Math.tan(beta) / 2;
    }
    if (orthogonal) {
      if (next.x >= bounds.x && next.x <= bounds.x + bounds.width) {
        p.x = next.x;
      } else if (next.y >= bounds.y && next.y <= bounds.y + bounds.height) {
        p.y = next.y;
      }
      if (next.x < bounds.x) {
        p.x = bounds.x;
      } else if (next.x > bounds.x + bounds.width) {
        p.x = bounds.x + bounds.width;
      }
      if (next.y < bounds.y) {
        p.y = bounds.y;
      } else if (next.y > bounds.y + bounds.height) {
        p.y = bounds.y + bounds.height;
      }
    }
    return p;
  };

  // node_modules/@maxgraph/core/lib/esm/view/style/perimeter/RhombusPerimeter.js
  var RhombusPerimeter = (bounds, _vertex, next, orthogonal = false) => {
    const { x } = bounds;
    const { y } = bounds;
    const w = bounds.width;
    const h = bounds.height;
    const cx = x + w / 2;
    const cy = y + h / 2;
    const px = next.x;
    const py = next.y;
    if (cx === px) {
      if (cy > py) {
        return new Point_default(cx, y);
      }
      return new Point_default(cx, y + h);
    }
    if (cy === py) {
      if (cx > px) {
        return new Point_default(x, cy);
      }
      return new Point_default(x + w, cy);
    }
    let tx = cx;
    let ty = cy;
    if (orthogonal) {
      if (px >= x && px <= x + w) {
        tx = px;
      } else if (py >= y && py <= y + h) {
        ty = py;
      }
    }
    if (px < cx) {
      if (py < cy) {
        return intersection(px, py, tx, ty, cx, y, x, cy);
      }
      return intersection(px, py, tx, ty, cx, y + h, x, cy);
    }
    if (py < cy) {
      return intersection(px, py, tx, ty, cx, y, x + w, cy);
    }
    return intersection(px, py, tx, ty, cx, y + h, x + w, cy);
  };

  // node_modules/@maxgraph/core/lib/esm/view/style/perimeter/TrianglePerimeter.js
  var TrianglePerimeter = (bounds, vertex, next, orthogonal = false) => {
    const direction = vertex != null ? vertex.style.direction : null;
    const vertical = direction === "north" || direction === "south";
    const { x } = bounds;
    const { y } = bounds;
    const w = bounds.width;
    const h = bounds.height;
    let cx = x + w / 2;
    let cy = y + h / 2;
    let start = new Point_default(x, y);
    let corner = new Point_default(x + w, cy);
    let end = new Point_default(x, y + h);
    switch (direction) {
      case "north": {
        start = end;
        corner = new Point_default(cx, y);
        end = new Point_default(x + w, y + h);
        break;
      }
      case "south": {
        corner = new Point_default(cx, y + h);
        end = new Point_default(x + w, y);
        break;
      }
      case "west": {
        start = new Point_default(x + w, y);
        corner = new Point_default(x, cy);
        end = new Point_default(x + w, y + h);
        break;
      }
    }
    let dx = next.x - cx;
    let dy = next.y - cy;
    const alpha = vertical ? Math.atan2(dx, dy) : Math.atan2(dy, dx);
    const t = vertical ? Math.atan2(w, h) : Math.atan2(h, w);
    let base = false;
    if (direction === "north" || direction === "west") {
      base = alpha > -t && alpha < t;
    } else {
      base = alpha < -Math.PI + t || alpha > Math.PI - t;
    }
    let result = null;
    if (base) {
      if (orthogonal && (vertical && next.x >= start.x && next.x <= end.x || !vertical && next.y >= start.y && next.y <= end.y)) {
        if (vertical) {
          result = new Point_default(next.x, start.y);
        } else {
          result = new Point_default(start.x, next.y);
        }
      } else
        switch (direction) {
          case "north": {
            result = new Point_default(x + w / 2 + h * Math.tan(alpha) / 2, y + h);
            break;
          }
          case "south": {
            result = new Point_default(x + w / 2 - h * Math.tan(alpha) / 2, y);
            break;
          }
          case "west": {
            result = new Point_default(x + w, y + h / 2 + w * Math.tan(alpha) / 2);
            break;
          }
          default: {
            result = new Point_default(x, y + h / 2 - w * Math.tan(alpha) / 2);
          }
        }
    } else {
      if (orthogonal) {
        const pt = new Point_default(cx, cy);
        if (next.y >= y && next.y <= y + h) {
          pt.x = vertical ? cx : direction === "west" ? x + w : x;
          pt.y = next.y;
        } else if (next.x >= x && next.x <= x + w) {
          pt.x = next.x;
          pt.y = !vertical ? cy : direction === "north" ? y + h : y;
        }
        dx = next.x - pt.x;
        dy = next.y - pt.y;
        cx = pt.x;
        cy = pt.y;
      }
      if (vertical && next.x <= x + w / 2 || !vertical && next.y <= y + h / 2) {
        result = intersection(next.x, next.y, cx, cy, start.x, start.y, corner.x, corner.y);
      } else {
        result = intersection(next.x, next.y, cx, cy, corner.x, corner.y, end.x, end.y);
      }
    }
    if (result == null) {
      result = new Point_default(cx, cy);
    }
    return result;
  };

  // node_modules/@maxgraph/core/lib/esm/view/style/marker/edge-markers.js
  var edge_markers_exports = {};
  __export(edge_markers_exports, {
    createArrow: () => createArrow,
    createOpenArrow: () => createOpenArrow,
    diamond: () => diamond,
    oval: () => oval
  });
  var isClassicOrClassicThin = (type) => type === "classic" || type === "classicThin";
  var isDiamond = (type) => type === "diamond";
  var createArrow = (widthFactor) => (canvas, _shape, type, pe, unitX, unitY, size, _source, sw, filled) => {
    const endOffsetX = unitX * sw * 1.118;
    const endOffsetY = unitY * sw * 1.118;
    unitX *= size + sw;
    unitY *= size + sw;
    const pt = pe.clone();
    pt.x -= endOffsetX;
    pt.y -= endOffsetY;
    const f = !isClassicOrClassicThin(type) ? 1 : 3 / 4;
    pe.x += -unitX * f - endOffsetX;
    pe.y += -unitY * f - endOffsetY;
    return () => {
      canvas.begin();
      canvas.moveTo(pt.x, pt.y);
      canvas.lineTo(pt.x - unitX - unitY / widthFactor, pt.y - unitY + unitX / widthFactor);
      if (isClassicOrClassicThin(type)) {
        canvas.lineTo(pt.x - unitX * 3 / 4, pt.y - unitY * 3 / 4);
      }
      canvas.lineTo(pt.x + unitY / widthFactor - unitX, pt.y - unitY - unitX / widthFactor);
      canvas.close();
      if (filled) {
        canvas.fillAndStroke();
      } else {
        canvas.stroke();
      }
    };
  };
  var createOpenArrow = (widthFactor) => (canvas, _shape, _type, pe, unitX, unitY, size, _source, sw, _filled) => {
    const endOffsetX = unitX * sw * 1.118;
    const endOffsetY = unitY * sw * 1.118;
    unitX *= size + sw;
    unitY *= size + sw;
    const pt = pe.clone();
    pt.x -= endOffsetX;
    pt.y -= endOffsetY;
    pe.x += -endOffsetX * 2;
    pe.y += -endOffsetY * 2;
    return () => {
      canvas.begin();
      canvas.moveTo(pt.x - unitX - unitY / widthFactor, pt.y - unitY + unitX / widthFactor);
      canvas.lineTo(pt.x, pt.y);
      canvas.lineTo(pt.x + unitY / widthFactor - unitX, pt.y - unitY - unitX / widthFactor);
      canvas.stroke();
    };
  };
  var oval = (canvas, _shape, _type, pe, unitX, unitY, size, _source, _sw, filled) => {
    const a = size / 2;
    const pt = pe.clone();
    pe.x -= unitX * a;
    pe.y -= unitY * a;
    return () => {
      canvas.ellipse(pt.x - a, pt.y - a, size, size);
      if (filled) {
        canvas.fillAndStroke();
      } else {
        canvas.stroke();
      }
    };
  };
  var diamond = (canvas, _shape, type, pe, unitX, unitY, size, _source, sw, filled) => {
    const swFactor = isDiamond(type) ? 0.7071 : 0.9862;
    const endOffsetX = unitX * sw * swFactor;
    const endOffsetY = unitY * sw * swFactor;
    unitX *= size + sw;
    unitY *= size + sw;
    const pt = pe.clone();
    pt.x -= endOffsetX;
    pt.y -= endOffsetY;
    pe.x += -unitX - endOffsetX;
    pe.y += -unitY - endOffsetY;
    const tk = isDiamond(type) ? 2 : 3.4;
    return () => {
      canvas.begin();
      canvas.moveTo(pt.x, pt.y);
      canvas.lineTo(pt.x - unitX / 2 - unitY / tk, pt.y + unitX / tk - unitY / 2);
      canvas.lineTo(pt.x - unitX, pt.y - unitY);
      canvas.lineTo(pt.x - unitX / 2 + unitY / tk, pt.y - unitY / 2 - unitX / tk);
      canvas.close();
      if (filled) {
        canvas.fillAndStroke();
      } else {
        canvas.stroke();
      }
    };
  };

  // node_modules/@maxgraph/core/lib/esm/internal/BaseRegistry.js
  var BaseRegistry = class {
    constructor() {
      this.values = /* @__PURE__ */ new Map();
    }
    add(name, value) {
      this.values.set(name, value);
    }
    get(name) {
      var _a2;
      return (_a2 = this.values.get(name)) != null ? _a2 : null;
    }
    getName(value) {
      for (const [name, style] of this.values.entries()) {
        if (style === value) {
          return name;
        }
      }
      return null;
    }
    clear() {
      this.values.clear();
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/style/edge/EdgeStyleRegistry.js
  var EdgeStyleRegistryImpl = class extends BaseRegistry {
    constructor() {
      super(...arguments);
      this.handlerMapping = /* @__PURE__ */ new Map();
      this.orthogonalStates = /* @__PURE__ */ new Map();
      this.intermediateHandlesStates = /* @__PURE__ */ new Map();
    }
    add(name, edgeStyle, metaData) {
      super.add(name, edgeStyle);
      (metaData == null ? void 0 : metaData.handlerKind) && this.handlerMapping.set(edgeStyle, metaData.handlerKind);
      !isNullish(metaData == null ? void 0 : metaData.isOrthogonal) && this.orthogonalStates.set(edgeStyle, metaData.isOrthogonal);
      !isNullish(metaData == null ? void 0 : metaData.allowIntermediateHandles) && this.intermediateHandlesStates.set(edgeStyle, metaData.allowIntermediateHandles);
    }
    isOrthogonal(edgeStyle) {
      var _a2;
      return (_a2 = this.orthogonalStates.get(edgeStyle)) != null ? _a2 : false;
    }
    getHandlerKind(edgeStyle) {
      var _a2;
      return (_a2 = this.handlerMapping.get(edgeStyle)) != null ? _a2 : "default";
    }
    allowsIntermediateHandles(edgeStyle) {
      var _a2;
      return (_a2 = this.intermediateHandlesStates.get(edgeStyle)) != null ? _a2 : true;
    }
    clear() {
      super.clear();
      this.handlerMapping.clear();
      this.orthogonalStates.clear();
      this.intermediateHandlesStates.clear();
    }
  };
  var EdgeStyleRegistry = new EdgeStyleRegistryImpl();

  // node_modules/@maxgraph/core/lib/esm/view/cell/CellHighlight.js
  var CellHighlight = class {
    constructor(graph, highlightColor, strokeWidth, dashed) {
      this.strokeWidth = 0;
      this.dashed = false;
      this.opacity = 100;
      this.shape = null;
      this.keepOnTop = false;
      this.state = null;
      this.spacing = 2;
      this.graph = graph;
      this.highlightColor = highlightColor != null ? highlightColor : DEFAULT_VALID_COLOR;
      this.strokeWidth = strokeWidth != null ? strokeWidth : HIGHLIGHT_STROKEWIDTH;
      this.dashed = dashed != null ? dashed : false;
      this.opacity = HIGHLIGHT_OPACITY;
      this.repaintHandler = () => {
        if (this.state) {
          const tmp = this.graph.view.getState(this.state.cell);
          if (!tmp) {
            this.hide();
          } else {
            this.state = tmp;
            this.repaint();
          }
        }
      };
      this.graph.getView().addListener(InternalEvent_default.SCALE, this.repaintHandler);
      this.graph.getView().addListener(InternalEvent_default.TRANSLATE, this.repaintHandler);
      this.graph.getView().addListener(InternalEvent_default.SCALE_AND_TRANSLATE, this.repaintHandler);
      this.graph.getDataModel().addListener(InternalEvent_default.CHANGE, this.repaintHandler);
      this.resetHandler = () => {
        this.hide();
      };
      this.graph.getView().addListener(InternalEvent_default.DOWN, this.resetHandler);
      this.graph.getView().addListener(InternalEvent_default.UP, this.resetHandler);
    }
    /**
     * Sets the color of the rectangle used to highlight drop targets.
     *
     * @param {string} color - String that represents the new highlight color.
     */
    setHighlightColor(color) {
      this.highlightColor = color;
      if (this.shape) {
        this.shape.stroke = color;
      }
    }
    /**
     * Creates and returns the highlight shape for the given state.
     */
    drawHighlight() {
      var _a2;
      this.shape = this.createShape();
      this.repaint();
      if (this.shape) {
        const node = this.shape.node;
        if (!this.keepOnTop && ((_a2 = node == null ? void 0 : node.parentNode) == null ? void 0 : _a2.firstChild) !== node && node.parentNode) {
          node.parentNode.insertBefore(node, node.parentNode.firstChild);
        }
      }
    }
    /**
     * Creates and returns the highlight shape for the given state.
     */
    createShape() {
      if (!this.state)
        return null;
      const shape = this.graph.cellRenderer.createShape(this.state);
      shape.svgStrokeTolerance = this.graph.getEventTolerance();
      shape.points = this.state.absolutePoints;
      shape.apply(this.state);
      shape.stroke = this.highlightColor;
      shape.opacity = this.opacity;
      shape.isDashed = this.dashed;
      shape.isShadow = false;
      shape.dialect = "svg";
      shape.init(this.graph.getView().getOverlayPane());
      InternalEvent_default.redirectMouseEvents(shape.node, this.graph, this.state);
      if (this.graph.dialect !== "svg") {
        shape.pointerEvents = false;
      } else {
        shape.svgPointerEvents = "stroke";
      }
      return shape;
    }
    /**
     * Updates the highlight after a change of the model or view.
     */
    getStrokeWidth(state = null) {
      return this.strokeWidth;
    }
    /**
     * Updates the highlight after a change of the model or view.
     */
    repaint() {
      var _a2;
      if (this.state && this.shape) {
        this.shape.scale = this.state.view.scale;
        if (this.state.cell.isEdge()) {
          this.shape.strokeWidth = this.getStrokeWidth();
          this.shape.points = this.state.absolutePoints;
          this.shape.outline = false;
        } else {
          this.shape.bounds = new Rectangle_default(this.state.x - this.spacing, this.state.y - this.spacing, this.state.width + 2 * this.spacing, this.state.height + 2 * this.spacing);
          this.shape.rotation = (_a2 = this.state.style.rotation) != null ? _a2 : 0;
          this.shape.strokeWidth = this.getStrokeWidth() / this.state.view.scale;
          this.shape.outline = true;
        }
        if (this.state.shape) {
          this.shape.setCursor(this.state.shape.getCursor());
        }
        this.shape.redraw();
      }
    }
    /**
     * Resets the state of the cell marker.
     */
    hide() {
      this.highlight(null);
    }
    /**
     * Marks the {@link CellState} and fires a {@link InternalEvent.MARK} event.
     */
    highlight(state = null) {
      if (this.state !== state) {
        if (this.shape) {
          this.shape.destroy();
          this.shape = null;
        }
        this.state = state;
        if (this.state) {
          this.drawHighlight();
        }
      }
    }
    /**
     * Returns true if this highlight is at the given position.
     */
    isHighlightAt(x, y) {
      let hit = false;
      if (this.shape && document.elementFromPoint) {
        let elt = document.elementFromPoint(x, y);
        while (elt) {
          if (elt === this.shape.node) {
            hit = true;
            break;
          }
          elt = elt.parentNode;
        }
      }
      return hit;
    }
    /**
     * Destroys the handler and all its resources and DOM nodes.
     */
    destroy() {
      const graph = this.graph;
      graph.getView().removeListener(this.resetHandler);
      graph.getView().removeListener(this.repaintHandler);
      graph.getDataModel().removeListener(this.repaintHandler);
      if (this.shape) {
        this.shape.destroy();
        this.shape = null;
      }
    }
  };
  var CellHighlight_default = CellHighlight;

  // node_modules/@maxgraph/core/lib/esm/view/cell/CellMarker.js
  var CellMarker = class extends EventSource_default {
    /**
     * Constructs a new cell marker.
     *
     * @param graph Reference to the enclosing {@link AbstractGraph}.
     * @param validColor Optional marker color for valid states. Default is {@link DEFAULT_VALID_COLOR}.
     * @param invalidColor Optional marker color for invalid states. Default is {@link DEFAULT_INVALID_COLOR}.
     * @param hotspot Portion of the width and height where a state intersects a given coordinate pair. A value of 0 means always highlight. Default is {@link DEFAULT_HOTSPOT}.
     */
    constructor(graph, validColor = DEFAULT_VALID_COLOR, invalidColor = DEFAULT_INVALID_COLOR, hotspot = DEFAULT_HOTSPOT) {
      super();
      this.enabled = true;
      this.hotspot = DEFAULT_HOTSPOT;
      this.hotspotEnabled = false;
      this.currentColor = NONE;
      this.validState = null;
      this.markedState = null;
      this.graph = graph;
      this.validColor = validColor;
      this.invalidColor = invalidColor;
      this.hotspot = hotspot;
      this.highlight = this.createCellHighlight(graph);
    }
    /**
     * Hook method to override the implementation of {@link highlight}.
     * @since 0.22.0
     */
    createCellHighlight(graph) {
      return new CellHighlight_default(graph);
    }
    /**
     * Enables or disables event handling.
     * This implementation updates {@link enabled}.
     *
     * @param enabled Boolean that specifies the new enabled state.
     */
    setEnabled(enabled) {
      this.enabled = enabled;
    }
    /**
     * Returns true if events are handled.
     * This implementation returns {@link enabled}.
     */
    isEnabled() {
      return this.enabled;
    }
    /**
     * Sets the {@link hotspot}.
     */
    setHotspot(hotspot) {
      this.hotspot = hotspot;
    }
    /**
     * Returns the {@link hotspot}.
     */
    getHotspot() {
      return this.hotspot;
    }
    /**
     * Specifies whether the hotspot should be used in <intersects>.
     */
    setHotspotEnabled(enabled) {
      this.hotspotEnabled = enabled;
    }
    /**
     * Returns true if hotspot is used in <intersects>.
     */
    isHotspotEnabled() {
      return this.hotspotEnabled;
    }
    /**
     * Returns true if {@link validState} is not `null`.
     */
    hasValidState() {
      return !!this.validState;
    }
    /**
     * Returns the {@link validState}.
     */
    getValidState() {
      return this.validState;
    }
    /**
     * Returns the {@link markedState}.
     */
    getMarkedState() {
      return this.markedState;
    }
    /**
     * Resets the state of the cell marker.
     */
    reset() {
      this.validState = null;
      if (this.markedState) {
        this.markedState = null;
        this.unmark();
      }
    }
    /**
     * Processes the given event and cell and marks the state returned by {@link getState} with the color returned by {@link getMarkerColor}.
     * If the markerColor is not `null`, then the state is stored in {@link markedState}.
     * If {@link isValidState} returns `true`, then the state is stored in {@link validState} regardless of the marker color.
     * The state is returned regardless of the marker color and valid state.
     */
    process(me) {
      let state = null;
      if (this.isEnabled()) {
        state = this.getState(me);
        this.setCurrentState(state, me);
      }
      return state;
    }
    /**
     * Sets and marks the current valid state.
     */
    setCurrentState(state, me, color) {
      const isValid = state ? this.isValidState(state) : false;
      color = color != null ? color : this.getMarkerColor(me.getEvent(), state, isValid);
      if (isValid) {
        this.validState = state;
      } else {
        this.validState = null;
      }
      if (state !== this.markedState || color !== this.currentColor) {
        this.currentColor = color;
        if (state && this.currentColor !== NONE) {
          this.markedState = state;
          this.mark();
        } else if (this.markedState) {
          this.markedState = null;
          this.unmark();
        }
      }
    }
    /**
     * Marks the given cell using the given color, or {@link validColor} if no color is specified.
     */
    markCell(cell, color) {
      const state = this.graph.getView().getState(cell);
      if (state) {
        this.currentColor = color != null ? color : this.validColor;
        this.markedState = state;
        this.mark();
      }
    }
    /**
     * Marks the {@link markedState} and fires a {@link InternalEvent.MARK} event.
     */
    mark() {
      this.highlight.setHighlightColor(this.currentColor);
      this.highlight.highlight(this.markedState);
      this.fireEvent(new EventObject_default(InternalEvent_default.MARK, "state", this.markedState));
    }
    /**
     * Hides the marker and fires a {@link InternalEvent.MARK} event.
     */
    unmark() {
      this.mark();
    }
    /**
     * Returns true if the given {@link CellState} is a valid state.
     * If this returns `true`, then the state is stored in {@link validState}.
     * The return value of this method is used as the argument for {@link getMarkerColor}.
     */
    isValidState(state) {
      return true;
    }
    /**
     * Returns the {@link validColor} or {@link invalidColor} depending on the value of {@link isValid}.
     * The given {@link CellState} is ignored by this implementation.
     */
    getMarkerColor(evt, state, isValid) {
      return isValid ? this.validColor : this.invalidColor;
    }
    /**
     * Uses {@link getCell}, {@link getStateToMark} and {@link intersects} to return the {@link CellState} for the given {@link MouseEvent}.
     */
    getState(me) {
      const view = this.graph.getView();
      const cell = this.getCell(me);
      if (!cell)
        return null;
      const state = this.getStateToMark(view.getState(cell));
      return state && this.intersects(state, me) ? state : null;
    }
    /**
     * Returns the {@link Cell} for the given event and cell.
     * This implementation returns the given cell.
     */
    getCell(me) {
      return me.getCell();
    }
    /**
     * Returns the {@link CellState} to be marked for the given {@link CellState} under the mouse.
     * This implementation returns the given state.
     */
    getStateToMark(state) {
      return state;
    }
    /**
     * Returns `true` if the given coordinate pair intersects the given state.
     * This returns `true` if the {@link hotspot} is `0` or the coordinates are inside the hotspot for the given cell state.
     */
    intersects(state, me) {
      const x = me.getGraphX();
      const y = me.getGraphY();
      if (this.hotspotEnabled) {
        return intersectsHotspot(state, x, y, this.hotspot, MIN_HOTSPOT_SIZE, MAX_HOTSPOT_SIZE);
      }
      return true;
    }
    /**
     * Destroys the handler and all its resources and DOM nodes.
     */
    destroy() {
      this.highlight.destroy();
      super.destroy();
    }
  };
  var CellMarker_default = CellMarker;

  // node_modules/@maxgraph/core/lib/esm/util/UrlConverter.js
  var UrlConverter = class {
    constructor() {
      this.enabled = true;
      this.baseUrl = null;
      this.baseDomain = null;
    }
    /**
     * Private helper function to update the base URL.
     */
    updateBaseUrl() {
      this.baseDomain = `${location.protocol}//${location.host}`;
      this.baseUrl = this.baseDomain + location.pathname;
      const tmp = this.baseUrl.lastIndexOf("/");
      if (tmp > 0) {
        this.baseUrl = this.baseUrl.substring(0, tmp + 1);
      }
    }
    /**
     * Returns <enabled>.
     */
    isEnabled() {
      return this.enabled;
    }
    /**
     * Sets <enabled>.
     */
    setEnabled(value) {
      this.enabled = value;
    }
    /**
     * Returns <baseUrl>.
     */
    getBaseUrl() {
      return this.baseUrl;
    }
    /**
     * Sets <baseUrl>.
     */
    setBaseUrl(value) {
      this.baseUrl = value;
    }
    /**
     * Returns <baseDomain>.
     */
    getBaseDomain() {
      return this.baseDomain;
    }
    /**
     * Sets <baseDomain>.
     */
    setBaseDomain(value) {
      this.baseDomain = value;
    }
    /**
     * Returns true if the given URL is relative.
     */
    isRelativeUrl(url) {
      return url && url.substring(0, 2) !== "//" && url.substring(0, 7) !== "http://" && url.substring(0, 8) !== "https://" && url.substring(0, 10) !== "data:image" && url.substring(0, 7) !== "file://";
    }
    /**
     * Converts the given URL to an absolute URL with protol and domain.
     * Relative URLs are first converted to absolute URLs.
     */
    convert(url) {
      if (this.isEnabled() && this.isRelativeUrl(url)) {
        if (!this.getBaseUrl()) {
          this.updateBaseUrl();
        }
        if (url.charAt(0) === "/") {
          url = this.getBaseDomain() + url;
        } else {
          url = this.getBaseUrl() + url;
        }
      }
      return url;
    }
  };
  var UrlConverter_default = UrlConverter;

  // node_modules/@maxgraph/core/lib/esm/view/canvas/AbstractCanvas2D.js
  var AbstractCanvas2D = class {
    constructor() {
      this.state = this.createState();
      this.states = [];
      this.path = [];
      this.rotateHtml = true;
      this.lastX = 0;
      this.lastY = 0;
      this.moveOp = "M";
      this.lineOp = "L";
      this.quadOp = "Q";
      this.curveOp = "C";
      this.closeOp = "Z";
      this.pointerEvents = false;
      this.pointerEventsValue = null;
      this.addOp = (op, ...args) => {
        this.path.push(op);
        if (args.length > 1) {
          const s = this.state;
          for (let i = 1; i < args.length; i += 2) {
            this.lastX = args[i - 1];
            this.lastY = args[i];
            this.path.push(this.format((this.lastX + s.dx) * s.scale));
            this.path.push(this.format((this.lastY + s.dy) * s.scale));
          }
        }
      };
      this.converter = this.createUrlConverter();
      this.reset();
    }
    /**
     * Create a new <UrlConverter> and returns it.
     */
    createUrlConverter() {
      return new UrlConverter_default();
    }
    /**
     * Resets the state of this canvas.
     */
    reset() {
      this.state = this.createState();
      this.states = [];
    }
    /**
     * Creates the state of the this canvas.
     */
    createState() {
      return {
        dx: 0,
        dy: 0,
        scale: 1,
        alpha: 1,
        fillAlpha: 1,
        strokeAlpha: 1,
        fillColor: NONE,
        gradientFillAlpha: 1,
        gradientColor: NONE,
        gradientAlpha: 1,
        gradientDirection: "east",
        strokeColor: NONE,
        strokeWidth: 1,
        dashed: false,
        dashPattern: "3 3",
        fixDash: false,
        lineCap: "flat",
        lineJoin: "miter",
        miterLimit: 10,
        fontColor: "#000000",
        fontBackgroundColor: NONE,
        fontBorderColor: NONE,
        fontSize: StyleDefaultsConfig.fontSize,
        fontFamily: StyleDefaultsConfig.fontFamily,
        fontStyle: 0,
        shadow: false,
        shadowColor: StyleDefaultsConfig.shadowColor,
        shadowAlpha: StyleDefaultsConfig.shadowOpacity,
        shadowDx: StyleDefaultsConfig.shadowOffsetX,
        shadowDy: StyleDefaultsConfig.shadowOffsetY,
        rotation: 0,
        rotationCx: 0,
        rotationCy: 0
      };
    }
    /**
     * Rounds all numbers to integers.
     */
    format(value) {
      return Math.round(value);
    }
    /**
     * Rotates the given point and returns the result as an {@link Point}.
     */
    rotatePoint(x, y, theta, cx, cy) {
      const rad = theta * (Math.PI / 180);
      return getRotatedPoint(new Point_default(x, y), Math.cos(rad), Math.sin(rad), new Point_default(cx, cy));
    }
    /**
     * Saves the current state.
     */
    save() {
      this.states.push(this.state);
      this.state = clone(this.state);
    }
    /**
     * Restores the current state.
     */
    restore() {
      const state = this.states.pop();
      if (state)
        this.state = state;
    }
    /**
     * Sets the current link. Hook for subclassers.
     */
    setLink(link) {
    }
    /**
     * Scales the current state.
     */
    scale(value) {
      this.state.scale *= value;
      if (this.state.strokeWidth !== null)
        this.state.strokeWidth *= value;
    }
    /**
     * Translates the current state.
     */
    translate(dx, dy) {
      this.state.dx += dx;
      this.state.dy += dy;
    }
    /**
     * Rotates the current state.
     */
    rotate(theta, flipH, flipV, cx, cy) {
    }
    /**
     * Sets the current alpha.
     */
    setAlpha(value) {
      this.state.alpha = value;
    }
    /**
     * Sets the current solid fill alpha.
     */
    setFillAlpha(value) {
      this.state.fillAlpha = value;
    }
    /**
     * Sets the current stroke alpha.
     */
    setStrokeAlpha(value) {
      this.state.strokeAlpha = value;
    }
    /**
     * Sets the current fill color.
     */
    setFillColor(value) {
      this.state.fillColor = value != null ? value : NONE;
      this.state.gradientColor = NONE;
    }
    /**
     * Sets the current gradient.
     */
    setGradient(color1, color2, x, y, w, h, direction, alpha1 = 1, alpha2 = 1) {
      const s = this.state;
      s.fillColor = color1;
      s.gradientFillAlpha = alpha1;
      s.gradientColor = color2;
      s.gradientAlpha = alpha2;
      s.gradientDirection = direction;
    }
    /**
     * Sets the current stroke color.
     */
    setStrokeColor(value) {
      this.state.strokeColor = value != null ? value : NONE;
    }
    /**
     * Sets the current stroke width.
     */
    setStrokeWidth(value) {
      this.state.strokeWidth = value;
    }
    /**
     * Enables or disables dashed lines.
     */
    setDashed(value, fixDash = false) {
      this.state.dashed = value;
      this.state.fixDash = fixDash;
    }
    /**
     * Sets the current dash pattern.
     */
    setDashPattern(value) {
      this.state.dashPattern = value;
    }
    /**
     * Sets the current line cap.
     */
    setLineCap(value) {
      this.state.lineCap = value;
    }
    /**
     * Sets the current line join.
     */
    setLineJoin(value) {
      this.state.lineJoin = value;
    }
    /**
     * Sets the current miter limit.
     */
    setMiterLimit(value) {
      this.state.miterLimit = value;
    }
    /**
     * Sets the current font color.
     */
    setFontColor(value) {
      this.state.fontColor = value != null ? value : NONE;
    }
    /**
     * Sets the current font background color.
     */
    setFontBackgroundColor(value) {
      this.state.fontBackgroundColor = value != null ? value : NONE;
    }
    /**
     * Sets the current font border color.
     */
    setFontBorderColor(value) {
      this.state.fontBorderColor = value != null ? value : NONE;
    }
    /**
     * Sets the current font size.
     */
    setFontSize(value) {
      this.state.fontSize = value;
    }
    /**
     * Sets the current font family.
     */
    setFontFamily(value) {
      this.state.fontFamily = value;
    }
    /**
     * Sets the current font style.
     */
    setFontStyle(value) {
      this.state.fontStyle = value;
    }
    /**
     * Enables or disables and configures the current shadow.
     */
    setShadow(enabled) {
      this.state.shadow = enabled;
    }
    /**
     * Sets the current shadow color.
     *
     * @param value Hexadecimal representation of the color or `none`.
     */
    setShadowColor(value) {
      this.state.shadowColor = value != null ? value : NONE;
    }
    /**
     * Sets the current shadow alpha.
     *
     * @param value Number that represents the new alpha. Possible values are between 1 (opaque) and 0 (transparent).
     */
    setShadowAlpha(value) {
      this.state.shadowAlpha = value;
    }
    /**
     * Sets the current shadow offset.
     *
     * @param dx Number that represents the horizontal offset of the shadow.
     * @param dy Number that represents the vertical offset of the shadow.
     */
    setShadowOffset(dx, dy) {
      this.state.shadowDx = dx;
      this.state.shadowDy = dy;
    }
    /**
     * Starts a new path.
     */
    begin() {
      this.lastX = 0;
      this.lastY = 0;
      this.path = [];
    }
    /**
     *  Moves the current path the given coordinates.
     */
    moveTo(x, y) {
      this.addOp(this.moveOp, x, y);
    }
    /**
     * Draws a line to the given coordinates. Uses moveTo with the op argument.
     */
    lineTo(x, y) {
      this.addOp(this.lineOp, x, y);
    }
    /**
     * Adds a quadratic curve to the current path.
     */
    quadTo(x1, y1, x2, y2) {
      this.addOp(this.quadOp, x1, y1, x2, y2);
    }
    /**
     * Adds a bezier curve to the current path.
     */
    curveTo(x1, y1, x2, y2, x3, y3) {
      this.addOp(this.curveOp, x1, y1, x2, y2, x3, y3);
    }
    /**
     * Adds the given arc to the current path. This is a synthetic operation that
     * is broken down into curves.
     * @param rx: The x distance between the current position
     *            and the center of the ellipse around which to arc
     * @param ry: The y distance between the current position
     *            and the center of the ellipse around which to arc
     * @param x: The x position of the end point of the arc
     * @param y: The y position of the end point of the arc
     */
    arcTo(rx, ry, angle, largeArcFlag, sweepFlag, x, y) {
      const curves = arcToCurves(this.lastX, this.lastY, rx, ry, angle, largeArcFlag, sweepFlag, x, y);
      if (curves != null) {
        for (let i = 0; i < curves.length; i += 6) {
          this.curveTo(curves[i], curves[i + 1], curves[i + 2], curves[i + 3], curves[i + 4], curves[i + 5]);
        }
      }
    }
    /**
     * Closes the current path.
     */
    close(x1, y1, x2, y2, x3, y3) {
      this.addOp(this.closeOp);
    }
  };
  var AbstractCanvas2D_default = AbstractCanvas2D;

  // node_modules/@maxgraph/core/lib/esm/util/xmlUtils.js
  var parseXml = (xmlString) => {
    return new DOMParser().parseFromString(xmlString, "text/xml");
  };
  var getXml = (node, linefeed = "&#xa;") => {
    const xmlSerializer = new XMLSerializer();
    let xml = xmlSerializer.serializeToString(node);
    xml = xml.replace(/\n/g, linefeed);
    return xml;
  };

  // node_modules/@maxgraph/core/lib/esm/view/canvas/SvgCanvas2D.js
  var useAbsoluteIds = typeof DOMParser === "function" && !Client_default.IS_CHROMEAPP && !Client_default.IS_EDGE && document.getElementsByTagName("base").length > 0;
  var SvgCanvas2D = class _SvgCanvas2D extends AbstractCanvas2D_default {
    constructor(root, styleEnabled) {
      super();
      this.defs = null;
      this.styleEnabled = true;
      this.node = null;
      this.matchHtmlAlignment = true;
      this.textEnabled = true;
      this.foEnabled = true;
      this.foAltText = "[Object]";
      this.foOffset = 0;
      this.textOffset = 0;
      this.imageOffset = 0;
      this.strokeTolerance = 0;
      this.minStrokeWidth = 1;
      this.refCount = 0;
      this.lineHeightCorrection = 1;
      this.pointerEventsValue = "all";
      this.fontMetricsPadding = 10;
      this.cacheOffsetSize = true;
      this.originalRoot = null;
      this.root = root;
      this.gradients = {};
      this.defs = null;
      this.styleEnabled = styleEnabled != null ? styleEnabled : false;
      let svg = null;
      if (root.ownerDocument !== document) {
        let node = root;
        while (node && node.nodeName !== "svg") {
          node = node.parentElement;
        }
        svg = node;
      }
      if (svg) {
        const tmp = svg.getElementsByTagName("defs");
        if (tmp.length > 0) {
          this.defs = svg.getElementsByTagName("defs")[0];
        }
        if (!this.defs) {
          this.defs = this.createElement("defs");
          if (svg.firstChild != null) {
            svg.insertBefore(this.defs, svg.firstChild);
          } else {
            svg.appendChild(this.defs);
          }
        }
        if (this.styleEnabled) {
          this.defs.appendChild(this.createStyle());
        }
      }
    }
    /**
     * Rounds all numbers to 2 decimal points.
     */
    format(value) {
      return Number.parseFloat(value.toFixed(2));
    }
    /**
     * Returns the URL of the page without the hash part. This needs to use href to
     * include any search part with no params (ie question mark alone). This is a
     * workaround for the fact that window.location.search is empty if there is
     * no search string behind the question mark.
     */
    getBaseUrl() {
      let { href } = window.location;
      const hash = href.lastIndexOf("#");
      if (hash > 0) {
        href = href.substring(0, hash);
      }
      return href;
    }
    /**
     * Returns any offsets for rendering pixels.
     */
    reset() {
      super.reset();
      this.gradients = {};
    }
    end() {
      return;
    }
    /**
     * Creates the optional style section.
     */
    createStyle() {
      const style = this.createElement("style");
      style.setAttribute("type", "text/css");
      write(style, `svg{font-family:${StyleDefaultsConfig.fontFamily};font-size:${StyleDefaultsConfig.fontSize};fill:none;stroke-miterlimit:10}`);
      return style;
    }
    /**
     * Private helper function to create SVG elements
     */
    createElement(tagName, namespace) {
      var _a2;
      return (_a2 = this.root) == null ? void 0 : _a2.ownerDocument.createElementNS(namespace || NS_SVG, tagName);
    }
    /**
     * Returns the alternate text string for the given foreignObject.
     */
    getAlternateText(fo, x, y, w, h, str, align, valign, wrap, format, overflow, clip, rotation) {
      return !isNullish(str) ? this.foAltText : null;
    }
    /**
     * Returns the alternate content for the given foreignObject.
     */
    createAlternateContent(fo, x, y, w, h, str, align, valign, wrap, format, overflow, clip, rotation) {
      const text = this.getAlternateText(fo, x, y, w, h, str, align, valign, wrap, format, overflow, clip, rotation);
      const s = this.state;
      if (!isNullish(text) && s.fontSize > 0) {
        const dy = valign === "top" ? 1 : valign === "bottom" ? 0 : 0.3;
        const anchor = align === "right" ? "end" : align === "left" ? "start" : "middle";
        const alt = this.createElement("text");
        alt.setAttribute("x", String(Math.round(x + s.dx)));
        alt.setAttribute("y", String(Math.round(y + s.dy + dy * s.fontSize)));
        alt.setAttribute("fill", s.fontColor || "black");
        alt.setAttribute("font-family", s.fontFamily);
        alt.setAttribute("font-size", `${Math.round(s.fontSize)}px`);
        anchor !== "start" && alt.setAttribute("text-anchor", anchor);
        const fontStyle = s.fontStyle;
        matchBinaryMask(fontStyle, FONT_STYLE_MASK.BOLD) && alt.setAttribute("font-weight", "bold");
        matchBinaryMask(fontStyle, FONT_STYLE_MASK.ITALIC) && alt.setAttribute("font-style", "italic");
        const txtDecor = [];
        matchBinaryMask(fontStyle, FONT_STYLE_MASK.UNDERLINE) && txtDecor.push("underline");
        matchBinaryMask(fontStyle, FONT_STYLE_MASK.STRIKETHROUGH) && txtDecor.push("line-through");
        txtDecor.length > 0 && alt.setAttribute("text-decoration", txtDecor.join(" "));
        write(alt, text);
        return alt;
      }
      return null;
    }
    /**
     * Private helper function to create SVG elements
     */
    createGradientId(start, end, alpha1, alpha2, direction) {
      if (start.charAt(0) === "#") {
        start = start.substring(1);
      }
      if (end.charAt(0) === "#") {
        end = end.substring(1);
      }
      start = `${start.toLowerCase()}-${alpha1}`;
      end = `${end.toLowerCase()}-${alpha2}`;
      let dir = null;
      if (direction == null || direction === "south") {
        dir = "s";
      } else if (direction === "east") {
        dir = "e";
      } else {
        const tmp = start;
        start = end;
        end = tmp;
        if (direction === "north") {
          dir = "s";
        } else if (direction === "west") {
          dir = "e";
        }
      }
      return `mx-gradient-${start}-${end}-${dir}`;
    }
    /**
     * Private helper function to create SVG elements
     */
    getSvgGradient(start, end, alpha1, alpha2, direction) {
      const id = this.createGradientId(start, end, alpha1, alpha2, direction);
      let gradient = this.gradients[id];
      if (!gradient) {
        const svg = this.root.ownerSVGElement;
        let counter = 0;
        let tmpId = `${id}-${counter}`;
        if (svg) {
          gradient = svg.ownerDocument.getElementById(tmpId);
          while (gradient && gradient.ownerSVGElement !== svg) {
            tmpId = `${id}-${counter++}`;
            gradient = svg.ownerDocument.getElementById(tmpId);
          }
        } else {
          tmpId = `id${++this.refCount}`;
        }
        if (!gradient) {
          gradient = this.createSvgGradient(start, end, alpha1, alpha2, direction);
          gradient.setAttribute("id", tmpId);
          if (this.defs) {
            this.defs.appendChild(gradient);
          } else if (svg) {
            svg.appendChild(gradient);
          }
        }
        this.gradients[id] = gradient;
      }
      return gradient.getAttribute("id");
    }
    /**
     * Creates the given SVG gradient.
     */
    createSvgGradient(start, end, alpha1, alpha2, direction) {
      const gradient = this.createElement("linearGradient");
      gradient.setAttribute("x1", "0%");
      gradient.setAttribute("y1", "0%");
      gradient.setAttribute("x2", "0%");
      gradient.setAttribute("y2", "0%");
      if (direction == null || direction === "south") {
        gradient.setAttribute("y2", "100%");
      } else
        switch (direction) {
          case "east": {
            gradient.setAttribute("x2", "100%");
            break;
          }
          case "north": {
            gradient.setAttribute("y1", "100%");
            break;
          }
          case "west": {
            gradient.setAttribute("x1", "100%");
            break;
          }
        }
      let op = alpha1 < 1 ? `;stop-opacity:${alpha1}` : "";
      let stop = this.createElement("stop");
      stop.setAttribute("offset", "0%");
      stop.setAttribute("style", `stop-color:${start}${op}`);
      gradient.appendChild(stop);
      op = alpha2 < 1 ? `;stop-opacity:${alpha2}` : "";
      stop = this.createElement("stop");
      stop.setAttribute("offset", "100%");
      stop.setAttribute("style", `stop-color:${end}${op}`);
      gradient.appendChild(stop);
      return gradient;
    }
    /**
     * Private helper function to create SVG elements
     */
    addNode(filled, stroked) {
      const { node } = this;
      const s = this.state;
      if (node) {
        if (node.nodeName === "path") {
          if (this.path && this.path.length > 0) {
            node.setAttribute("d", this.path.join(" "));
          } else {
            return;
          }
        }
        if (filled && s.fillColor !== NONE) {
          this.updateFill();
        } else if (!this.styleEnabled) {
          if (node.nodeName === "ellipse" && Client_default.IS_FF) {
            node.setAttribute("fill", "transparent");
          } else {
            node.setAttribute("fill", NONE);
          }
          filled = false;
        }
        if (stroked && s.strokeColor !== NONE) {
          this.updateStroke();
        } else if (!this.styleEnabled) {
          node.setAttribute("stroke", NONE);
        }
        if (s.transform && s.transform.length > 0) {
          node.setAttribute("transform", s.transform);
        }
        if (s.shadow) {
          this.root.appendChild(this.createShadow(node));
        }
        if (this.strokeTolerance > 0 && !filled) {
          this.root.appendChild(this.createTolerance(node));
        }
        if (this.pointerEvents) {
          node.setAttribute("pointer-events", this.pointerEventsValue);
        } else if (!this.pointerEvents && !this.originalRoot) {
          node.setAttribute("pointer-events", NONE);
        }
        if (node.nodeName !== "rect" && node.nodeName !== "path" && node.nodeName !== "ellipse" || node.getAttribute("fill") !== NONE && node.getAttribute("fill") !== "transparent" || node.getAttribute("stroke") !== NONE || node.getAttribute("pointer-events") !== NONE) {
          this.root.appendChild(node);
        }
        this.node = null;
      }
    }
    /**
     * Transfers the stroke attributes from <state> to <node>.
     */
    updateFill() {
      var _a2;
      const s = this.state;
      if (s.alpha < 1 || s.fillAlpha < 1) {
        this.node.setAttribute("fill-opacity", String(s.alpha * s.fillAlpha));
      }
      if (s.fillColor !== NONE) {
        if (s.gradientColor !== NONE) {
          const id = this.getSvgGradient(s.fillColor, s.gradientColor, s.gradientFillAlpha, s.gradientAlpha, s.gradientDirection);
          if (((_a2 = this.root) == null ? void 0 : _a2.ownerDocument) === document && useAbsoluteIds) {
            const base = this.getBaseUrl().replace(/([()])/g, "\\$1");
            this.node.setAttribute("fill", `url(${base}#${id})`);
          } else {
            this.node.setAttribute("fill", `url(#${id})`);
          }
        } else {
          this.node.setAttribute("fill", s.fillColor.toLowerCase());
        }
      }
    }
    /**
     * Returns the current stroke width (>= 1), ie. max(1, this.format(this.state.strokeWidth * this.state.scale)).
     */
    getCurrentStrokeWidth() {
      return Math.max(this.minStrokeWidth, Math.max(0.01, this.format(this.state.strokeWidth * this.state.scale)));
    }
    /**
     * Transfers the stroke attributes from {@link AbstractCanvas2D.state} to {@link node}.
     */
    updateStroke() {
      const s = this.state;
      if (s.strokeColor && s.strokeColor !== NONE) {
        this.node.setAttribute("stroke", s.strokeColor.toLowerCase());
      }
      if (s.alpha < 1 || s.strokeAlpha < 1) {
        this.node.setAttribute("stroke-opacity", String(s.alpha * s.strokeAlpha));
      }
      const sw = this.getCurrentStrokeWidth();
      if (sw !== 1) {
        this.node.setAttribute("stroke-width", String(sw));
      }
      if (this.node.nodeName === "path") {
        this.updateStrokeAttributes();
      }
      if (s.dashed) {
        this.node.setAttribute("stroke-dasharray", this.createDashPattern((s.fixDash ? 1 : s.strokeWidth) * s.scale));
      }
    }
    /**
     * Transfers the stroke attributes from {@link AbstractCanvas2D.state} to {@link node}.
     */
    updateStrokeAttributes() {
      const s = this.state;
      if (s.lineJoin && s.lineJoin !== "miter") {
        this.node.setAttribute("stroke-linejoin", s.lineJoin);
      }
      if (s.lineCap) {
        let value = s.lineCap;
        if (value === "flat") {
          value = "butt";
        }
        if (value !== "butt") {
          this.node.setAttribute("stroke-linecap", value);
        }
      }
      if (s.miterLimit != null && (!this.styleEnabled || s.miterLimit !== 10)) {
        this.node.setAttribute("stroke-miterlimit", String(s.miterLimit));
      }
    }
    /**
     * Creates the SVG dash pattern for the given state.
     */
    createDashPattern(scale) {
      const pat = [];
      if (typeof this.state.dashPattern === "string") {
        const dash = this.state.dashPattern.split(" ");
        if (dash.length > 0) {
          for (let i = 0; i < dash.length; i += 1) {
            pat[i] = Number(dash[i]) * scale;
          }
        }
      }
      return pat.join(" ");
    }
    /**
     * Creates a hit detection tolerance shape for the given node.
     */
    createTolerance(node) {
      const tol = node.cloneNode(true);
      const sw = Number.parseFloat(tol.getAttribute("stroke-width") || "1") + this.strokeTolerance;
      tol.setAttribute("pointer-events", "stroke");
      tol.setAttribute("visibility", "hidden");
      tol.removeAttribute("stroke-dasharray");
      tol.setAttribute("stroke-width", String(sw));
      tol.setAttribute("fill", "none");
      tol.setAttribute("stroke", "white");
      return tol;
    }
    /**
     * Creates a shadow for the given node.
     */
    createShadow(node) {
      const shadow = node.cloneNode(true);
      const s = this.state;
      if (shadow.getAttribute("fill") !== "none" && (!Client_default.IS_FF || shadow.getAttribute("fill") !== "transparent")) {
        shadow.setAttribute("fill", s.shadowColor);
      }
      if (shadow.getAttribute("stroke") !== "none" && s.shadowColor && s.shadowColor !== NONE) {
        shadow.setAttribute("stroke", s.shadowColor);
      }
      shadow.setAttribute("transform", `translate(${this.format(s.shadowDx * s.scale)},${this.format(s.shadowDy * s.scale)})${s.transform || ""}`);
      shadow.setAttribute("opacity", String(s.shadowAlpha));
      return shadow;
    }
    /**
     * Experimental implementation for hyperlinks.
     */
    setLink(link) {
      if (!link) {
        this.root = this.originalRoot;
      } else {
        this.originalRoot = this.root;
        const node = this.createElement("a");
        if (node.setAttributeNS == null || this.root.ownerDocument !== document) {
          node.setAttribute("xlink:href", link);
        } else {
          node.setAttributeNS(NS_XLINK, "xlink:href", link);
        }
        this.root.appendChild(node);
        this.root = node;
      }
    }
    /**
     * Sets the rotation of the canvas. Note that rotation cannot be concatenated.
     */
    rotate(theta, flipH, flipV, cx, cy) {
      if (theta !== 0 || flipH || flipV) {
        const s = this.state;
        cx += s.dx;
        cy += s.dy;
        cx *= s.scale;
        cy *= s.scale;
        s.transform = s.transform || "";
        if (flipH && flipV) {
          theta += 180;
        } else if (flipH !== flipV) {
          const tx = flipH ? cx : 0;
          const sx = flipH ? -1 : 1;
          const ty = flipV ? cy : 0;
          const sy = flipV ? -1 : 1;
          s.transform += `translate(${this.format(tx)},${this.format(ty)})scale(${this.format(sx)},${this.format(sy)})translate(${this.format(-tx)},${this.format(-ty)})`;
        }
        if (flipH ? !flipV : flipV) {
          theta *= -1;
        }
        if (theta !== 0) {
          s.transform += `rotate(${this.format(theta)},${this.format(cx)},${this.format(cy)})`;
        }
        s.rotation += theta;
        s.rotationCx = cx;
        s.rotationCy = cy;
      }
    }
    /**
     * Begins a new path.
     */
    begin() {
      super.begin();
      this.node = this.createElement("path");
    }
    /**
     * Private helper function to create SVG elements
     */
    rect(x, y, w, h) {
      const s = this.state;
      const n = this.createElement("rect");
      n.setAttribute("x", String(this.format((x + s.dx) * s.scale)));
      n.setAttribute("y", String(this.format((y + s.dy) * s.scale)));
      n.setAttribute("width", String(this.format(w * s.scale)));
      n.setAttribute("height", String(this.format(h * s.scale)));
      this.node = n;
    }
    /**
     * Private helper function to create SVG elements
     */
    roundrect(x, y, w, h, dx, dy) {
      this.rect(x, y, w, h);
      if (dx > 0) {
        this.node.setAttribute("rx", String(this.format(dx * this.state.scale)));
      }
      if (dy > 0) {
        this.node.setAttribute("ry", String(this.format(dy * this.state.scale)));
      }
    }
    /**
     * Private helper function to create SVG elements
     */
    ellipse(x, y, w, h) {
      const s = this.state;
      const n = this.createElement("ellipse");
      n.setAttribute("cx", String(this.format((x + w / 2 + s.dx) * s.scale)));
      n.setAttribute("cy", String(this.format((y + h / 2 + s.dy) * s.scale)));
      n.setAttribute("rx", String(w / 2 * s.scale));
      n.setAttribute("ry", String(h / 2 * s.scale));
      this.node = n;
    }
    /**
     * Private helper function to create SVG elements
     */
    image(x, y, w, h, src, aspect = true, flipH = false, flipV = false) {
      src = this.converter.convert(src);
      const s = this.state;
      x += s.dx;
      y += s.dy;
      const node = this.createElement("image");
      node.setAttribute("x", String(this.format(x * s.scale) + this.imageOffset));
      node.setAttribute("y", String(this.format(y * s.scale) + this.imageOffset));
      node.setAttribute("width", String(this.format(w * s.scale)));
      node.setAttribute("height", String(this.format(h * s.scale)));
      if (!node.setAttributeNS) {
        node.setAttribute("xlink:href", src);
      } else {
        node.setAttributeNS(NS_XLINK, "xlink:href", src);
      }
      if (!aspect) {
        node.setAttribute("preserveAspectRatio", "none");
      }
      if (s.alpha < 1 || s.fillAlpha < 1) {
        node.setAttribute("opacity", String(s.alpha * s.fillAlpha));
      }
      let tr = this.state.transform || "";
      if (flipH || flipV) {
        let sx = 1;
        let sy = 1;
        let dx = 0;
        let dy = 0;
        if (flipH) {
          sx = -1;
          dx = -w - 2 * x;
        }
        if (flipV) {
          sy = -1;
          dy = -h - 2 * y;
        }
        tr += `scale(${sx},${sy})translate(${dx * s.scale},${dy * s.scale})`;
      }
      if (tr.length > 0) {
        node.setAttribute("transform", tr);
      }
      if (!this.pointerEvents) {
        node.setAttribute("pointer-events", "none");
      }
      this.root.appendChild(node);
    }
    /**
     * Converts the given HTML string to XHTML.
     */
    convertHtml(val) {
      const doc = new DOMParser().parseFromString(val, "text/html");
      if (doc) {
        val = new XMLSerializer().serializeToString(doc.body);
        if (val.substring(0, 5) === "<body") {
          val = val.substring(val.indexOf(">", 5) + 1);
        }
        if (val.substring(val.length - 7, val.length) === "</body>") {
          val = val.substring(0, val.length - 7);
        }
      }
      return val;
    }
    /**
     * Private helper function to create SVG elements
     * Note: signature changed in mxgraph 4.1.0
     */
    createDiv(str) {
      let val = str;
      if (!isNode(val)) {
        val = `<div><div>${this.convertHtml(val)}</div></div>`;
      }
      if (document.createElementNS) {
        const div = document.createElementNS("http://www.w3.org/1999/xhtml", "div");
        if (isNode(val)) {
          const div2 = document.createElement("div");
          const div3 = div2.cloneNode(false);
          if (this.root.ownerDocument !== document) {
            div2.appendChild(val.cloneNode(true));
          } else {
            div2.appendChild(val);
          }
          div3.appendChild(div2);
          div.appendChild(div3);
        } else {
          div.innerHTML = val;
        }
        return div;
      }
      if (isNode(val)) {
        val = `<div><div>${getXml(val)}</div></div>`;
      }
      val = `<div xmlns="http://www.w3.org/1999/xhtml">${val}</div>`;
      return parseXml(val).documentElement;
    }
    /**
     * Updates existing DOM nodes for text rendering. LATER: Merge common parts with text function below.
     */
    updateText(x, y, w, h, align, valign, wrap, overflow, clip, rotation, node) {
      if (node && node.firstChild && node.firstChild.firstChild) {
        this.updateTextNodes(x, y, w, h, align, valign, wrap, overflow, clip, rotation, node.firstChild);
      }
    }
    /**
     * Creates a foreignObject for the given string and adds it to the given root.
     */
    addForeignObject(x, y, w, h, str, align, valign, wrap, format, overflow, clip, rotation, dir, div, root) {
      var _a2;
      const group = this.createElement("g");
      const fo = this.createElement("foreignObject");
      fo.setAttribute("style", "overflow: visible; text-align: left;");
      fo.setAttribute("pointer-events", "none");
      fo.appendChild(div);
      group.appendChild(fo);
      this.updateTextNodes(x, y, w, h, align, valign, wrap, overflow, clip, rotation, group);
      if (((_a2 = this.root) == null ? void 0 : _a2.ownerDocument) !== document) {
        const alt = this.createAlternateContent(fo, x, y, w, h, str, align, valign, wrap, format, overflow, clip, rotation);
        if (alt != null) {
          fo.setAttribute("requiredFeatures", "http://www.w3.org/TR/SVG11/feature#Extensibility");
          const sw = this.createElement("switch");
          sw.appendChild(fo);
          sw.appendChild(alt);
          group.appendChild(sw);
        }
      }
      root.appendChild(group);
    }
    /**
     * Updates existing DOM nodes for text rendering.
     */
    updateTextNodes(x, y, w, h, align, valign, wrap, overflow, clip, rotation, g) {
      const s = this.state.scale;
      _SvgCanvas2D.createCss(w + 2, h, align, valign, wrap, overflow, clip, this.state.fontBackgroundColor != null ? this.state.fontBackgroundColor : null, this.state.fontBorderColor != null ? this.state.fontBorderColor : null, `display: flex; align-items: unsafe ${valign === "top" ? "flex-start" : valign === "bottom" ? "flex-end" : "center"}; justify-content: unsafe ${align === "left" ? "flex-start" : align === "right" ? "flex-end" : "center"}; `, this.getTextCss(), s, (dx, dy, flex, item, block) => {
        x += this.state.dx;
        y += this.state.dy;
        const fo = g.firstChild;
        const div = fo.firstChild;
        const box = div.firstChild;
        const text = box.firstChild;
        const r = (this.rotateHtml ? this.state.rotation : 0) + (rotation != null ? rotation : 0);
        let t = (this.foOffset !== 0 ? `translate(${this.foOffset} ${this.foOffset})` : "") + (s !== 1 ? `scale(${s})` : "");
        text.setAttribute("style", block);
        box.setAttribute("style", item);
        fo.setAttribute("width", `${Math.ceil(1 / Math.min(1, s) * 100)}%`);
        fo.setAttribute("height", `${Math.ceil(1 / Math.min(1, s) * 100)}%`);
        const yp = Math.round(y + dy);
        if (yp < 0) {
          fo.setAttribute("y", String(yp));
        } else {
          fo.removeAttribute("y");
          flex += `padding-top: ${yp}px; `;
        }
        div.setAttribute("style", `${flex}margin-left: ${Math.round(x + dx)}px;`);
        t += r !== 0 ? `rotate(${r} ${x} ${y})` : "";
        if (t !== "") {
          g.setAttribute("transform", t);
        } else {
          g.removeAttribute("transform");
        }
        if (this.state.alpha !== 1) {
          g.setAttribute("opacity", String(this.state.alpha));
        } else {
          g.removeAttribute("opacity");
        }
      });
    }
    /**
     * Private helper function to create SVG elements
     */
    getTextCss() {
      const s = this.state;
      const lh = ABSOLUTE_LINE_HEIGHT ? `${s.fontSize * LINE_HEIGHT}px` : LINE_HEIGHT * this.lineHeightCorrection;
      let css = `display: inline-block; font-size: ${s.fontSize}px; font-family: ${s.fontFamily}; color: ${s.fontColor}; line-height: ${lh}; pointer-events: ${this.pointerEvents ? this.pointerEventsValue : "none"}; `;
      const fontStyle = s.fontStyle;
      matchBinaryMask(fontStyle, FONT_STYLE_MASK.BOLD) && (css += "font-weight: bold; ");
      matchBinaryMask(fontStyle, FONT_STYLE_MASK.ITALIC) && (css += "font-style: italic; ");
      const txtDecor = [];
      matchBinaryMask(fontStyle, FONT_STYLE_MASK.UNDERLINE) && txtDecor.push("underline");
      matchBinaryMask(fontStyle, FONT_STYLE_MASK.STRIKETHROUGH) && txtDecor.push("line-through");
      txtDecor.length > 0 && (css += `text-decoration: ${txtDecor.join(" ")}; `);
      return css;
    }
    /**
     * Paints the given text.
     *
     * Possible values for format are empty string for plain text and HTML for HTML markup.
     *
     * Note that HTML markup is only supported if `foreignObject` is supported and {@link foEnabled} is `true`.
     */
    text(x, y, w, h, str, align, valign, wrap, format, overflow, clip, rotation = 0, dir) {
      if (this.textEnabled && str != null) {
        rotation = rotation != null ? rotation : 0;
        if (this.foEnabled && format === "html") {
          const div = this.createDiv(str);
          if (div != null) {
            if (dir != null) {
              div.setAttribute("dir", dir);
            }
            this.addForeignObject(x, y, w, h, str, align, valign, wrap, format, overflow, clip, rotation, dir, div, this.root);
          }
        } else {
          this.plainText(x + this.state.dx, y + this.state.dy, w, h, str, align, valign, wrap, overflow, clip, rotation, dir);
        }
      }
    }
    /**
     * Creates a clip for the given coordinates.
     */
    createClip(x, y, w, h) {
      x = Math.round(x);
      y = Math.round(y);
      w = Math.round(w);
      h = Math.round(h);
      const id = `mx-clip-${x}-${y}-${w}-${h}`;
      let counter = 0;
      let tmp = `${id}-${counter}`;
      while (document.getElementById(tmp) != null) {
        tmp = `${id}-${++counter}`;
      }
      const clip = this.createElement("clipPath");
      clip.setAttribute("id", tmp);
      const rect = this.createElement("rect");
      rect.setAttribute("x", String(x));
      rect.setAttribute("y", String(y));
      rect.setAttribute("width", String(w));
      rect.setAttribute("height", String(h));
      clip.appendChild(rect);
      return clip;
    }
    /**
     * Paints the given text. Possible values for format are empty string for
     * plain text and html for HTML markup.
     */
    plainText(x, y, w, h, str, align, valign, wrap, overflow, clip, rotation = 0, dir) {
      const s = this.state;
      const size = s.fontSize;
      const node = this.createElement("g");
      let tr = s.transform || "";
      this.updateFont(node);
      if (!this.pointerEvents && this.originalRoot == null) {
        node.setAttribute("pointer-events", "none");
      }
      if (rotation !== 0) {
        tr += `rotate(${rotation},${this.format(x * s.scale)},${this.format(y * s.scale)})`;
      }
      if (dir != null) {
        node.setAttribute("direction", dir);
      }
      if (clip && w > 0 && h > 0) {
        let cx = x;
        let cy2 = y;
        if (align === "center") {
          cx -= w / 2;
        } else if (align === "right") {
          cx -= w;
        }
        if (overflow !== "fill") {
          if (valign === "middle") {
            cy2 -= h / 2;
          } else if (valign === "bottom") {
            cy2 -= h;
          }
        }
        const c = this.createClip(cx * s.scale - 2, cy2 * s.scale - 2, w * s.scale + 4, h * s.scale + 4);
        if (this.defs != null) {
          this.defs.appendChild(c);
        } else {
          this.root.appendChild(c);
        }
        if (!Client_default.IS_CHROMEAPP && !Client_default.IS_EDGE && this.root.ownerDocument === document) {
          const base = this.getBaseUrl().replace(/([()])/g, "\\$1");
          node.setAttribute("clip-path", `url(${base}#${c.getAttribute("id")})`);
        } else {
          node.setAttribute("clip-path", `url(#${c.getAttribute("id")})`);
        }
      }
      const anchor = align === "right" ? "end" : align === "center" ? "middle" : "start";
      if (anchor !== "start") {
        node.setAttribute("text-anchor", anchor);
      }
      if (!this.styleEnabled || size !== StyleDefaultsConfig.fontSize) {
        node.setAttribute("font-size", `${size * s.scale}px`);
      }
      if (tr.length > 0) {
        node.setAttribute("transform", tr);
      }
      if (s.alpha < 1) {
        node.setAttribute("opacity", String(s.alpha));
      }
      const lines = str.split("\n");
      const lh = Math.round(size * LINE_HEIGHT);
      const textHeight = size + (lines.length - 1) * lh;
      let cy = y + size - 1;
      if (valign === "middle") {
        if (overflow === "fill") {
          cy -= h / 2;
        } else {
          const dy = (this.matchHtmlAlignment && clip && h > 0 ? Math.min(textHeight, h) : textHeight) / 2;
          cy -= dy;
        }
      } else if (valign === "bottom") {
        if (overflow === "fill") {
          cy -= h;
        } else {
          const dy = this.matchHtmlAlignment && clip && h > 0 ? Math.min(textHeight, h) : textHeight;
          cy -= dy + 1;
        }
      }
      for (let i = 0; i < lines.length; i += 1) {
        const line = trim(lines[i]);
        if (line) {
          const text = this.createElement("text");
          text.setAttribute("x", String(this.format(x * s.scale) + this.textOffset));
          text.setAttribute("y", String(this.format(cy * s.scale) + this.textOffset));
          write(text, line);
          node.appendChild(text);
        }
        cy += lh;
      }
      this.root.appendChild(node);
      this.addTextBackground(node, str, x, y, w, overflow === "fill" ? h : textHeight, align, valign, overflow);
    }
    /**
     * Updates the text properties for the given node. (NOTE: For this to work in
     * IE, the given node must be a text or tspan element.)
     */
    updateFont(node) {
      const s = this.state;
      if (s.fontColor && s.fontColor !== NONE) {
        node.setAttribute("fill", s.fontColor);
      }
      if (!this.styleEnabled || s.fontFamily !== StyleDefaultsConfig.fontFamily) {
        node.setAttribute("font-family", s.fontFamily);
      }
      const fontStyle = s.fontStyle;
      matchBinaryMask(fontStyle, FONT_STYLE_MASK.BOLD) && node.setAttribute("font-weight", "bold");
      matchBinaryMask(fontStyle, FONT_STYLE_MASK.ITALIC) && node.setAttribute("font-style", "italic");
      const txtDecor = [];
      matchBinaryMask(fontStyle, FONT_STYLE_MASK.UNDERLINE) && txtDecor.push("underline");
      matchBinaryMask(fontStyle, FONT_STYLE_MASK.STRIKETHROUGH) && txtDecor.push("line-through");
      txtDecor.length > 0 && node.setAttribute("text-decoration", txtDecor.join(" "));
    }
    /**
     * Background color and border
     */
    addTextBackground(node, str, x, y, w, h, align, valign, overflow) {
      var _a2;
      const s = this.state;
      if (s.fontBackgroundColor != null || s.fontBorderColor != null) {
        let bbox = null;
        if (overflow === "fill" || overflow === "width") {
          if (align === "center") {
            x -= w / 2;
          } else if (align === "right") {
            x -= w;
          }
          if (valign === "middle") {
            y -= h / 2;
          } else if (valign === "bottom") {
            y -= h;
          }
          bbox = new Rectangle_default((x + 1) * s.scale, y * s.scale, (w - 2) * s.scale, (h + 2) * s.scale);
        } else if (node.getBBox != null && this.root.ownerDocument === document) {
          try {
            bbox = node.getBBox();
            bbox = new Rectangle_default(bbox.x, bbox.y + 1, bbox.width, bbox.height + 0);
          } catch (e) {
          }
        }
        if (bbox == null || bbox.width === 0 || bbox.height === 0) {
          const div = document.createElement("div");
          div.style.lineHeight = ABSOLUTE_LINE_HEIGHT ? `${s.fontSize * LINE_HEIGHT}px` : String(LINE_HEIGHT);
          div.style.fontSize = `${s.fontSize}px`;
          div.style.fontFamily = s.fontFamily;
          div.style.whiteSpace = "nowrap";
          div.style.position = "absolute";
          div.style.visibility = "hidden";
          div.style.display = "inline-block";
          matchBinaryMask(s.fontStyle, FONT_STYLE_MASK.BOLD) && (div.style.fontWeight = "bold");
          matchBinaryMask(s.fontStyle, FONT_STYLE_MASK.ITALIC) && (div.style.fontStyle = "italic");
          str = htmlEntities(str, false);
          div.innerHTML = str.replace(/\n/g, "<br/>");
          document.body.appendChild(div);
          const w2 = div.offsetWidth;
          const h2 = div.offsetHeight;
          document.body.removeChild(div);
          if (align === "center") {
            x -= w2 / 2;
          } else if (align === "right") {
            x -= w2;
          }
          if (valign === "middle") {
            y -= h2 / 2;
          } else if (valign === "bottom") {
            y -= h2;
          }
          bbox = new Rectangle_default((x + 1) * s.scale, (y + 2) * s.scale, w2 * s.scale, (h2 + 1) * s.scale);
        }
        if (bbox != null) {
          const n = this.createElement("rect");
          n.setAttribute("fill", s.fontBackgroundColor || "none");
          n.setAttribute("stroke", s.fontBorderColor || "none");
          n.setAttribute("x", String(Math.floor(bbox.x - 1)));
          n.setAttribute("y", String(Math.floor(bbox.y - 1)));
          n.setAttribute("width", String(Math.ceil(bbox.width + 2)));
          n.setAttribute("height", String(Math.ceil(bbox.height)));
          const sw = s.fontBorderColor ? Math.max(1, this.format(s.scale)) : 0;
          n.setAttribute("stroke-width", String(sw));
          if (((_a2 = this.root) == null ? void 0 : _a2.ownerDocument) === document && mod(sw, 2) === 1) {
            n.setAttribute("transform", "translate(0.5, 0.5)");
          }
          node.insertBefore(n, node.firstChild);
        }
      }
    }
    /**
     * Paints the outline of the current path.
     */
    stroke() {
      this.addNode(false, true);
    }
    /**
     * Fills the current path.
     */
    fill() {
      this.addNode(true, false);
    }
    /**
     * Fills and paints the outline of the current path.
     */
    fillAndStroke() {
      this.addNode(true, true);
    }
  };
  SvgCanvas2D.createCss = (w, h, align, valign, wrap, overflow, clip, bg, border, flex, block, scale, callback) => {
    let item = `box-sizing: border-box; font-size: 0; text-align: ${align === "left" ? "left" : align === "right" ? "right" : "center"}; `;
    const pt = getAlignmentAsPoint(align, valign);
    let ofl = "overflow: hidden; ";
    let fw = "width: 1px; ";
    let fh = "height: 1px; ";
    let dx = pt.x * w;
    let dy = pt.y * h;
    if (clip) {
      fw = `width: ${Math.round(w)}px; `;
      item += `max-height: ${Math.round(h)}px; `;
      dy = 0;
    } else if (overflow === "fill") {
      fw = `width: ${Math.round(w)}px; `;
      fh = `height: ${Math.round(h)}px; `;
      block += "width: 100%; height: 100%; ";
      item += fw + fh;
    } else if (overflow === "width") {
      fw = `width: ${Math.round(w)}px; `;
      block += "width: 100%; ";
      item += fw;
      dy = 0;
      if (h > 0) {
        item += `max-height: ${Math.round(h)}px; `;
      }
    } else {
      ofl = "";
      dy = 0;
    }
    let bgc = "";
    if (bg) {
      bgc += `background-color: ${bg}; `;
    }
    if (border) {
      bgc += `border: 1px solid ${border}; `;
    }
    if (ofl == "" || clip) {
      block += bgc;
    } else {
      item += bgc;
    }
    if (wrap && w > 0) {
      block += `white-space: normal; word-wrap: ${WORD_WRAP}; `;
      fw = `width: ${Math.round(w)}px; `;
      if (ofl !== "" && overflow !== "fill") {
        dy = 0;
      }
    } else {
      block += "white-space: nowrap; ";
      if (ofl === "") {
        dx = 0;
      }
    }
    callback(dx, dy, flex + fw + fh, item + ofl, block, ofl);
  };
  var SvgCanvas2D_default = SvgCanvas2D;

  // node_modules/@maxgraph/core/lib/esm/view/shape/Shape.js
  var Shape = class {
    constructor(stencil = null) {
      this.preserveImageAspect = false;
      this.overlay = null;
      this.indicator = null;
      this.indicatorShape = null;
      this.opacity = 100;
      this.isDashed = false;
      this.fill = NONE;
      this.gradient = NONE;
      this.gradientDirection = "east";
      this.fillOpacity = 100;
      this.strokeOpacity = 100;
      this.stroke = NONE;
      this.strokeWidth = 1;
      this.spacing = 0;
      this.startSize = 1;
      this.endSize = 1;
      this.startArrow = NONE;
      this.endArrow = NONE;
      this.direction = "east";
      this.flipH = false;
      this.flipV = false;
      this.isShadow = false;
      this.isRounded = false;
      this.rotation = 0;
      this.cursor = "";
      this.verticalTextRotation = 0;
      this.oldGradients = {};
      this.glass = false;
      this.dialect = null;
      this.scale = 1;
      this.antiAlias = true;
      this.minSvgStrokeWidth = 1;
      this.bounds = null;
      this.points = [];
      this.state = null;
      this.style = null;
      this.boundingBox = null;
      this.stencil = null;
      this.svgStrokeTolerance = 8;
      this.pointerEvents = true;
      this.originalPointerEvents = null;
      this.svgPointerEvents = "all";
      this.shapePointerEvents = false;
      this.stencilPointerEvents = false;
      this.outline = false;
      this.visible = true;
      this.useSvgBoundingBox = true;
      this.image = null;
      this.imageSrc = null;
      this.indicatorColor = NONE;
      this.indicatorStrokeColor = NONE;
      this.indicatorGradientColor = NONE;
      this.indicatorDirection = "east";
      this.indicatorImageSrc = null;
      if (stencil) {
        this.stencil = stencil;
      }
      this.node = this.create();
    }
    /**
     * Initializes the shape by adding it into the given container if the node of the shape doesn't already have a parent.
     *
     * @param container DOM node that will contain the shape.
     */
    init(container) {
      if (!this.node.parentNode) {
        container.appendChild(this.node);
      }
    }
    /**
     * Sets the styles to their default values.
     */
    initStyles() {
      this.strokeWidth = 1;
      this.rotation = 0;
      this.opacity = 100;
      this.fillOpacity = 100;
      this.strokeOpacity = 100;
      this.flipH = false;
      this.flipV = false;
    }
    /**
     * Returns true if HTML is allowed for this shape. This implementation always returns `false`.
     */
    isHtmlAllowed() {
      return false;
    }
    /**
     * Returns 0, or 0.5 if {@link strokeWidth} % 2 == 1.
     */
    getSvgScreenOffset() {
      var _a2;
      const sw = this.stencil && this.stencil.strokeWidthValue !== "inherit" ? Number(this.stencil.strokeWidthValue) : (_a2 = this.strokeWidth) != null ? _a2 : 0;
      return mod(Math.max(1, Math.round(sw * this.scale)), 2) === 1 ? 0.5 : 0;
    }
    /**
     * Creates and returns the DOM node for the shape.
     * This implementation assumes that `maxGraph` produces SVG elements.
     */
    create() {
      return document.createElementNS(NS_SVG, "g");
    }
    redraw() {
      this.updateBoundsFromPoints();
      if (this.visible && this.checkBounds()) {
        this.node.style.visibility = "visible";
        this.clear();
        this.redrawShape();
        this.updateBoundingBox();
      } else {
        this.node.style.visibility = "hidden";
        this.boundingBox = null;
      }
    }
    /**
     * Removes all child nodes and resets all CSS.
     */
    clear() {
      while (this.node.lastChild) {
        this.node.removeChild(this.node.lastChild);
      }
    }
    /**
     * Updates the bounds based on the points.
     */
    updateBoundsFromPoints() {
      const pts = this.points;
      if (pts.length > 0 && pts[0]) {
        this.bounds = new Rectangle_default(Math.round(pts[0].x), Math.round(pts[0].y), 1, 1);
        for (const pt of pts) {
          if (pt) {
            this.bounds.add(new Rectangle_default(Math.round(pt.x), Math.round(pt.y), 1, 1));
          }
        }
      }
    }
    /**
     * Returns the {@link Rectangle} for the label bounds of this shape, based on the
     * given scaled and translated bounds of the shape. This method should not
     * change the rectangle in-place. This implementation returns the given rect.
     */
    getLabelBounds(rect) {
      var _a2, _b, _c, _d, _e, _f;
      const d = (_b = (_a2 = this.style) == null ? void 0 : _a2.direction) != null ? _b : "east";
      let bounds = rect.clone();
      if (d !== "south" && d !== "north" && this.state && this.state.text && this.state.text.isPaintBoundsInverted()) {
        bounds = bounds.clone();
        [bounds.width, bounds.height] = [bounds.height, bounds.width];
      }
      let labelMargins = this.getLabelMargins(bounds);
      if (labelMargins) {
        labelMargins = labelMargins.clone();
        let flipH = (_d = (_c = this.style) == null ? void 0 : _c.flipH) != null ? _d : false;
        let flipV = (_f = (_e = this.style) == null ? void 0 : _e.flipV) != null ? _f : false;
        if (this.state && this.state.text && this.state.text.isPaintBoundsInverted()) {
          const tmp = labelMargins.x;
          labelMargins.x = labelMargins.height;
          labelMargins.height = labelMargins.width;
          labelMargins.width = labelMargins.y;
          labelMargins.y = tmp;
          [flipH, flipV] = [flipV, flipH];
        }
        return getDirectedBounds(rect, labelMargins, this.style, flipH, flipV);
      }
      return rect;
    }
    /**
     * Returns the scaled top, left, bottom and right margin to be used for
     * computing the label bounds as an {@link Rectangle}, where the bottom and right
     * margin are defined in the width and height of the rectangle, respectively.
     */
    getLabelMargins(rect) {
      return null;
    }
    /**
     * Returns true if the bounds are not null and all of its variables are numeric.
     */
    checkBounds() {
      return !Number.isNaN(this.scale) && Number.isFinite(this.scale) && this.scale > 0 && this.bounds && !Number.isNaN(this.bounds.x) && !Number.isNaN(this.bounds.y) && !Number.isNaN(this.bounds.width) && !Number.isNaN(this.bounds.height) && this.bounds.width > 0 && this.bounds.height > 0;
    }
    /**
     * Updates the SVG or VML shape.
     */
    redrawShape() {
      const canvas = this.createCanvas();
      if (canvas) {
        canvas.pointerEvents = this.pointerEvents;
        this.beforePaint(canvas);
        this.paint(canvas);
        this.afterPaint(canvas);
        if (this.node !== canvas.root && canvas.root) {
          this.node.insertAdjacentHTML("beforeend", canvas.root.outerHTML);
        }
        this.destroyCanvas(canvas);
      }
    }
    /**
     * Creates a new canvas for drawing this shape. May return null.
     */
    createCanvas() {
      const canvas = this.createSvgCanvas();
      if (canvas && this.outline) {
        canvas.setStrokeWidth(this.strokeWidth);
        canvas.setStrokeColor(this.stroke);
        if (this.isDashed) {
          canvas.setDashed(this.isDashed);
        }
        canvas.setStrokeWidth = () => {
          return;
        };
        canvas.setStrokeColor = () => {
          return;
        };
        canvas.setFillColor = () => {
          return;
        };
        canvas.setGradient = () => {
          return;
        };
        canvas.setDashed = () => {
          return;
        };
        canvas.text = () => {
          return;
        };
      }
      return canvas;
    }
    /**
     * Creates and returns an {@link SvgCanvas2D} for rendering this shape.
     */
    createSvgCanvas() {
      if (!this.node)
        return null;
      const canvas = new SvgCanvas2D_default(this.node, false);
      canvas.strokeTolerance = this.pointerEvents ? this.svgStrokeTolerance : 0;
      canvas.pointerEventsValue = this.svgPointerEvents;
      const off = this.getSvgScreenOffset();
      if (off !== 0) {
        this.node.setAttribute("transform", `translate(${off},${off})`);
      } else {
        this.node.removeAttribute("transform");
      }
      canvas.minStrokeWidth = this.minSvgStrokeWidth;
      if (!this.antiAlias) {
        canvas.format = (value) => {
          return Math.round(value);
        };
      }
      return canvas;
    }
    /**
     * Destroys the given canvas which was used for drawing. This implementation
     * increments the reference counts on all shared gradients used in the canvas.
     */
    destroyCanvas(canvas) {
      if (canvas instanceof SvgCanvas2D_default) {
        for (const key in canvas.gradients) {
          const gradient = canvas.gradients[key];
          if (gradient) {
            gradient.mxRefCount = (gradient.mxRefCount || 0) + 1;
          }
        }
        this.releaseSvgGradients(this.oldGradients);
        this.oldGradients = canvas.gradients;
      }
    }
    /**
     * Invoked before paint is called.
     */
    beforePaint(c) {
      return;
    }
    /**
     * Invokes after paint was called.
     */
    afterPaint(c) {
      return;
    }
    /**
     * Generic rendering code.
     */
    paint(c) {
      let strokeDrawn = false;
      if (c && this.outline) {
        const { stroke } = c;
        c.stroke = (...args) => {
          strokeDrawn = true;
          stroke.apply(c, args);
        };
        const { fillAndStroke } = c;
        c.fillAndStroke = (...args) => {
          strokeDrawn = true;
          fillAndStroke.apply(c, args);
        };
      }
      const s = this.scale;
      const bounds = this.bounds;
      if (bounds) {
        let x = bounds.x / s;
        let y = bounds.y / s;
        let w = bounds.width / s;
        let h = bounds.height / s;
        if (this.isPaintBoundsInverted()) {
          const t = (w - h) / 2;
          x += t;
          y -= t;
          const tmp = w;
          w = h;
          h = tmp;
        }
        this.updateTransform(c, x, y, w, h);
        this.configureCanvas(c, x, y, w, h);
        let bg = null;
        if (!this.stencil && this.points.length === 0 && this.shapePointerEvents || this.stencil && this.stencilPointerEvents) {
          const bb = this.createBoundingBox();
          if (bb && this.node) {
            bg = this.createTransparentSvgRectangle(bb.x, bb.y, bb.width, bb.height);
            this.node.appendChild(bg);
          }
        }
        if (this.stencil) {
          this.stencil.drawShape(c, this, x, y, w, h);
        } else {
          c.setStrokeWidth(this.strokeWidth);
          if (this.points.length > 0) {
            const pts = [];
            for (let i = 0; i < this.points.length; i += 1) {
              const p = this.points[i];
              if (p) {
                pts.push(new Point_default(p.x / s, p.y / s));
              }
            }
            this.paintEdgeShape(c, pts);
          } else {
            this.paintVertexShape(c, x, y, w, h);
          }
        }
        if (bg && c.state && !isNullish(c.state.transform)) {
          bg.setAttribute("transform", c.state.transform);
        }
        if (c && this.outline && !strokeDrawn) {
          c.rect(x, y, w, h);
          c.stroke();
        }
      }
    }
    /**
     * Sets the state of the canvas for drawing the shape.
     */
    configureCanvas(c, x, y, w, h) {
      var _a2, _b;
      let dash = null;
      if (this.style && this.style.dashPattern != null) {
        dash = this.style.dashPattern;
      }
      c.setAlpha(this.opacity / 100);
      c.setFillAlpha(this.fillOpacity / 100);
      c.setStrokeAlpha(this.strokeOpacity / 100);
      if (this.isShadow) {
        c.setShadow(this.isShadow);
      }
      if (this.isDashed) {
        c.setDashed(this.isDashed, (_b = (_a2 = this.style) == null ? void 0 : _a2.fixDash) != null ? _b : false);
      }
      if (dash) {
        c.setDashPattern(dash);
      }
      if (this.fill !== NONE && this.gradient !== NONE) {
        const b = this.getGradientBounds(c, x, y, w, h);
        c.setGradient(this.fill, this.gradient, b.x, b.y, b.width, b.height, this.gradientDirection);
      } else {
        c.setFillColor(this.fill);
      }
      c.setStrokeColor(this.stroke);
    }
    /**
     * Returns the bounding box for the gradient box for this shape.
     */
    getGradientBounds(c, x, y, w, h) {
      return new Rectangle_default(x, y, w, h);
    }
    /**
     * Sets the scale and rotation on the given canvas.
     */
    updateTransform(c, x, y, w, h) {
      c.scale(this.scale);
      c.rotate(this.getShapeRotation(), this.flipH, this.flipV, x + w / 2, y + h / 2);
    }
    /**
     * Paints the vertex shape.
     */
    paintVertexShape(c, x, y, w, h) {
      var _a2;
      this.paintBackground(c, x, y, w, h);
      if (!this.outline || !this.style || !((_a2 = this.style.backgroundOutline) != null ? _a2 : false)) {
        c.setShadow(false);
        this.paintForeground(c, x, y, w, h);
      }
    }
    /**
     * Hook for subclassers. This implementation is empty.
     */
    paintBackground(c, x, y, w, h) {
      return;
    }
    /**
     * Hook for subclassers. This implementation is empty.
     */
    paintForeground(c, x, y, w, h) {
      return;
    }
    /**
     * Hook for subclassers. This implementation is empty.
     */
    paintEdgeShape(c, pts) {
      return;
    }
    /**
     * Base arc size for the shape, taken from the style.
     * @since 0.21.0
     */
    getBaseArcSize() {
      var _a2, _b;
      return ((_b = (_a2 = this.style) == null ? void 0 : _a2.arcSize) != null ? _b : StyleDefaultsConfig.lineArcSize) / 2;
    }
    /**
     * Returns the arc size for the given dimension.
     */
    getArcSize(w, h) {
      var _a2, _b, _c;
      if ((_a2 = this.style) == null ? void 0 : _a2.absoluteArcSize) {
        return Math.min(this.getBaseArcSize(), Math.min(h, w) / 2);
      }
      const roundingFactor = ((_c = (_b = this.style) == null ? void 0 : _b.arcSize) != null ? _c : StyleDefaultsConfig.roundingFactor * 100) / 100;
      return Math.min(w, h) * roundingFactor;
    }
    /**
     * Paints the glass gradient effect.
     */
    paintGlassEffect(c, x, y, w, h, arc) {
      var _a2;
      const sw = Math.ceil(((_a2 = this.strokeWidth) != null ? _a2 : 0) / 2);
      const size = 0.4;
      c.setGradient("#ffffff", "#ffffff", x, y, w, h * 0.6, "south", 0.9, 0.1);
      c.begin();
      arc += 2 * sw;
      if (this.isRounded) {
        c.moveTo(x - sw + arc, y - sw);
        c.quadTo(x - sw, y - sw, x - sw, y - sw + arc);
        c.lineTo(x - sw, y + h * size);
        c.quadTo(x + w * 0.5, y + h * 0.7, x + w + sw, y + h * size);
        c.lineTo(x + w + sw, y - sw + arc);
        c.quadTo(x + w + sw, y - sw, x + w + sw - arc, y - sw);
      } else {
        c.moveTo(x - sw, y - sw);
        c.lineTo(x - sw, y + h * size);
        c.quadTo(x + w * 0.5, y + h * 0.7, x + w + sw, y + h * size);
        c.lineTo(x + w + sw, y - sw);
      }
      c.close();
      c.fill();
    }
    /**
     * Paints the given points with rounded corners.
     */
    addPoints(c, pts, rounded = false, arcSize, close = false, exclude = [], initialMove = true) {
      if (pts.length > 0) {
        const pe = pts[pts.length - 1];
        if (close && rounded) {
          pts = pts.slice();
          const p0 = pts[0];
          const wp = new Point_default(pe.x + (p0.x - pe.x) / 2, pe.y + (p0.y - pe.y) / 2);
          pts.splice(0, 0, wp);
        }
        let pt = pts[0];
        let i = 1;
        if (initialMove) {
          c.moveTo(pt.x, pt.y);
        } else {
          c.lineTo(pt.x, pt.y);
        }
        while (i < (close ? pts.length : pts.length - 1)) {
          let tmp = pts[mod(i, pts.length)];
          let dx = pt.x - tmp.x;
          let dy = pt.y - tmp.y;
          if (rounded && (dx !== 0 || dy !== 0) && !exclude.includes(i - 1)) {
            let dist = Math.sqrt(dx * dx + dy * dy);
            const nx1 = dx * Math.min(arcSize, dist / 2) / dist;
            const ny1 = dy * Math.min(arcSize, dist / 2) / dist;
            const x1 = tmp.x + nx1;
            const y1 = tmp.y + ny1;
            c.lineTo(x1, y1);
            let next = pts[mod(i + 1, pts.length)];
            while (i < pts.length - 2 && Math.round(next.x - tmp.x) === 0 && Math.round(next.y - tmp.y) === 0) {
              next = pts[mod(i + 2, pts.length)];
              i++;
            }
            dx = next.x - tmp.x;
            dy = next.y - tmp.y;
            dist = Math.max(1, Math.sqrt(dx * dx + dy * dy));
            const nx2 = dx * Math.min(arcSize, dist / 2) / dist;
            const ny2 = dy * Math.min(arcSize, dist / 2) / dist;
            const x2 = tmp.x + nx2;
            const y2 = tmp.y + ny2;
            c.quadTo(tmp.x, tmp.y, x2, y2);
            tmp = new Point_default(x2, y2);
          } else {
            c.lineTo(tmp.x, tmp.y);
          }
          pt = tmp;
          i += 1;
        }
        if (close) {
          c.close();
        } else {
          c.lineTo(pe.x, pe.y);
        }
      }
    }
    /**
     * Resets all styles.
     */
    resetStyles() {
      this.initStyles();
      this.spacing = 0;
      this.fill = NONE;
      this.gradient = NONE;
      this.gradientDirection = "east";
      this.stroke = NONE;
      this.startSize = 1;
      this.endSize = 1;
      this.startArrow = NONE;
      this.endArrow = NONE;
      this.direction = "east";
      this.isShadow = false;
      this.isDashed = false;
      this.isRounded = false;
      this.glass = false;
    }
    /**
     * Applies the style of the given <CellState> to the shape. This
     * implementation assigns the following styles to local fields:
     *
     * - <'fillColor'> => fill
     * - <'gradientColor'> => gradient
     * - <'gradientDirection'> => gradientDirection
     * - <'opacity'> => opacity
     * - {@link Constants#STYLE_FILL_OPACITY} => fillOpacity
     * - {@link Constants#STYLE_STROKE_OPACITY} => strokeOpacity
     * - <'strokeColor'> => stroke
     * - <'strokeWidth'> => strokewidth
     * - <'shadow'> => isShadow
     * - <'dashed'> => isDashed
     * - <'spacing'> => spacing
     * - <'startSize'> => startSize
     * - <'endSize'> => endSize
     * - <'rounded'> => isRounded
     * - <'startArrow'> => startArrow
     * - <'endArrow'> => endArrow
     * - <'rotation'> => rotation
     * - <'direction'> => direction
     * - <'glass'> => glass
     *
     * This keeps a reference to the <style>. If you need to keep a reference to
     * the cell, you can override this method and store a local reference to
     * state.cell or the <CellState> itself. If <outline> should be true, make
     * sure to set it before calling this method.
     *
     * @param state <CellState> of the corresponding cell.
     */
    apply(state) {
      var _a2, _b, _c, _d, _e, _f, _g, _h, _i, _j, _k, _l, _m, _n, _o, _p, _q, _r, _s;
      this.state = state;
      this.style = state.style;
      if (this.style) {
        this.fill = (_a2 = this.style.fillColor) != null ? _a2 : this.fill;
        this.gradient = (_b = this.style.gradientColor) != null ? _b : this.gradient;
        this.gradientDirection = (_c = this.style.gradientDirection) != null ? _c : this.gradientDirection;
        this.opacity = (_d = this.style.opacity) != null ? _d : this.opacity;
        this.fillOpacity = (_e = this.style.fillOpacity) != null ? _e : this.fillOpacity;
        this.strokeOpacity = (_f = this.style.strokeOpacity) != null ? _f : this.strokeOpacity;
        this.stroke = (_g = this.style.strokeColor) != null ? _g : this.stroke;
        this.strokeWidth = (_h = this.style.strokeWidth) != null ? _h : this.strokeWidth;
        this.spacing = (_i = this.style.spacing) != null ? _i : this.spacing;
        this.startSize = (_j = this.style.startSize) != null ? _j : this.startSize;
        this.endSize = (_k = this.style.endSize) != null ? _k : this.endSize;
        this.startArrow = (_l = this.style.startArrow) != null ? _l : this.startArrow;
        this.endArrow = (_m = this.style.endArrow) != null ? _m : this.endArrow;
        this.rotation = (_n = this.style.rotation) != null ? _n : this.rotation;
        this.direction = (_o = this.style.direction) != null ? _o : this.direction;
        this.flipH = !!this.style.flipH;
        this.flipV = !!this.style.flipV;
        if (this.direction === "north" || this.direction === "south") {
          const tmp = this.flipH;
          this.flipH = this.flipV;
          this.flipV = tmp;
        }
        this.isShadow = (_p = this.style.shadow) != null ? _p : this.isShadow;
        this.isDashed = (_q = this.style.dashed) != null ? _q : this.isDashed;
        this.isRounded = (_r = this.style.rounded) != null ? _r : this.isRounded;
        this.glass = (_s = this.style.glass) != null ? _s : this.glass;
      }
    }
    /**
     * Sets the cursor on the given shape.
     *
     * @param cursor The cursor to be used.
     */
    setCursor(cursor) {
      this.cursor = cursor;
      this.node.style.cursor = cursor;
    }
    /**
     * Returns the current cursor.
     */
    getCursor() {
      return this.cursor;
    }
    /**
     * Hook for subclassers. This implementation returns `false`.
     */
    isRoundable(c, x, y, w, h) {
      return false;
    }
    /**
     * Updates the {@link boundingBox} for this shape using {@link createBoundingBox} and
     * {@link augmentBoundingBox} and stores the result in {@link boundingBox}.
     */
    updateBoundingBox() {
      var _a2;
      if (this.useSvgBoundingBox && this.node.ownerSVGElement) {
        try {
          const b = this.node.getBBox();
          if (b.width > 0 && b.height > 0) {
            this.boundingBox = new Rectangle_default(b.x, b.y, b.width, b.height);
            this.boundingBox.grow(((_a2 = this.strokeWidth) != null ? _a2 : 0) * this.scale / 2);
            return;
          }
        } catch (e) {
        }
      }
      if (this.bounds) {
        let bbox = this.createBoundingBox();
        if (bbox) {
          this.augmentBoundingBox(bbox);
          const rot = this.getShapeRotation();
          if (rot !== 0) {
            bbox = getBoundingBox(bbox, rot);
          }
        }
        this.boundingBox = bbox;
      }
    }
    /**
     * Returns a new rectangle that represents the bounding box of the bare shape
     * with no shadows or strokewidths.
     */
    createBoundingBox() {
      if (!this.bounds)
        return null;
      const bb = this.bounds.clone();
      if (this.stencil && (this.direction === "north" || this.direction === "south") || this.isPaintBoundsInverted()) {
        bb.rotate90();
      }
      return bb;
    }
    /**
     * Augments the bounding box with the strokewidth and shadow offsets.
     */
    augmentBoundingBox(bbox) {
      var _a2;
      if (this.isShadow) {
        bbox.width += Math.ceil(StyleDefaultsConfig.shadowOffsetX * this.scale);
        bbox.height += Math.ceil(StyleDefaultsConfig.shadowOffsetX * this.scale);
      }
      bbox.grow(((_a2 = this.strokeWidth) != null ? _a2 : 0) * this.scale / 2);
    }
    /**
     * Returns true if the bounds should be inverted.
     */
    isPaintBoundsInverted() {
      return !this.stencil && (this.direction === "north" || this.direction === "south");
    }
    /**
     * Returns the rotation from the style.
     */
    getRotation() {
      var _a2;
      return (_a2 = this.rotation) != null ? _a2 : 0;
    }
    /**
     * Returns the rotation for the text label.
     */
    getTextRotation() {
      var _a2, _b;
      let rot = this.getRotation();
      if (!((_b = (_a2 = this.style) == null ? void 0 : _a2.horizontal) != null ? _b : true)) {
        rot += this.verticalTextRotation || -90;
      }
      return rot;
    }
    /**
     * Returns the actual rotation of the shape.
     */
    getShapeRotation() {
      let rot = this.getRotation();
      switch (this.direction) {
        case "north": {
          rot += 270;
          break;
        }
        case "west": {
          rot += 180;
          break;
        }
        case "south": {
          rot += 90;
          break;
        }
      }
      return rot;
    }
    /**
     * Adds a transparent rectangle that catches all events.
     */
    createTransparentSvgRectangle(x, y, w, h) {
      const rect = document.createElementNS(NS_SVG, "rect");
      rect.setAttribute("x", String(x));
      rect.setAttribute("y", String(y));
      rect.setAttribute("width", String(w));
      rect.setAttribute("height", String(h));
      rect.setAttribute("fill", NONE);
      rect.setAttribute("stroke", NONE);
      rect.setAttribute("pointer-events", "all");
      return rect;
    }
    redrawHtmlShape() {
      return;
    }
    /**
     * Sets a transparent background CSS style to catch all events.
     *
     * Paints the line shape.
     */
    setTransparentBackgroundImage(node) {
      node.style.backgroundImage = `url('${Client_default.imageBasePath}/transparent.gif')`;
    }
    /**
     * Paints the line shape.
     */
    releaseSvgGradients(grads) {
      for (const key in grads) {
        const gradient = grads[key];
        if (gradient) {
          gradient.mxRefCount = (gradient.mxRefCount || 0) - 1;
          if (gradient.mxRefCount === 0 && gradient.parentNode) {
            gradient.parentNode.removeChild(gradient);
          }
        }
      }
    }
    /**
     * Destroys the shape by removing it from the DOM and releasing the DOM
     * node associated with the shape using {@link Event#release}.
     */
    destroy() {
      InternalEvent_default.release(this.node);
      if (this.node.parentNode) {
        this.node.parentNode.removeChild(this.node);
      }
      this.node.innerHTML = "";
      this.releaseSvgGradients(this.oldGradients);
      this.oldGradients = {};
    }
  };
  var Shape_default = Shape;

  // node_modules/@maxgraph/core/lib/esm/view/shape/node/EllipseShape.js
  var EllipseShape = class extends Shape_default {
    constructor(bounds, fill, stroke, strokeWidth = 1) {
      super();
      this.bounds = bounds;
      this.fill = fill;
      this.stroke = stroke;
      this.strokeWidth = strokeWidth;
    }
    /**
     * Paints the ellipse shape.
     */
    paintVertexShape(c, x, y, w, h) {
      c.ellipse(x, y, w, h);
      c.fillAndStroke();
    }
  };
  var EllipseShape_default = EllipseShape;

  // node_modules/@maxgraph/core/lib/esm/view/shape/node/RectangleShape.js
  var RectangleShape = class extends Shape_default {
    constructor(bounds, fill, stroke, strokeWidth = 1) {
      super();
      this.bounds = bounds;
      this.fill = fill;
      this.stroke = stroke;
      this.strokeWidth = strokeWidth;
    }
    /**
     * Returns true for non-rounded, non-rotated shapes with no glass gradient.
     */
    isHtmlAllowed() {
      let events = true;
      if (this.style && this.style.pointerEvents != null) {
        events = this.style.pointerEvents;
      }
      return !this.isRounded && !this.glass && this.rotation === 0 && (events || this.fill !== NONE);
    }
    /**
     * Generic background painting implementation.
     */
    paintBackground(c, x, y, w, h) {
      let events = true;
      if (this.style && this.style.pointerEvents != null) {
        events = this.style.pointerEvents;
      }
      if (events || this.fill !== NONE || this.stroke !== NONE) {
        if (!events && this.fill === NONE) {
          c.pointerEvents = false;
        }
        if (this.isRounded) {
          const r = this.getArcSize(w, h);
          c.roundrect(x, y, w, h, r, r);
        } else {
          c.rect(x, y, w, h);
        }
        c.fillAndStroke();
      }
    }
    /**
     * Adds roundable support.
     */
    isRoundable(c, x, y, w, h) {
      return true;
    }
    /**
     * Generic background painting implementation.
     */
    paintForeground(c, x, y, w, h) {
      if (this.glass && !this.outline && this.fill !== NONE) {
        this.paintGlassEffect(c, x, y, w, h, this.getArcSize(w + this.strokeWidth, h + this.strokeWidth));
      }
    }
  };
  var RectangleShape_default = RectangleShape;

  // node_modules/@maxgraph/core/lib/esm/view/shape/node/ImageShape.js
  var ImageShape = class extends RectangleShape_default {
    constructor(bounds, imageSrc, fill = "#FFFFFF", stroke = "#000000", strokeWidth = 1) {
      super(bounds, fill, stroke, strokeWidth);
      this.imageSrc = imageSrc;
      this.shadow = false;
      this.preserveImageAspect = true;
    }
    /**
     * Disables offset in IE9 for crisper image output.
     */
    getSvgScreenOffset() {
      return 0;
    }
    /**
     * Overrides to replace the fill and stroke colors with the respective values from {@link imageBackground} and {@link imageBorder}.
     *
     * Applies the style of the given {@link CellState} to the shape. This implementation assigns the following styles to local fields:
     *
     * - {@link imageBackground} => fill
     * - {@link imageBorder} => stroke
     *
     * @param {CellState} state   {@link CellState} of the corresponding cell.
     */
    apply(state) {
      super.apply(state);
      this.fill = NONE;
      this.stroke = NONE;
      this.gradient = NONE;
      if (this.style && this.style.imageAspect != null) {
        this.preserveImageAspect = this.style.imageAspect;
      }
    }
    isHtmlAllowed() {
      return !this.preserveImageAspect;
    }
    /**
     * Disables inherited roundable support.
     */
    isRoundable(c, x, y, w, h) {
      return false;
    }
    /**
     * Generic background painting implementation.
     */
    paintVertexShape(c, x, y, w, h) {
      var _a2, _b, _c, _d;
      if (this.imageSrc) {
        const fill = (_b = (_a2 = this.style) == null ? void 0 : _a2.imageBackground) != null ? _b : NONE;
        const stroke = (_d = (_c = this.style) == null ? void 0 : _c.imageBorder) != null ? _d : NONE;
        if (fill !== NONE) {
          c.setFillColor(fill);
          c.setStrokeColor(stroke);
          c.rect(x, y, w, h);
          c.fillAndStroke();
        }
        c.image(x, y, w, h, this.imageSrc, this.preserveImageAspect, false, false);
        if (stroke !== NONE) {
          c.setShadow(false);
          c.setStrokeColor(stroke);
          c.rect(x, y, w, h);
          c.stroke();
        }
      } else {
        this.paintBackground(c, x, y, w, h);
      }
    }
  };
  var ImageShape_default = ImageShape;

  // node_modules/@maxgraph/core/lib/esm/view/other/ConnectionConstraint.js
  var ConnectionConstraint = class {
    constructor(point, perimeter = true, name = null, dx = 0, dy = 0) {
      this.perimeter = true;
      this.name = null;
      this.dx = 0;
      this.dy = 0;
      this.point = point;
      this.perimeter = perimeter;
      this.name = name;
      this.dx = dx;
      this.dy = dy;
    }
  };
  var ConnectionConstraint_default = ConnectionConstraint;

  // node_modules/@maxgraph/core/lib/esm/view/handler/ConstraintHandler.js
  var ConstraintHandler = class {
    constructor(graph) {
      this.pointImage = new ImageBox_default(`${Client_default.imageBasePath}/point.gif`, 5, 5);
      this.currentFocus = null;
      this.currentFocusArea = null;
      this.focusIcons = [];
      this.constraints = null;
      this.currentConstraint = null;
      this.focusHighlight = null;
      this.focusPoints = [];
      this.currentPoint = null;
      this.enabled = true;
      this.highlightColor = DEFAULT_VALID_COLOR;
      this.mouseleaveHandler = null;
      this.graph = graph;
      this.resetHandler = () => {
        if (this.currentFocus && !this.graph.view.getState(this.currentFocus.cell)) {
          this.reset();
        } else {
          this.redraw();
        }
      };
      this.graph.model.addListener(InternalEvent_default.CHANGE, this.resetHandler);
      this.graph.view.addListener(InternalEvent_default.SCALE_AND_TRANSLATE, this.resetHandler);
      this.graph.view.addListener(InternalEvent_default.TRANSLATE, this.resetHandler);
      this.graph.view.addListener(InternalEvent_default.SCALE, this.resetHandler);
      this.graph.addListener(InternalEvent_default.ROOT, this.resetHandler);
    }
    /**
     * Returns true if events are handled. This implementation
     * returns {@link enabled}.
     */
    isEnabled() {
      return this.enabled;
    }
    /**
     * Enables or disables event handling. This implementation
     * updates {@link enabled}.
     *
     * @param {boolean} enabled - Boolean that specifies the new enabled state.
     */
    setEnabled(enabled) {
      this.enabled = enabled;
    }
    /**
     * Resets the state of this handler.
     */
    reset() {
      for (let i = 0; i < this.focusIcons.length; i += 1) {
        this.focusIcons[i].destroy();
      }
      this.focusIcons = [];
      if (this.focusHighlight) {
        this.focusHighlight.destroy();
        this.focusHighlight = null;
      }
      this.currentConstraint = null;
      this.currentFocusArea = null;
      this.currentPoint = null;
      this.currentFocus = null;
      this.focusPoints = [];
    }
    /**
     * Returns the tolerance to be used for intersecting connection points. This
     * implementation returns {@link mxGraph.tolerance}.
     *
     * @param me {@link mxMouseEvent} whose tolerance should be returned.
     */
    getTolerance(me) {
      return this.graph.getEventTolerance();
    }
    /**
     * Returns the tolerance to be used for intersecting connection points.
     */
    getImageForConstraint(state, constraint, point) {
      return this.pointImage;
    }
    /**
     * Returns true if the given {@link mxMouseEvent} should be ignored in {@link update}. This
     * implementation always returns false.
     */
    isEventIgnored(me, source = false) {
      return false;
    }
    /**
     * Returns true if the given state should be ignored. This always returns false.
     */
    isStateIgnored(state, source = false) {
      return false;
    }
    /**
     * Destroys the {@link focusIcons} if they exist.
     */
    destroyIcons() {
      for (let i = 0; i < this.focusIcons.length; i += 1) {
        this.focusIcons[i].destroy();
      }
      this.focusIcons = [];
      this.focusPoints = [];
    }
    /**
     * Destroys the {@link focusHighlight} if one exists.
     */
    destroyFocusHighlight() {
      if (this.focusHighlight) {
        this.focusHighlight.destroy();
        this.focusHighlight = null;
      }
    }
    /**
     * Returns true if the current focused state should not be changed for the given event.
     * This returns true if shift and alt are pressed.
     */
    isKeepFocusEvent(me) {
      return isShiftDown(me.getEvent());
    }
    /**
     * Returns the cell for the given event.
     */
    getCellForEvent(me, point) {
      let cell = me.getCell();
      if (!cell && point && (me.getGraphX() !== point.x || me.getGraphY() !== point.y)) {
        cell = this.graph.getCellAt(point.x, point.y);
      }
      if (cell && !cell.isConnectable()) {
        const parent = cell.getParent();
        if (parent && parent.isVertex() && parent.isConnectable()) {
          cell = parent;
        }
      }
      if (cell) {
        return this.graph.isCellLocked(cell) ? null : cell;
      } else {
        return null;
      }
    }
    /**
     * Updates the state of this handler based on the given {@link mxMouseEvent}.
     * Source is a boolean indicating if the cell is a source or target.
     */
    update(me, source, existingEdge, point) {
      if (this.isEnabled() && !this.isEventIgnored(me)) {
        if (!this.mouseleaveHandler && this.graph.container) {
          this.mouseleaveHandler = () => {
            this.reset();
          };
          InternalEvent_default.addListener(this.graph.container, "mouseleave", this.resetHandler);
        }
        const tol = this.getTolerance(me);
        const x = point ? point.x : me.getGraphX();
        const y = point ? point.y : me.getGraphY();
        const grid = new Rectangle_default(x - tol, y - tol, 2 * tol, 2 * tol);
        const mouse = new Rectangle_default(me.getGraphX() - tol, me.getGraphY() - tol, 2 * tol, 2 * tol);
        const state = this.graph.view.getState(this.getCellForEvent(me, point));
        if (!this.isKeepFocusEvent(me) && (!this.currentFocusArea || !this.currentFocus || state || !this.currentFocus.cell.isVertex() || !intersects(this.currentFocusArea, mouse)) && state !== this.currentFocus) {
          this.currentFocusArea = null;
          this.currentFocus = null;
          this.setFocus(me, state, source);
        }
        this.currentConstraint = null;
        this.currentPoint = null;
        let minDistSq = null;
        let tmp;
        if (this.focusIcons.length > 0 && this.constraints && (!state || this.currentFocus === state)) {
          const cx = mouse.getCenterX();
          const cy = mouse.getCenterY();
          for (let i = 0; i < this.focusIcons.length; i += 1) {
            const dx = cx - this.focusIcons[i].bounds.getCenterX();
            const dy = cy - this.focusIcons[i].bounds.getCenterY();
            tmp = dx * dx + dy * dy;
            if ((this.intersects(this.focusIcons[i], mouse, source, existingEdge) || point && this.intersects(this.focusIcons[i], grid, source, existingEdge)) && (minDistSq === null || tmp < minDistSq)) {
              this.currentConstraint = this.constraints[i];
              this.currentPoint = this.focusPoints[i];
              minDistSq = tmp;
              tmp = this.focusIcons[i].bounds.clone();
              tmp.grow(HIGHLIGHT_SIZE + 1);
              tmp.width -= 1;
              tmp.height -= 1;
              if (!this.focusHighlight) {
                const hl = this.createHighlightShape();
                hl.dialect = "svg";
                hl.pointerEvents = false;
                hl.init(this.graph.getView().getOverlayPane());
                this.focusHighlight = hl;
                const getState = () => {
                  return this.currentFocus ? this.currentFocus : state;
                };
                InternalEvent_default.redirectMouseEvents(hl.node, this.graph, getState);
              }
              this.focusHighlight.bounds = tmp;
              this.focusHighlight.redraw();
            }
          }
        }
        if (!this.currentConstraint) {
          this.destroyFocusHighlight();
        }
      } else {
        this.currentConstraint = null;
        this.currentFocus = null;
        this.currentPoint = null;
      }
    }
    /**
     * Transfers the focus to the given state as a source or target terminal. If
     * the handler is not enabled then the outline is painted, but the constraints
     * are ignored.
     */
    redraw() {
      if (this.currentFocus && this.constraints && this.focusIcons.length > 0) {
        const state = this.graph.view.getState(this.currentFocus.cell);
        this.currentFocus = state;
        this.currentFocusArea = new Rectangle_default(state.x, state.y, state.width, state.height);
        for (let i = 0; i < this.constraints.length; i += 1) {
          const cp = this.graph.getConnectionPoint(state, this.constraints[i]);
          const img = this.getImageForConstraint(state, this.constraints[i], cp);
          const bounds = new Rectangle_default(Math.round(cp.x - img.width / 2), Math.round(cp.y - img.height / 2), img.width, img.height);
          this.focusIcons[i].bounds = bounds;
          this.focusIcons[i].redraw();
          this.currentFocusArea.add(this.focusIcons[i].bounds);
          this.focusPoints[i] = cp;
        }
      }
    }
    /**
     * Transfers the focus to the given state as a source or target terminal. If
     * the handler is not enabled then the outline is painted, but the constraints
     * are ignored.
     */
    setFocus(me, state, source) {
      var _a2, _b;
      this.constraints = state && !this.isStateIgnored(state, source) && state.cell.isConnectable() ? this.isEnabled() ? (_a2 = this.graph.getAllConnectionConstraints(state, source)) != null ? _a2 : [] : [] : null;
      if (this.constraints && state) {
        this.currentFocus = state;
        this.currentFocusArea = new Rectangle_default(state.x, state.y, state.width, state.height);
        for (let i = 0; i < this.focusIcons.length; i += 1) {
          this.focusIcons[i].destroy();
        }
        this.focusIcons = [];
        this.focusPoints = [];
        for (let i = 0; i < this.constraints.length; i += 1) {
          const cp = this.graph.getConnectionPoint(state, this.constraints[i]);
          const img = this.getImageForConstraint(state, this.constraints[i], cp);
          const { src } = img;
          const bounds = new Rectangle_default(Math.round(cp.x - img.width / 2), Math.round(cp.y - img.height / 2), img.width, img.height);
          const icon = new ImageShape_default(bounds, src);
          icon.dialect = this.graph.dialect !== "svg" ? "mixedHtml" : "svg";
          icon.preserveImageAspect = false;
          icon.init(this.graph.getView().getDecoratorPane());
          if (icon.node.previousSibling) {
            (_b = icon.node.parentNode) == null ? void 0 : _b.insertBefore(icon.node, icon.node.parentNode.firstChild);
          }
          const getState = () => {
            return this.currentFocus ? this.currentFocus : state;
          };
          icon.redraw();
          InternalEvent_default.redirectMouseEvents(icon.node, this.graph, getState);
          this.currentFocusArea.add(icon.bounds);
          this.focusIcons.push(icon);
          this.focusPoints.push(cp);
        }
        this.currentFocusArea.grow(this.getTolerance(me));
      } else {
        this.destroyIcons();
        this.destroyFocusHighlight();
      }
    }
    /**
     * Create the shape used to paint the highlight.
     *
     * Returns true if the given icon intersects the given point.
     */
    createHighlightShape() {
      const hl = new RectangleShape_default(new Rectangle_default(), this.highlightColor, this.highlightColor, HIGHLIGHT_STROKEWIDTH);
      hl.opacity = HIGHLIGHT_OPACITY;
      return hl;
    }
    /**
     * Returns `true` if the given icon intersects the given rectangle.
     */
    intersects(icon, rectangle, source, existingEdge) {
      return intersects(icon.bounds, rectangle);
    }
    /**
     * Destroy this handler.
     */
    onDestroy() {
      this.reset();
      this.graph.model.removeListener(this.resetHandler);
      this.graph.view.removeListener(this.resetHandler);
      this.graph.removeListener(this.resetHandler);
      if (this.mouseleaveHandler && this.graph.container) {
        InternalEvent_default.removeListener(this.graph.container, "mouseleave", this.mouseleaveHandler);
        this.mouseleaveHandler = null;
      }
    }
  };
  var ConstraintHandler_default = ConstraintHandler;

  // node_modules/@maxgraph/core/lib/esm/view/handler/config.js
  var EdgeHandlerConfig = {
    addBendOnShiftClickEnabled: false,
    connectFillColor: CONNECT_HANDLE_FILLCOLOR,
    cursorBend: "crosshair",
    cursorMovable: "move",
    cursorTerminal: "pointer",
    cursorVirtualBend: "crosshair",
    handleShape: "square",
    removeBendOnShiftClickEnabled: false,
    selectionColor: EDGE_SELECTION_COLOR,
    selectionDashed: EDGE_SELECTION_DASHED,
    selectionStrokeWidth: EDGE_SELECTION_STROKEWIDTH,
    virtualBendOpacity: 20,
    virtualBendsEnabled: false
  };
  var defaultEdgeHandlerConfig = __spreadValues({}, EdgeHandlerConfig);
  var HandleConfig = {
    /**
     * Defines the default color to be used for the handle fill color. Use `none` for no color.
     * @default {@link HANDLE_FILLCOLOR}
     */
    fillColor: HANDLE_FILLCOLOR,
    /**
     * Defines the cursor to be used for the label handle.
     * @default 'default'
     * @since 0.20.0
     */
    labelCursor: "default",
    /**
     * Defines the color to be used for the label handle fill color. Use `none` for no color.
     * @default {@link LABEL_HANDLE_FILLCOLOR}
     */
    labelFillColor: LABEL_HANDLE_FILLCOLOR,
    /**
     * Defines the default size for label handles.
     * @default {@link LABEL_HANDLE_SIZE}
     */
    labelSize: LABEL_HANDLE_SIZE,
    /**
     * Defines the default size for handles.
     * @default {@link HANDLE_SIZE}
     */
    size: HANDLE_SIZE,
    /**
     * Defines the default color to be used for the handle stroke color. Use `none` for no color.
     * @default {@link HANDLE_STROKECOLOR}
     */
    strokeColor: HANDLE_STROKECOLOR
  };
  var defaultHandleConfig = __spreadValues({}, HandleConfig);
  var VertexHandlerConfig = {
    /**
     * Defines the cursor for a movable vertex.
     * @since 0.20.0
     */
    cursorMovable: "move",
    /**
     * Enable rotation handle
     * @default false
     */
    rotationEnabled: false,
    /**
     * Defines the default color to be used for the selection border of vertices. Use `none` for no color.
     * @default {@link VERTEX_SELECTION_COLOR}
     * @since 0.14.0
     */
    selectionColor: VERTEX_SELECTION_COLOR,
    /**
     * Defines the default stroke width to be used for vertex selections.
     * @default {@link VERTEX_SELECTION_STROKEWIDTH}
     * @since 0.14.0
     */
    selectionStrokeWidth: VERTEX_SELECTION_STROKEWIDTH,
    /**
     * Defines the default dashed state to be used for the vertex selection border.
     * @default {@link VERTEX_SELECTION_DASHED}
     * @since 0.14.0
     */
    selectionDashed: VERTEX_SELECTION_DASHED
  };
  var defaultVertexHandlerConfig = __spreadValues({}, VertexHandlerConfig);

  // node_modules/@maxgraph/core/lib/esm/view/handler/EdgeHandler.js
  var EdgeHandler = class {
    constructor(state) {
      this.error = null;
      this.bends = [];
      this.cloneEnabled = true;
      this.dblClickRemoveEnabled = false;
      this.mergeRemoveEnabled = false;
      this.straightRemoveEnabled = false;
      this.parentHighlightEnabled = false;
      this.preferHtml = false;
      this.allowHandleBoundsCheck = true;
      this.snapToTerminals = false;
      this.handleImage = null;
      this.labelHandleImage = null;
      this.tolerance = 0;
      this.outlineConnect = false;
      this.manageLabelHandle = false;
      this.currentPoint = null;
      this.parentHighlight = null;
      this.index = null;
      this.isSource = false;
      this.isTarget = false;
      this.isLabel = false;
      this.points = [];
      this.snapPoint = null;
      this.abspoints = [];
      this.startX = 0;
      this.startY = 0;
      this.outline = true;
      this.active = true;
      this.state = state;
      this.graph = this.state.view.graph;
      this.marker = this.createMarker();
      this.constraintHandler = this.createConstraintHandler();
      this.points = [];
      this.abspoints = this.getSelectionPoints(this.state);
      this.shape = this.createSelectionShape(this.abspoints);
      this.shape.dialect = this.graph.dialect !== "svg" ? "mixedHtml" : "svg";
      this.shape.init(this.graph.getView().getOverlayPane());
      this.shape.pointerEvents = false;
      this.shape.setCursor(EdgeHandlerConfig.cursorMovable);
      InternalEvent_default.redirectMouseEvents(this.shape.node, this.graph, this.state);
      this.preferHtml = this.state.text != null && this.state.text.node.parentNode === this.graph.container;
      if (!this.preferHtml) {
        const sourceState = this.state.getVisibleTerminalState(true);
        if (sourceState != null) {
          this.preferHtml = sourceState.text != null && sourceState.text.node.parentNode === this.graph.container;
        }
        if (!this.preferHtml) {
          const targetState = this.state.getVisibleTerminalState(false);
          if (targetState != null) {
            this.preferHtml = targetState.text != null && targetState.text.node.parentNode === this.graph.container;
          }
        }
      }
      const selectionHandler = this.graph.getPlugin("SelectionHandler");
      if (selectionHandler && (this.graph.getSelectionCount() < selectionHandler.maxCells || selectionHandler.maxCells <= 0)) {
        this.bends = this.createBends();
        if (this.isVirtualBendsEnabled()) {
          this.virtualBends = this.createVirtualBends();
        }
      }
      this.label = new Point_default(this.state.absoluteOffset.x, this.state.absoluteOffset.y);
      this.labelShape = this.createLabelHandleShape();
      this.initBend(this.labelShape);
      this.labelShape.setCursor(HandleConfig.labelCursor);
      this.customHandles = this.createCustomHandles();
      this.updateParentHighlight();
      this.redraw();
      this.escapeHandler = (_sender, _evt) => {
        const dirty = this.index != null;
        this.reset();
        if (dirty) {
          this.graph.cellRenderer.redraw(this.state, false, state.view.isRendering());
        }
      };
      this.state.view.graph.addListener(InternalEvent_default.ESCAPE, this.escapeHandler);
    }
    /**
     * Hook for subclasses to change the implementation of {@link ConstraintHandler} used here.
     * @since 0.21.0
     */
    createConstraintHandler() {
      return new ConstraintHandler_default(this.graph);
    }
    /**
     * Returns true if the parent highlight should be visible. This implementation
     * always returns true.
     */
    isParentHighlightVisible() {
      const parent = this.state.cell.getParent();
      return parent ? !this.graph.isCellSelected(parent) : null;
    }
    /**
     * Updates the highlight of the parent if {@link parentHighlightEnabled} is true.
     */
    updateParentHighlight() {
      if (!this.isDestroyed()) {
        const visible = this.isParentHighlightVisible();
        const parent = this.state.cell.getParent();
        const pstate = parent ? this.graph.view.getState(parent) : null;
        if (this.parentHighlight) {
          if (parent && parent.isVertex() && visible) {
            const b = this.parentHighlight.bounds;
            if (pstate && b && (b.x !== pstate.x || b.y !== pstate.y || b.width !== pstate.width || b.height !== pstate.height)) {
              this.parentHighlight.bounds = Rectangle_default.fromRectangle(pstate);
              this.parentHighlight.redraw();
            }
          } else {
            if (pstate && pstate.parentHighlight === this.parentHighlight) {
              pstate.parentHighlight = null;
            }
            this.parentHighlight.destroy();
            this.parentHighlight = null;
          }
        } else if (this.parentHighlightEnabled && visible) {
          if (parent && parent.isVertex() && pstate && !pstate.parentHighlight) {
            this.parentHighlight = this.createParentHighlightShape(pstate);
            this.parentHighlight.dialect = "svg";
            this.parentHighlight.pointerEvents = false;
            if (pstate.style.rotation) {
              this.parentHighlight.rotation = pstate.style.rotation;
            }
            this.parentHighlight.init(this.graph.getView().getOverlayPane());
            this.parentHighlight.redraw();
            pstate.parentHighlight = this.parentHighlight;
          }
        }
      }
    }
    /**
     * Returns an array of custom handles. This implementation returns an empty array.
     */
    createCustomHandles() {
      return [];
    }
    /**
     * Returns true if virtual bends should be added. This returns true if
     * {@link virtualBendsEnabled} is true and the current style allows and
     * renders custom waypoints.
     */
    isVirtualBendsEnabled(evt) {
      return EdgeHandlerConfig.virtualBendsEnabled && (this.state.style.edgeStyle == null || this.state.style.edgeStyle === NONE || this.state.style.noEdgeStyle) && this.state.style.shape !== "arrow";
    }
    /**
     * Returns true if the given cell allows new connections to be created. This implementation
     * always returns true.
     */
    isCellEnabled(cell) {
      return true;
    }
    /**
     * Returns true if the given event is a trigger to add a new Point. This
     * implementation returns true if shift is pressed.
     */
    isAddPointEvent(evt) {
      return isShiftDown(evt);
    }
    /**
     * Returns true if the given event is a trigger to remove a point. This
     * implementation returns true if shift is pressed.
     */
    isRemovePointEvent(evt) {
      return isShiftDown(evt);
    }
    /**
     * Returns the list of points that defines the selection stroke.
     */
    getSelectionPoints(state) {
      return state.absolutePoints;
    }
    /**
     * Creates the shape used to draw the selection border.
     */
    createParentHighlightShape(bounds) {
      const shape = new RectangleShape_default(Rectangle_default.fromRectangle(bounds), NONE, this.getSelectionColor());
      shape.strokeWidth = this.getSelectionStrokeWidth();
      shape.isDashed = this.isSelectionDashed();
      return shape;
    }
    /**
     * Creates the shape used to draw the selection border.
     */
    createSelectionShape(points) {
      const c = this.state.shape.constructor;
      const shape = new c();
      shape.outline = true;
      shape.apply(this.state);
      shape.isDashed = this.isSelectionDashed();
      shape.stroke = this.getSelectionColor();
      shape.isShadow = false;
      return shape;
    }
    /**
     * Returns {@link EdgeHandlerConfig.selectionColor}.
     */
    getSelectionColor() {
      return EdgeHandlerConfig.selectionColor;
    }
    /**
     * Returns {@link EdgeHandlerConfig.selectionStrokeWidth}.
     */
    getSelectionStrokeWidth() {
      return EdgeHandlerConfig.selectionStrokeWidth;
    }
    /**
     * Returns {@link EdgeHandlerConfig.selectionDashed}.
     */
    isSelectionDashed() {
      return EdgeHandlerConfig.selectionDashed;
    }
    /**
     * Returns true if the given cell is connectable. This is a hook to
     * disable floating connections. This implementation returns true.
     */
    isConnectableCell(cell) {
      return true;
    }
    /**
     * Creates and returns the {@link CellMarker} used in {@link marker}.
     */
    getCellAt(x, y) {
      return !this.outlineConnect ? this.graph.getCellAt(x, y) : null;
    }
    /**
     * Creates and returns the {@link CellMarker} used in {@link marker}.
     */
    createMarker() {
      return new EdgeHandlerCellMarker(this.graph, this);
    }
    /**
     * Returns the error message or an empty string if the connection for the
     * given source, target pair is not valid. Otherwise, it returns null. This
     * implementation uses {@link AbstractGraph.getEdgeValidationError}.
     *
     * @param source {@link Cell} that represents the source terminal.
     * @param target {@link Cell} that represents the target terminal.
     */
    validateConnection(source, target) {
      return this.graph.getEdgeValidationError(this.state.cell, source, target);
    }
    /**
     * Creates and returns the bends used for modifying the edge. This is
     * typically an array of {@link RectangleShape}.
     */
    createBends() {
      const { cell } = this.state;
      const bends = [];
      for (let i = 0; i < this.abspoints.length; i += 1) {
        if (this.isHandleVisible(i)) {
          const source = i === 0;
          const target = i === this.abspoints.length - 1;
          const terminal = source || target;
          if (terminal || this.graph.isCellBendable(cell)) {
            ((index) => {
              const bend = this.createHandleShape(index);
              this.initBend(bend, () => {
                if (this.dblClickRemoveEnabled) {
                  this.removePoint(this.state, index);
                }
              });
              if (this.isHandleEnabled(i)) {
                bend.setCursor(terminal ? EdgeHandlerConfig.cursorTerminal : EdgeHandlerConfig.cursorBend);
              }
              bends.push(bend);
              if (!terminal) {
                this.points.push(new Point_default(0, 0));
                bend.node.style.visibility = "hidden";
              }
            })(i);
          }
        }
      }
      return bends;
    }
    /**
     * Creates and returns the bends used for modifying the edge. This is
     * typically an array of {@link RectangleShape}.
     */
    createVirtualBends() {
      const { cell } = this.state;
      const last = this.abspoints[0];
      const bends = [];
      if (this.graph.isCellBendable(cell)) {
        for (let i = 1; i < this.abspoints.length; i += 1) {
          ((bend) => {
            this.initBend(bend);
            bend.setCursor(EdgeHandlerConfig.cursorVirtualBend);
            bends.push(bend);
          })(this.createHandleShape());
        }
      }
      return bends;
    }
    /**
     * Creates the shape used to display the given bend.
     */
    isHandleEnabled(index) {
      return true;
    }
    /**
     * Returns true if the handle at the given index is visible.
     */
    isHandleVisible(index) {
      const source = this.state.getVisibleTerminalState(true);
      const target = this.state.getVisibleTerminalState(false);
      const geo = this.state.cell.getGeometry();
      const edgeStyle = geo ? this.graph.view.getEdgeStyle(this.state, geo.points || void 0, source, target) : null;
      return EdgeStyleRegistry.allowsIntermediateHandles(edgeStyle) || index === 0 || index === this.abspoints.length - 1;
    }
    /**
     * Creates the shape used to display the given bend.
     * Note that the index
     * - may be `null` for special cases, such as when called from {@link ElbowEdgeHandler.createVirtualBend}.
     * - is `null` for virtual handles.
     *
     * Only images and rectangles should be returned if support for HTML labels with not foreign objects is required.
     */
    createHandleShape(_index) {
      if (this.handleImage) {
        const shape = new ImageShape_default(new Rectangle_default(0, 0, this.handleImage.width, this.handleImage.height), this.handleImage.src);
        shape.preserveImageAspect = false;
        return shape;
      }
      let s = HandleConfig.size;
      if (this.preferHtml) {
        s -= 1;
      }
      const shapeConstructor = EdgeHandlerConfig.handleShape === "circle" ? EllipseShape_default : RectangleShape_default;
      return new shapeConstructor(new Rectangle_default(0, 0, s, s), HandleConfig.fillColor, HandleConfig.strokeColor);
    }
    /**
     * Creates the shape used to display the label handle.
     */
    createLabelHandleShape() {
      if (this.labelHandleImage) {
        const shape = new ImageShape_default(new Rectangle_default(0, 0, this.labelHandleImage.width, this.labelHandleImage.height), this.labelHandleImage.src);
        shape.preserveImageAspect = false;
        return shape;
      }
      const s = HandleConfig.labelSize;
      return new RectangleShape_default(new Rectangle_default(0, 0, s, s), HandleConfig.labelFillColor, HandleConfig.strokeColor);
    }
    /**
     * Helper method to initialize the given bend.
     *
     * @param bend {@link Shape} that represents the bend to be initialized.
     * @param dblClick Optional function to be called on double click.
     */
    initBend(bend, dblClick) {
      if (this.preferHtml) {
        bend.dialect = "strictHtml";
        bend.init(this.graph.container);
      } else {
        bend.dialect = this.graph.dialect !== "svg" ? "mixedHtml" : "svg";
        bend.init(this.graph.getView().getOverlayPane());
      }
      InternalEvent_default.redirectMouseEvents(bend.node, this.graph, this.state, null, null, null, dblClick);
      if (Client_default.IS_TOUCH) {
        bend.node.setAttribute("pointer-events", "none");
      }
    }
    /**
     * Returns the index of the handle for the given event.
     */
    getHandleForEvent(me) {
      let result = null;
      const tol = !isMouseEvent(me.getEvent()) ? this.tolerance : 1;
      const hit = this.allowHandleBoundsCheck && tol > 0 ? new Rectangle_default(me.getGraphX() - tol, me.getGraphY() - tol, 2 * tol, 2 * tol) : null;
      let minDistSq = Number.POSITIVE_INFINITY;
      function checkShape(shape) {
        if (shape && shape.bounds && shape.node && shape.node.style.display !== "none" && shape.node.style.visibility !== "hidden" && (me.isSource(shape) || hit && intersects(shape.bounds, hit))) {
          const dx = me.getGraphX() - shape.bounds.getCenterX();
          const dy = me.getGraphY() - shape.bounds.getCenterY();
          const tmp = dx * dx + dy * dy;
          if (tmp <= minDistSq) {
            minDistSq = tmp;
            return true;
          }
        }
        return false;
      }
      if (this.isCustomHandleEvent(me) && this.customHandles) {
        for (let i = this.customHandles.length - 1; i >= 0; i--) {
          if (checkShape(this.customHandles[i].shape)) {
            return InternalEvent_default.CUSTOM_HANDLE - i;
          }
        }
      }
      if (me.isSource(this.state.text) || checkShape(this.labelShape)) {
        result = InternalEvent_default.LABEL_HANDLE;
      }
      for (let i = 0; i < this.bends.length; i += 1) {
        if (checkShape(this.bends[i])) {
          result = i;
        }
      }
      if (this.virtualBends && this.isAddVirtualBendEvent(me)) {
        for (let i = 0; i < this.virtualBends.length; i += 1) {
          if (checkShape(this.virtualBends[i])) {
            result = InternalEvent_default.VIRTUAL_HANDLE - i;
          }
        }
      }
      return result;
    }
    /**
     * Returns true if the given event allows virtual bends to be added. This
     * implementation returns true.
     */
    isAddVirtualBendEvent(me) {
      return true;
    }
    /**
     * Returns true if the given event allows custom handles to be changed. This
     * implementation returns true.
     */
    isCustomHandleEvent(me) {
      return true;
    }
    /**
     * Handles the event by checking if a special element of the handler
     * was clicked, in which case the index parameter is non-null. The
     * indices may be one of {@link InternalEvent.LABEL_HANDLE} or the number of the respective
     * control point. The source and target points are used for reconnecting
     * the edge.
     */
    mouseDown(_sender, me) {
      const handle = this.getHandleForEvent(me);
      if (handle !== null && this.bends[handle]) {
        const b = this.bends[handle].bounds;
        if (b)
          this.snapPoint = new Point_default(b.getCenterX(), b.getCenterY());
      }
      if (EdgeHandlerConfig.addBendOnShiftClickEnabled && handle === null && this.isAddPointEvent(me.getEvent())) {
        this.addPoint(this.state, me.getEvent());
        me.consume();
      } else if (handle !== null && !me.isConsumed() && this.graph.isEnabled()) {
        const cell = me.getCell();
        if (EdgeHandlerConfig.removeBendOnShiftClickEnabled && this.isRemovePointEvent(me.getEvent())) {
          this.removePoint(this.state, handle);
        } else if (handle !== InternalEvent_default.LABEL_HANDLE || cell && this.graph.isLabelMovable(cell)) {
          if (this.virtualBends && handle <= InternalEvent_default.VIRTUAL_HANDLE) {
            setOpacity(this.virtualBends[InternalEvent_default.VIRTUAL_HANDLE - handle].node, 100);
          }
          this.start(me.getX(), me.getY(), handle);
        }
        me.consume();
      }
    }
    /**
     * Starts the handling of the mouse gesture.
     */
    start(x, y, index) {
      this.startX = x;
      this.startY = y;
      this.isSource = this.bends.length === 0 ? false : index === 0;
      this.isTarget = this.bends.length === 0 ? false : index === this.bends.length - 1;
      this.isLabel = index === InternalEvent_default.LABEL_HANDLE;
      if (this.isSource || this.isTarget) {
        const { cell } = this.state;
        const terminal = cell.getTerminal(this.isSource);
        if (terminal == null && this.graph.isTerminalPointMovable(cell, this.isSource) || terminal != null && this.graph.isCellDisconnectable(cell, terminal, this.isSource)) {
          this.index = index;
        }
      } else {
        this.index = index;
      }
      if (this.index !== null && this.index <= InternalEvent_default.CUSTOM_HANDLE && this.index > InternalEvent_default.VIRTUAL_HANDLE) {
        if (this.customHandles != null) {
          for (let i = 0; i < this.customHandles.length; i += 1) {
            if (i !== InternalEvent_default.CUSTOM_HANDLE - this.index) {
              this.customHandles[i].setVisible(false);
            }
          }
        }
      }
    }
    /**
     * Returns a clone of the current preview state for the given point and terminal.
     */
    clonePreviewState(point, terminal) {
      return this.state.clone();
    }
    /**
     * Returns the tolerance for the guides. Default value is
     * gridSize * scale / 2.
     */
    getSnapToTerminalTolerance() {
      return this.graph.getGridSize() * this.graph.getView().scale / 2;
    }
    /**
     * Hook for subclassers do show details while the handler is active.
     */
    updateHint(me, point) {
      return;
    }
    /**
     * Hooks for subclassers to hide details when the handler gets inactive.
     */
    removeHint() {
      return;
    }
    /**
     * Hook for rounding the unscaled width or height. This uses Math.round.
     */
    roundLength(length) {
      return Math.round(length);
    }
    /**
     * Returns true if {@link snapToTerminals} is true and if alt is not pressed.
     */
    isSnapToTerminalsEvent(me) {
      return this.snapToTerminals && !isAltDown(me.getEvent());
    }
    /**
     * Returns the point for the given event.
     */
    getPointForEvent(me) {
      const view = this.graph.getView();
      const { scale } = view;
      const point = new Point_default(this.roundLength(me.getGraphX() / scale) * scale, this.roundLength(me.getGraphY() / scale) * scale);
      const tt = this.getSnapToTerminalTolerance();
      let overrideX = false;
      let overrideY = false;
      if (tt > 0 && this.isSnapToTerminalsEvent(me)) {
        const snapToPoint = (pt) => {
          if (pt) {
            const { x } = pt;
            if (Math.abs(point.x - x) < tt) {
              point.x = x;
              overrideX = true;
            }
            const { y } = pt;
            if (Math.abs(point.y - y) < tt) {
              point.y = y;
              overrideY = true;
            }
          }
        };
        const snapToTerminal = (terminal) => {
          if (terminal) {
            snapToPoint(new Point_default(view.getRoutingCenterX(terminal), view.getRoutingCenterY(terminal)));
          }
        };
        snapToTerminal(this.state.getVisibleTerminalState(true));
        snapToTerminal(this.state.getVisibleTerminalState(false));
        for (let i = 0; i < this.state.absolutePoints.length; i += 1) {
          snapToPoint(this.state.absolutePoints[i]);
        }
      }
      if (this.graph.isGridEnabledEvent(me.getEvent())) {
        const tr = view.translate;
        if (!overrideX) {
          point.x = (this.graph.snap(point.x / scale - tr.x) + tr.x) * scale;
        }
        if (!overrideY) {
          point.y = (this.graph.snap(point.y / scale - tr.y) + tr.y) * scale;
        }
      }
      return point;
    }
    /**
     * Updates the given preview state taking into account the state of the constraint handler.
     */
    getPreviewTerminalState(me) {
      this.constraintHandler.update(me, this.isSource, true, me.isSource(this.marker.highlight.shape) ? null : this.currentPoint);
      if (this.constraintHandler.currentFocus && this.constraintHandler.currentConstraint) {
        if (this.marker.highlight && this.marker.highlight.shape && this.marker.highlight.state && this.marker.highlight.state.cell === this.constraintHandler.currentFocus.cell) {
          if (this.marker.highlight.shape.stroke !== "transparent") {
            this.marker.highlight.shape.stroke = "transparent";
            this.marker.highlight.repaint();
          }
        } else {
          this.marker.markCell(this.constraintHandler.currentFocus.cell, "transparent");
        }
        const other = this.graph.view.getTerminalPort(this.state, this.graph.view.getState(this.state.cell.getTerminal(!this.isSource)), !this.isSource);
        const otherCell = other ? other.cell : null;
        const source = this.isSource ? this.constraintHandler.currentFocus.cell : otherCell;
        const target = this.isSource ? otherCell : this.constraintHandler.currentFocus.cell;
        this.error = this.validateConnection(source, target);
        let result = null;
        if (this.error === null) {
          result = this.constraintHandler.currentFocus;
        }
        if (this.error !== null || result && !this.isCellEnabled(result.cell)) {
          this.constraintHandler.reset();
        }
        return result;
      }
      if (!this.graph.isIgnoreTerminalEvent(me.getEvent())) {
        this.marker.process(me);
        const state = this.marker.getValidState();
        if (state && !this.isCellEnabled(state.cell)) {
          this.constraintHandler.reset();
          this.marker.reset();
        }
        return this.marker.getValidState();
      }
      this.marker.reset();
      return null;
    }
    /**
     * Updates the given preview state taking into account the state of the constraint handler.
     *
     * @param pt {@link Point} that contains the current pointer position.
     * @param me Optional {@link MouseEvent} that contains the current event.
     */
    getPreviewPoints(pt, me) {
      const geometry = this.state.cell.getGeometry();
      if (!geometry)
        return null;
      let points = (geometry.points || []).slice();
      const point = new Point_default(pt.x, pt.y);
      let result = null;
      if (!this.isSource && !this.isTarget && this.index !== null) {
        this.convertPoint(point, false);
        if (this.index <= InternalEvent_default.VIRTUAL_HANDLE) {
          points.splice(InternalEvent_default.VIRTUAL_HANDLE - this.index, 0, point);
        }
        if (!this.isSource && !this.isTarget) {
          for (let i = 0; i < this.bends.length; i += 1) {
            if (i !== this.index) {
              const bend = this.bends[i];
              if (bend && contains(bend.bounds, pt.x, pt.y)) {
                if (this.index <= InternalEvent_default.VIRTUAL_HANDLE) {
                  points.splice(InternalEvent_default.VIRTUAL_HANDLE - this.index, 1);
                } else {
                  points.splice(this.index - 1, 1);
                }
                result = points;
              }
            }
          }
          if (!result && this.straightRemoveEnabled && (!me || !isAltDown(me.getEvent()))) {
            const tol = this.graph.getEventTolerance() * this.graph.getEventTolerance();
            const abs = this.state.absolutePoints.slice();
            abs[this.index] = pt;
            const src = this.state.getVisibleTerminalState(true);
            if (src != null) {
              const c = this.graph.getConnectionConstraint(this.state, src, true);
              if (c == null || this.graph.getConnectionPoint(src, c) == null) {
                abs[0] = new Point_default(src.view.getRoutingCenterX(src), src.view.getRoutingCenterY(src));
              }
            }
            const trg = this.state.getVisibleTerminalState(false);
            if (trg != null) {
              const c = this.graph.getConnectionConstraint(this.state, trg, false);
              if (c == null || this.graph.getConnectionPoint(trg, c) == null) {
                abs[abs.length - 1] = new Point_default(trg.view.getRoutingCenterX(trg), trg.view.getRoutingCenterY(trg));
              }
            }
            const checkRemove = (idx, tmp) => {
              if (idx > 0 && idx < abs.length - 1 && ptSegDistSq(abs[idx - 1].x, abs[idx - 1].y, abs[idx + 1].x, abs[idx + 1].y, tmp.x, tmp.y) < tol) {
                points.splice(idx - 1, 1);
                result = points;
              }
            };
            checkRemove(this.index, pt);
          }
        }
        if (result == null && this.index > InternalEvent_default.VIRTUAL_HANDLE) {
          points[this.index - 1] = point;
        }
      } else if (this.graph.isResetEdgesOnConnect()) {
        points = [];
      }
      return result != null ? result : points;
    }
    /**
     * Returns true if {@link outlineConnect} is true and the source of the event is the outline shape
     * or shift is pressed.
     */
    isOutlineConnectEvent(me) {
      if (!this.currentPoint)
        return false;
      const offset = getOffset(this.graph.container);
      const evt = me.getEvent();
      const clientX = getClientX(evt);
      const clientY = getClientY(evt);
      const doc = document.documentElement;
      const left = (window.pageXOffset || doc.scrollLeft) - (doc.clientLeft || 0);
      const top = (window.pageYOffset || doc.scrollTop) - (doc.clientTop || 0);
      const gridX = this.currentPoint.x - this.graph.container.scrollLeft + offset.x - left;
      const gridY = this.currentPoint.y - this.graph.container.scrollTop + offset.y - top;
      return this.outlineConnect && !isShiftDown(me.getEvent()) && (me.isSource(this.marker.highlight.shape) || isAltDown(me.getEvent()) && me.getState() != null || this.marker.highlight.isHighlightAt(clientX, clientY) || (gridX !== clientX || gridY !== clientY) && me.getState() == null && this.marker.highlight.isHighlightAt(gridX, gridY));
    }
    /**
     * Updates the given preview state taking into account the state of the constraint handler.
     */
    updatePreviewState(edgeState, point, terminalState, me, outline = false) {
      const sourceState = this.isSource ? terminalState : this.state.getVisibleTerminalState(true);
      const targetState = this.isTarget ? terminalState : this.state.getVisibleTerminalState(false);
      let sourceConstraint = this.graph.getConnectionConstraint(edgeState, sourceState, true);
      let targetConstraint = this.graph.getConnectionConstraint(edgeState, targetState, false);
      let constraint = this.constraintHandler.currentConstraint;
      if (constraint == null && outline) {
        if (terminalState != null) {
          if (me.isSource(this.marker.highlight.shape)) {
            point = new Point_default(me.getGraphX(), me.getGraphY());
          }
          constraint = this.graph.getOutlineConstraint(point, terminalState, me);
          this.constraintHandler.setFocus(me, terminalState, this.isSource);
          this.constraintHandler.currentConstraint = constraint;
          this.constraintHandler.currentPoint = point;
        } else {
          constraint = new ConnectionConstraint_default(null);
        }
      }
      if (this.outlineConnect && this.marker.highlight != null && this.marker.highlight.shape != null) {
        const s = this.graph.view.scale;
        if (this.constraintHandler.currentConstraint != null && this.constraintHandler.currentFocus != null) {
          this.marker.highlight.shape.stroke = outline ? OUTLINE_HIGHLIGHT_COLOR : "transparent";
          this.marker.highlight.shape.strokeWidth = OUTLINE_HIGHLIGHT_STROKEWIDTH / s / s;
          this.marker.highlight.repaint();
        } else if (this.marker.hasValidState()) {
          const cell = me.getCell();
          this.marker.highlight.shape.stroke = cell && cell.isConnectable() && this.marker.getValidState() !== me.getState() ? "transparent" : DEFAULT_VALID_COLOR;
          this.marker.highlight.shape.strokeWidth = HIGHLIGHT_STROKEWIDTH / s / s;
          this.marker.highlight.repaint();
        }
      }
      if (this.isSource) {
        sourceConstraint = constraint;
      } else if (this.isTarget) {
        targetConstraint = constraint;
      }
      if (this.isSource || this.isTarget) {
        if (constraint != null && constraint.point != null) {
          edgeState.style[this.isSource ? "exitX" : "entryX"] = constraint.point.x;
          edgeState.style[this.isSource ? "exitY" : "entryY"] = constraint.point.y;
        } else {
          delete edgeState.style[this.isSource ? "exitX" : "entryX"];
          delete edgeState.style[this.isSource ? "exitY" : "entryY"];
        }
      }
      edgeState.setVisibleTerminalState(sourceState, true);
      edgeState.setVisibleTerminalState(targetState, false);
      if (!this.isSource || sourceState != null) {
        edgeState.view.updateFixedTerminalPoint(edgeState, sourceState, true, sourceConstraint);
      }
      if (!this.isTarget || targetState != null) {
        edgeState.view.updateFixedTerminalPoint(edgeState, targetState, false, targetConstraint);
      }
      if ((this.isSource || this.isTarget) && terminalState == null) {
        edgeState.setAbsoluteTerminalPoint(point, this.isSource);
        if (this.marker.getMarkedState() == null) {
          this.error = this.graph.isAllowDanglingEdges() ? null : "";
        }
      }
      edgeState.view.updatePoints(edgeState, this.points, sourceState, targetState);
      edgeState.view.updateFloatingTerminalPoints(edgeState, sourceState, targetState);
    }
    /**
     * Handles the event by updating the preview.
     */
    mouseMove(_sender, me) {
      var _a2;
      if (this.index != null && this.marker != null) {
        this.currentPoint = this.getPointForEvent(me);
        this.error = null;
        if (!this.graph.isIgnoreTerminalEvent(me.getEvent()) && isShiftDown(me.getEvent()) && this.snapPoint != null) {
          if (Math.abs(this.snapPoint.x - this.currentPoint.x) < Math.abs(this.snapPoint.y - this.currentPoint.y)) {
            this.currentPoint.x = this.snapPoint.x;
          } else {
            this.currentPoint.y = this.snapPoint.y;
          }
        }
        if (this.index <= InternalEvent_default.CUSTOM_HANDLE && this.index > InternalEvent_default.VIRTUAL_HANDLE) {
          if (this.customHandles != null) {
            this.customHandles[InternalEvent_default.CUSTOM_HANDLE - this.index].processEvent(me);
            this.customHandles[InternalEvent_default.CUSTOM_HANDLE - this.index].positionChanged();
            if (this.shape != null && this.shape.node != null) {
              this.shape.node.style.display = "none";
            }
          }
        } else if (this.isLabel && this.label) {
          this.label.x = this.currentPoint.x;
          this.label.y = this.currentPoint.y;
        } else {
          this.points = this.getPreviewPoints(this.currentPoint, me);
          let terminalState = this.isSource || this.isTarget ? this.getPreviewTerminalState(me) : null;
          if (this.constraintHandler.currentConstraint != null && this.constraintHandler.currentFocus != null && this.constraintHandler.currentPoint != null) {
            this.currentPoint = this.constraintHandler.currentPoint.clone();
          } else if (this.outlineConnect) {
            const outline = this.isSource || this.isTarget ? this.isOutlineConnectEvent(me) : false;
            if (outline) {
              terminalState = this.marker.highlight.state;
            } else if (terminalState != null && terminalState !== me.getState() && ((_a2 = me.getCell()) == null ? void 0 : _a2.isConnectable()) && this.marker.highlight.shape != null) {
              this.marker.highlight.shape.stroke = "transparent";
              this.marker.highlight.repaint();
              terminalState = null;
            }
          }
          if (terminalState != null && !this.isCellEnabled(terminalState.cell)) {
            terminalState = null;
            this.marker.reset();
          }
          if (this.currentPoint) {
            const clone2 = this.clonePreviewState(this.currentPoint, terminalState != null ? terminalState.cell : null);
            this.updatePreviewState(clone2, this.currentPoint, terminalState, me, this.outline);
            const color = this.error == null ? this.marker.validColor : this.marker.invalidColor;
            this.setPreviewColor(color);
            this.abspoints = clone2.absolutePoints;
            this.active = true;
            this.updateHint(me, this.currentPoint);
          }
        }
        this.drawPreview();
        InternalEvent_default.consume(me.getEvent());
        me.consume();
      }
    }
    /**
     * Handles the event to applying the previewed changes on the edge by
     * using {@link moveLabel}, {@link connect} or {@link changePoints}.
     */
    mouseUp(_sender, me) {
      if (this.index != null && this.marker != null) {
        if (this.shape != null && this.shape.node != null) {
          this.shape.node.style.display = "";
        }
        let edge = this.state.cell;
        const { index } = this;
        this.index = null;
        if (me.getX() !== this.startX || me.getY() !== this.startY) {
          const clone2 = !this.graph.isIgnoreTerminalEvent(me.getEvent()) && this.graph.isCloneEvent(me.getEvent()) && this.cloneEnabled && this.graph.isCellsCloneable();
          if (this.error != null) {
            if (this.error.length > 0) {
              this.graph.validationAlert(this.error);
            }
          } else if (index <= InternalEvent_default.CUSTOM_HANDLE && index > InternalEvent_default.VIRTUAL_HANDLE) {
            if (this.customHandles != null) {
              const model = this.graph.getDataModel();
              model.beginUpdate();
              try {
                this.customHandles[InternalEvent_default.CUSTOM_HANDLE - index].execute(me);
                if (this.shape != null && this.shape.node != null) {
                  this.shape.apply(this.state);
                  this.shape.redraw();
                }
              } finally {
                model.endUpdate();
              }
            }
          } else if (this.isLabel && this.label) {
            this.moveLabel(this.state, this.label.x, this.label.y);
          } else if (this.isSource || this.isTarget) {
            let terminal = null;
            if (this.constraintHandler.currentConstraint != null && this.constraintHandler.currentFocus != null) {
              terminal = this.constraintHandler.currentFocus.cell;
            }
            if (!terminal && this.marker.hasValidState() && this.marker.highlight != null && this.marker.highlight.shape != null && this.marker.highlight.shape.stroke !== "transparent" && this.marker.highlight.shape.stroke !== "white") {
              terminal = this.marker.validState.cell;
            }
            if (terminal) {
              const model = this.graph.getDataModel();
              const parent = edge.getParent();
              model.beginUpdate();
              try {
                if (clone2) {
                  let geo = edge.getGeometry();
                  const cloned = this.graph.cloneCell(edge);
                  model.add(parent, cloned, parent.getChildCount());
                  if (geo != null) {
                    geo = geo.clone();
                    model.setGeometry(cloned, geo);
                  }
                  const other = edge.getTerminal(!this.isSource);
                  this.graph.connectCell(cloned, other, !this.isSource);
                  edge = cloned;
                }
                edge = this.connect(edge, terminal, this.isSource, clone2, me);
              } finally {
                model.endUpdate();
              }
            } else if (this.graph.isAllowDanglingEdges()) {
              const pt = this.abspoints[this.isSource ? 0 : this.abspoints.length - 1];
              pt.x = this.roundLength(pt.x / this.graph.view.scale - this.graph.view.translate.x);
              pt.y = this.roundLength(pt.y / this.graph.view.scale - this.graph.view.translate.y);
              const parent = edge.getParent();
              const pstate = parent ? this.graph.getView().getState(parent) : null;
              if (pstate != null) {
                pt.x -= pstate.origin.x;
                pt.y -= pstate.origin.y;
              }
              pt.x -= this.graph.getPanDx() / this.graph.view.scale;
              pt.y -= this.graph.getPanDy() / this.graph.view.scale;
              edge = this.changeTerminalPoint(edge, pt, this.isSource, clone2);
            }
          } else if (this.active) {
            edge = this.changePoints(edge, this.points, clone2);
          } else {
            this.graph.getView().invalidate(this.state.cell);
            this.graph.getView().validate(this.state.cell);
          }
        } else if (this.graph.isToggleEvent(me.getEvent())) {
          this.graph.selectCellForEvent(this.state.cell, me.getEvent());
        }
        if (this.marker != null) {
          this.reset();
          if (edge !== this.state.cell) {
            this.graph.setSelectionCell(edge);
          }
        }
        me.consume();
      }
    }
    /**
     * Resets the state of this handler.
     */
    reset() {
      if (this.active) {
        this.refresh();
      }
      this.error = null;
      this.index = null;
      this.points = [];
      this.snapPoint = null;
      this.isLabel = false;
      this.isSource = false;
      this.isTarget = false;
      this.active = false;
      if (this.marker) {
        this.marker.reset();
      }
      this.constraintHandler.reset();
      if (this.customHandles) {
        for (let i = 0; i < this.customHandles.length; i += 1) {
          this.customHandles[i].reset();
        }
      }
      this.setPreviewColor(EdgeHandlerConfig.selectionColor);
      this.removeHint();
      this.redraw();
    }
    /**
     * Sets the color of the preview to the given value.
     */
    setPreviewColor(color) {
      this.shape.stroke = color;
    }
    /**
     * Converts the given point in-place from screen to unscaled, untranslated
     * graph coordinates and applies the grid. Returns the given, modified
     * point instance.
     *
     * @param point {@link Point} to be converted.
     * @param gridEnabled Boolean that specifies if the grid should be applied.
     */
    convertPoint(point, gridEnabled) {
      const scale = this.graph.getView().getScale();
      const tr = this.graph.getView().getTranslate();
      if (gridEnabled) {
        point.x = this.graph.snap(point.x);
        point.y = this.graph.snap(point.y);
      }
      point.x = Math.round(point.x / scale - tr.x);
      point.y = Math.round(point.y / scale - tr.y);
      const parent = this.state.cell.getParent();
      const pstate = parent ? this.graph.getView().getState(parent) : parent;
      if (pstate) {
        point.x -= pstate.origin.x;
        point.y -= pstate.origin.y;
      }
      return point;
    }
    /**
     * Changes the coordinates for the label of the given edge.
     *
     * @param edge {@link Cell} that represents the edge.
     * @param x Integer that specifies the x-coordinate of the new location.
     * @param y Integer that specifies the y-coordinate of the new location.
     */
    moveLabel(edgeState, x, y) {
      const model = this.graph.getDataModel();
      let geometry = edgeState.cell.getGeometry();
      if (geometry != null) {
        const { scale } = this.graph.getView();
        geometry = geometry.clone();
        if (geometry.relative) {
          let pt = this.graph.getView().getRelativePoint(edgeState, x, y);
          geometry.x = Math.round(pt.x * 1e4) / 1e4;
          geometry.y = Math.round(pt.y);
          geometry.offset = new Point_default(0, 0);
          pt = this.graph.view.getPoint(edgeState, geometry);
          geometry.offset = new Point_default(Math.round((x - pt.x) / scale), Math.round((y - pt.y) / scale));
        } else {
          const points = edgeState.absolutePoints;
          const p0 = points[0];
          const pe = points[points.length - 1];
          if (p0 != null && pe != null) {
            const cx = p0.x + (pe.x - p0.x) / 2;
            const cy = p0.y + (pe.y - p0.y) / 2;
            geometry.offset = new Point_default(Math.round((x - cx) / scale), Math.round((y - cy) / scale));
            geometry.x = 0;
            geometry.y = 0;
          }
        }
        model.setGeometry(edgeState.cell, geometry);
      }
    }
    /**
     * Changes the terminal or terminal point of the given edge in the graph model.
     *
     * @param edge {@link Cell} that represents the edge to be reconnected.
     * @param terminal {@link Cell} that represents the new terminal.
     * @param isSource Boolean indicating if the new terminal is the source or target terminal.
     * @param _isClone Boolean indicating if the new connection should be a clone of the old edge.
     * @param _me {@link MouseEvent} that contains the mouse up event.
     */
    connect(edge, terminal, isSource, _isClone, _me) {
      const parent = edge.getParent();
      this.graph.batchUpdate(() => {
        let constraint = this.constraintHandler.currentConstraint;
        if (constraint == null) {
          constraint = new ConnectionConstraint_default(null);
        }
        this.graph.connectCell(edge, terminal, isSource, constraint);
      });
      return edge;
    }
    /**
     * Changes the terminal point of the given edge.
     */
    changeTerminalPoint(edge, point, isSource, clone2) {
      const model = this.graph.getDataModel();
      model.batchUpdate(() => {
        if (clone2) {
          const parent = edge.getParent();
          const terminal = edge.getTerminal(!isSource);
          edge = this.graph.cloneCell(edge);
          model.add(parent, edge, parent.getChildCount());
          model.setTerminal(edge, terminal, !isSource);
        }
        let geo = edge.getGeometry();
        if (geo != null) {
          geo = geo.clone();
          geo.setTerminalPoint(point, isSource);
          model.setGeometry(edge, geo);
          this.graph.connectCell(edge, null, isSource, new ConnectionConstraint_default(null));
        }
      });
      return edge;
    }
    /**
     * Changes the control points of the given edge in the graph model.
     */
    changePoints(edge, points, clone2) {
      const model = this.graph.getDataModel();
      model.batchUpdate(() => {
        if (clone2) {
          const parent = edge.getParent();
          const source = edge.getTerminal(true);
          const target = edge.getTerminal(false);
          edge = this.graph.cloneCell(edge);
          model.add(parent, edge, parent.getChildCount());
          model.setTerminal(edge, source, true);
          model.setTerminal(edge, target, false);
        }
        let geo = edge.getGeometry();
        if (geo != null) {
          geo = geo.clone();
          geo.points = points;
          model.setGeometry(edge, geo);
        }
      });
      return edge;
    }
    /**
     * Adds a control point for the given state and event.
     */
    addPoint(state, evt) {
      const pt = convertPoint(this.graph.container, getClientX(evt), getClientY(evt));
      const gridEnabled = this.graph.isGridEnabledEvent(evt);
      this.convertPoint(pt, gridEnabled);
      this.addPointAt(state, pt.x, pt.y);
      InternalEvent_default.consume(evt);
    }
    /**
     * Adds a control point at the given point.
     */
    addPointAt(state, x, y) {
      let geo = state.cell.getGeometry();
      const pt = new Point_default(x, y);
      if (geo != null) {
        geo = geo.clone();
        const t = this.graph.view.translate;
        const s = this.graph.view.scale;
        let offset = new Point_default(t.x * s, t.y * s);
        const parent = this.state.cell.getParent();
        if (parent && parent.isVertex()) {
          const pState = this.graph.view.getState(parent);
          if (pState)
            offset = new Point_default(pState.x, pState.y);
        }
        const index = findNearestSegment(state, pt.x * s + offset.x, pt.y * s + offset.y);
        if (geo.points == null) {
          geo.points = [pt];
        } else {
          geo.points.splice(index, 0, pt);
        }
        this.graph.getDataModel().setGeometry(state.cell, geo);
        this.refresh();
        this.redraw();
      }
    }
    /**
     * Removes the control point at the given index from the given state.
     */
    removePoint(state, index) {
      if (index > 0 && index < this.abspoints.length - 1) {
        let geo = this.state.cell.getGeometry();
        if (geo != null && geo.points != null) {
          geo = geo.clone();
          (geo.points || []).splice(index - 1, 1);
          this.graph.getDataModel().setGeometry(state.cell, geo);
          this.refresh();
          this.redraw();
        }
      }
    }
    /**
     * Returns the fillcolor for the handle at the given index.
     */
    getHandleFillColor(index) {
      const isSource = index === 0;
      const { cell } = this.state;
      const terminal = cell.getTerminal(isSource);
      let color = HandleConfig.fillColor;
      if (terminal != null && !this.graph.isCellDisconnectable(cell, terminal, isSource) || terminal == null && !this.graph.isTerminalPointMovable(cell, isSource)) {
        color = LOCKED_HANDLE_FILLCOLOR;
      } else if (terminal != null && this.graph.isCellDisconnectable(cell, terminal, isSource)) {
        color = EdgeHandlerConfig.connectFillColor;
      }
      return color;
    }
    /**
     * Redraws the preview, and the bends- and label control points.
     */
    redraw(ignoreHandles) {
      this.abspoints = this.state.absolutePoints.slice();
      const g = this.state.cell.getGeometry();
      if (g) {
        const pts = g.points;
        if (this.bends != null && this.bends.length > 0) {
          if (pts != null) {
            if (this.points == null) {
              this.points = [];
            }
            for (let i = 1; i < this.bends.length - 1; i += 1) {
              if (this.bends[i] != null && this.abspoints[i] != null) {
                this.points[i - 1] = pts[i - 1];
              }
            }
          }
        }
      }
      this.drawPreview();
      if (!ignoreHandles) {
        this.redrawHandles();
      }
    }
    /**
     * Redraws the handles.
     */
    redrawHandles() {
      const { cell } = this.state;
      let b = this.labelShape.bounds;
      this.label = new Point_default(this.state.absoluteOffset.x, this.state.absoluteOffset.y);
      this.labelShape.bounds = new Rectangle_default(Math.round(this.label.x - b.width / 2), Math.round(this.label.y - b.height / 2), b.width, b.height);
      const lab = this.graph.getLabel(cell);
      this.labelShape.visible = lab != null && lab.length > 0 && this.graph.isLabelMovable(cell);
      if (this.bends != null && this.bends.length > 0) {
        const n = this.abspoints.length - 1;
        const p0 = this.abspoints[0];
        const x0 = p0.x;
        const y0 = p0.y;
        b = this.bends[0].bounds;
        this.bends[0].bounds = new Rectangle_default(Math.floor(x0 - b.width / 2), Math.floor(y0 - b.height / 2), b.width, b.height);
        this.bends[0].fill = this.getHandleFillColor(0);
        this.bends[0].redraw();
        if (this.manageLabelHandle) {
          this.checkLabelHandle(this.bends[0].bounds);
        }
        const pe = this.abspoints[n];
        const xn = pe.x;
        const yn = pe.y;
        const bn = this.bends.length - 1;
        b = this.bends[bn].bounds;
        this.bends[bn].bounds = new Rectangle_default(Math.floor(xn - b.width / 2), Math.floor(yn - b.height / 2), b.width, b.height);
        this.bends[bn].fill = this.getHandleFillColor(bn);
        this.bends[bn].redraw();
        if (this.manageLabelHandle) {
          this.checkLabelHandle(this.bends[bn].bounds);
        }
        this.redrawInnerBends(p0, pe);
      }
      if (this.virtualBends && this.virtualBends.length > 0) {
        let last = this.abspoints[0];
        for (let i = 0; i < this.virtualBends.length; i += 1) {
          if (this.virtualBends[i] != null && this.abspoints[i + 1] != null) {
            const pt = this.abspoints[i + 1];
            const b2 = this.virtualBends[i];
            const x = last.x + (pt.x - last.x) / 2;
            const y = last.y + (pt.y - last.y) / 2;
            if (b2.bounds) {
              b2.bounds = new Rectangle_default(Math.floor(x - b2.bounds.width / 2), Math.floor(y - b2.bounds.height / 2), b2.bounds.width, b2.bounds.height);
              b2.redraw();
            }
            setOpacity(b2.node, EdgeHandlerConfig.virtualBendOpacity);
            last = pt;
            if (this.manageLabelHandle) {
              this.checkLabelHandle(b2.bounds);
            }
          }
        }
      }
      this.labelShape.redraw();
      if (this.customHandles) {
        for (let i = 0; i < this.customHandles.length; i += 1) {
          const shape = this.customHandles[i].shape;
          if (shape) {
            const temp = shape.node.style.display;
            this.customHandles[i].redraw();
            shape.node.style.display = temp;
            shape.node.style.visibility = this.isCustomHandleVisible(this.customHandles[i]) ? "" : "hidden";
          }
        }
      }
    }
    /**
     * Returns true if the given custom handle is visible.
     */
    isCustomHandleVisible(handle) {
      return !this.graph.isEditing() && this.state.view.graph.getSelectionCount() === 1;
    }
    /**
     * Shortcut to {@link hideSizers}.
     */
    setHandlesVisible(visible) {
      for (let i = 0; i < this.bends.length; i += 1) {
        this.bends[i].node.style.display = visible ? "" : "none";
      }
      if (this.virtualBends) {
        for (let i = 0; i < this.virtualBends.length; i += 1) {
          this.virtualBends[i].node.style.display = visible ? "" : "none";
        }
      }
      this.labelShape.node.style.display = visible ? "" : "none";
      if (this.customHandles) {
        for (let i = 0; i < this.customHandles.length; i += 1) {
          this.customHandles[i].setVisible(visible);
        }
      }
    }
    /**
     * Updates and redraws the inner bends.
     *
     * @param p0 {@link Point} that represents the location of the first point.
     * @param pe {@link Point} that represents the location of the last point.
     */
    redrawInnerBends(p0, pe) {
      for (let i = 1; i < this.bends.length - 1; i += 1) {
        if (this.bends[i] != null) {
          if (this.abspoints[i] != null) {
            const { x } = this.abspoints[i];
            const { y } = this.abspoints[i];
            const b = this.bends[i].bounds;
            this.bends[i].node.style.visibility = "visible";
            this.bends[i].bounds = new Rectangle_default(Math.round(x - b.width / 2), Math.round(y - b.height / 2), b.width, b.height);
            if (this.manageLabelHandle) {
              this.checkLabelHandle(this.bends[i].bounds);
            } else if (this.handleImage == null && this.labelShape.visible && intersects(this.bends[i].bounds, this.labelShape.bounds)) {
              const w = HandleConfig.size + 3;
              const h = w;
              this.bends[i].bounds = new Rectangle_default(Math.round(x - w / 2), Math.round(y - h / 2), w, h);
            }
            this.bends[i].redraw();
          } else {
            this.bends[i].destroy();
          }
        }
      }
    }
    /**
     * Checks if the label handle intersects the given bounds and moves it if it
     * intersects.
     */
    checkLabelHandle(b) {
      const b2 = this.labelShape.bounds;
      if (intersects(b, b2)) {
        if (b.getCenterY() < b2.getCenterY()) {
          b2.y = b.y + b.height;
        } else {
          b2.y = b.y - b2.height;
        }
      }
    }
    /**
     * Redraws the preview.
     */
    drawPreview() {
      try {
        if (this.isLabel) {
          const b = this.labelShape.bounds;
          const bounds = new Rectangle_default(Math.round(this.label.x - b.width / 2), Math.round(this.label.y - b.height / 2), b.width, b.height);
          if (!b.equals(bounds)) {
            this.labelShape.bounds = bounds;
            this.labelShape.redraw();
          }
        }
        if (this.shape != null && !equalPoints(this.shape.points, this.abspoints)) {
          this.shape.apply(this.state);
          this.shape.points = this.abspoints.slice();
          this.shape.scale = this.state.view.scale;
          this.shape.isDashed = this.isSelectionDashed();
          this.shape.stroke = this.getSelectionColor();
          this.shape.strokeWidth = this.getSelectionStrokeWidth() / this.shape.scale / this.shape.scale;
          this.shape.isShadow = false;
          this.shape.redraw();
        }
        this.updateParentHighlight();
      } catch (e) {
      }
    }
    /**
     * Refreshes the bends of this handler.
     */
    refresh() {
      if (this.state != null) {
        this.abspoints = this.getSelectionPoints(this.state);
        this.points = [];
        this.destroyBends(this.bends);
        this.bends = this.createBends();
        if (this.virtualBends) {
          this.destroyBends(this.virtualBends);
          this.virtualBends = this.createVirtualBends();
        }
        if (this.customHandles) {
          this.destroyBends(this.customHandles);
          this.customHandles = this.createCustomHandles();
        }
        if (this.labelShape != null && this.labelShape.node != null && this.labelShape.node.parentNode != null) {
          this.labelShape.node.parentNode.appendChild(this.labelShape.node);
        }
      }
    }
    /**
     * Returns true if {@link destroy} was called.
     */
    isDestroyed() {
      return this.shape == null;
    }
    /**
     * Destroys all elements in {@link bends}.
     */
    destroyBends(bends) {
      if (bends != null) {
        for (let i = 0; i < bends.length; i += 1) {
          if (bends[i] != null) {
            bends[i].destroy();
          }
        }
      }
    }
    /**
     * Destroys the handler and all its resources and DOM nodes. This does
     * normally not need to be called as handlers are destroyed automatically
     * when the corresponding cell is deselected.
     */
    onDestroy() {
      this.state.view.graph.removeListener(this.escapeHandler);
      this.marker.destroy();
      this.marker = null;
      this.shape.destroy();
      this.shape = null;
      if (this.parentHighlight) {
        const parent = this.state.cell.getParent();
        const pstate = parent ? this.graph.view.getState(parent) : null;
        if (pstate && pstate.parentHighlight === this.parentHighlight) {
          pstate.parentHighlight = null;
        }
        this.parentHighlight.destroy();
        this.parentHighlight = null;
      }
      this.labelShape.destroy();
      this.labelShape = null;
      this.constraintHandler.onDestroy();
      this.constraintHandler = null;
      if (this.virtualBends) {
        this.destroyBends(this.virtualBends);
        this.virtualBends = [];
      }
      if (this.customHandles) {
        this.destroyBends(this.customHandles);
        this.customHandles = [];
      }
      this.destroyBends(this.bends);
      this.bends = [];
      this.removeHint();
    }
  };
  var EdgeHandlerCellMarker = class extends CellMarker_default {
    constructor(graph, edgeHandler, validColor = DEFAULT_VALID_COLOR, invalidColor = DEFAULT_INVALID_COLOR, hotspot = DEFAULT_HOTSPOT) {
      super(graph, validColor, invalidColor, hotspot);
      this.getCell = (me) => {
        let cell = super.getCell(me);
        if ((cell === this.edgeHandler.state.cell || !cell) && this.edgeHandler.currentPoint) {
          cell = this.edgeHandler.graph.getCellAt(this.edgeHandler.currentPoint.x, this.edgeHandler.currentPoint.y);
        }
        if (cell && !cell.isConnectable()) {
          const parent = cell.getParent();
          if (parent && parent.isVertex() && parent.isConnectable()) {
            cell = parent;
          }
        }
        if (cell) {
          if (this.graph.isSwimlane(cell) && this.edgeHandler.currentPoint && this.graph.hitsSwimlaneContent(cell, this.edgeHandler.currentPoint.x, this.edgeHandler.currentPoint.y) || !this.edgeHandler.isConnectableCell(cell) || cell === this.edgeHandler.state.cell || cell && !this.edgeHandler.graph.connectableEdges && cell.isEdge() || this.edgeHandler.state.cell.isAncestor(cell)) {
            cell = null;
          }
        }
        if (cell && !cell.isConnectable()) {
          cell = null;
        }
        return cell;
      };
      this.isValidState = (state) => {
        const cell = this.edgeHandler.state.cell.getTerminal(!this.edgeHandler.isSource);
        const cellState = this.edgeHandler.graph.view.getState(cell);
        const other = this.edgeHandler.graph.view.getTerminalPort(state, cellState, !this.edgeHandler.isSource);
        const otherCell = other ? other.cell : null;
        const source = this.edgeHandler.isSource ? state.cell : otherCell;
        const target = this.edgeHandler.isSource ? otherCell : state.cell;
        this.edgeHandler.error = this.edgeHandler.validateConnection(source, target);
        return !this.edgeHandler.error;
      };
      this.edgeHandler = edgeHandler;
    }
  };
  var EdgeHandler_default = EdgeHandler;

  // node_modules/@maxgraph/core/lib/esm/view/handler/VertexHandler.js
  var VertexHandler = class {
    /**
     * Specifies if a rotation handle should be visible.
     *
     * This implementation returns {@link VertexHandlerConfig.rotationEnabled}.
     * @since 0.12.0
     */
    isRotationEnabled() {
      return VertexHandlerConfig.rotationEnabled;
    }
    /**
     * Constructs an event handler that allows to resize vertices and groups.
     *
     * @param state {@link CellState} of the cell to be resized.
     */
    constructor(state) {
      var _a2;
      this.sizers = [];
      this.singleSizer = false;
      this.index = null;
      this.allowHandleBoundsCheck = true;
      this.handleImage = null;
      this.handlesVisible = true;
      this.tolerance = 0;
      this.parentHighlightEnabled = false;
      this.rotationRaster = true;
      this.rotationCursor = "crosshair";
      this.livePreview = false;
      this.movePreviewToFront = false;
      this.manageSizers = false;
      this.constrainGroupByChildren = false;
      this.rotationHandleVSpacing = -16;
      this.horizontalOffset = 0;
      this.verticalOffset = 0;
      this.minBounds = null;
      this.x0 = 0;
      this.y0 = 0;
      this.customHandles = [];
      this.inTolerance = false;
      this.startX = 0;
      this.startY = 0;
      this.rotationShape = null;
      this.currentAlpha = null;
      this.startAngle = 0;
      this.startDist = 0;
      this.ghostPreview = null;
      this.livePreviewActive = false;
      this.childOffsetX = 0;
      this.childOffsetY = 0;
      this.parentState = null;
      this.parentHighlight = null;
      this.unscaledBounds = null;
      this.preview = null;
      this.labelShape = null;
      this.edgeHandlers = [];
      this.EMPTY_POINT = new Point_default();
      this.state = state;
      this.graph = this.state.view.graph;
      this.selectionBounds = this.getSelectionBounds(this.state);
      this.bounds = new Rectangle_default(this.selectionBounds.x, this.selectionBounds.y, this.selectionBounds.width, this.selectionBounds.height);
      this.selectionBorder = this.createSelectionShape(this.bounds);
      this.selectionBorder.dialect = "svg";
      this.selectionBorder.pointerEvents = false;
      this.selectionBorder.rotation = (_a2 = this.state.style.rotation) != null ? _a2 : 0;
      this.selectionBorder.init(this.graph.getView().getOverlayPane());
      InternalEvent_default.redirectMouseEvents(this.selectionBorder.node, this.graph, this.state);
      if (this.graph.isCellMovable(this.state.cell)) {
        this.selectionBorder.setCursor(VertexHandlerConfig.cursorMovable);
      }
      const selectionHandler = this.getSelectionHandler();
      if (selectionHandler && (selectionHandler.maxCells <= 0 || this.graph.getSelectionCount() < selectionHandler.maxCells)) {
        const resizable = this.graph.isCellResizable(this.state.cell);
        this.sizers = [];
        if (resizable || this.graph.isLabelMovable(this.state.cell) && this.state.width >= 2 && this.state.height >= 2) {
          let i = 0;
          if (resizable) {
            if (!this.singleSizer) {
              this.sizers.push(this.createSizer("nw-resize", i++));
              this.sizers.push(this.createSizer("n-resize", i++));
              this.sizers.push(this.createSizer("ne-resize", i++));
              this.sizers.push(this.createSizer("w-resize", i++));
              this.sizers.push(this.createSizer("e-resize", i++));
              this.sizers.push(this.createSizer("sw-resize", i++));
              this.sizers.push(this.createSizer("s-resize", i++));
            }
            this.sizers.push(this.createSizer("se-resize", i++));
          }
          const geo = this.state.cell.getGeometry();
          if (geo != null && !geo.relative && !this.graph.isSwimlane(this.state.cell) && this.graph.isLabelMovable(this.state.cell)) {
            this.labelShape = this.createSizer(HandleConfig.labelCursor, InternalEvent_default.LABEL_HANDLE, HandleConfig.labelSize, HandleConfig.labelFillColor);
            this.sizers.push(this.labelShape);
          }
        } else if (this.graph.isCellMovable(this.state.cell) && !this.graph.isCellResizable(this.state.cell) && this.state.width < 2 && this.state.height < 2) {
          this.labelShape = this.createSizer(VertexHandlerConfig.cursorMovable, InternalEvent_default.LABEL_HANDLE, void 0, HandleConfig.labelFillColor);
          this.sizers.push(this.labelShape);
        }
      }
      if (this.isRotationHandleVisible()) {
        this.rotationShape = this.createSizer(this.rotationCursor, InternalEvent_default.ROTATION_HANDLE, HandleConfig.size + 3, HandleConfig.fillColor);
        this.sizers.push(this.rotationShape);
      }
      this.customHandles = this.createCustomHandles();
      this.redraw();
      if (this.constrainGroupByChildren) {
        this.updateMinBounds();
      }
      this.escapeHandler = (_sender, _evt) => {
        if (this.livePreview && this.index != null) {
          this.state.view.graph.cellRenderer.redraw(this.state, true);
          this.state.view.invalidate(this.state.cell);
          this.state.invalid = false;
          this.state.view.validate();
        }
        this.reset();
      };
      this.state.view.graph.addListener(InternalEvent_default.ESCAPE, this.escapeHandler);
    }
    getSelectionHandler() {
      return this.graph.getPlugin("SelectionHandler");
    }
    /**
     * Returns `true` if the rotation handle should be showing.
     */
    isRotationHandleVisible() {
      const selectionHandler = this.getSelectionHandler();
      const selectionHandlerCheck = selectionHandler ? selectionHandler.maxCells <= 0 || this.graph.getSelectionCount() < selectionHandler.maxCells : true;
      return this.graph.isEnabled() && this.isRotationEnabled() && this.graph.isCellRotatable(this.state.cell) && selectionHandlerCheck;
    }
    /**
     * Returns `true` if the aspect ratio if the cell should be maintained.
     */
    isConstrainedEvent(me) {
      return isShiftDown(me.getEvent()) || this.state.style.aspect === "fixed";
    }
    /**
     * Returns `true` if the center of the vertex should be maintained during the resize.
     */
    isCenteredEvent(state, me) {
      return false;
    }
    /**
     * Returns an array of custom handles.
     *
     * This implementation returns an empty array.
     */
    createCustomHandles() {
      return [];
    }
    /**
     * Initializes the shapes required for this vertex handler.
     */
    updateMinBounds() {
      const children = this.graph.getChildCells(this.state.cell);
      if (children.length > 0) {
        this.minBounds = this.graph.view.getBounds(children);
        if (this.minBounds) {
          const s = this.state.view.scale;
          const t = this.state.view.translate;
          this.minBounds.x -= this.state.x;
          this.minBounds.y -= this.state.y;
          this.minBounds.x /= s;
          this.minBounds.y /= s;
          this.minBounds.width /= s;
          this.minBounds.height /= s;
          this.x0 = this.state.x / s - t.x;
          this.y0 = this.state.y / s - t.y;
        }
      }
    }
    /**
     * Returns the Rectangle that defines the bounds of the selection border.
     */
    getSelectionBounds(state) {
      return new Rectangle_default(Math.round(state.x), Math.round(state.y), Math.round(state.width), Math.round(state.height));
    }
    /**
     * Creates the shape used to draw the selection border.
     */
    createParentHighlightShape(bounds) {
      return this.createSelectionShape(bounds);
    }
    /**
     * Creates the shape used to draw the selection border.
     */
    createSelectionShape(bounds) {
      const shape = new RectangleShape_default(Rectangle_default.fromRectangle(bounds), NONE, this.getSelectionColor());
      shape.strokeWidth = this.getSelectionStrokeWidth();
      shape.isDashed = this.isSelectionDashed();
      return shape;
    }
    /**
     * Returns {@link VertexHandlerConfig.selectionColor}.
     */
    getSelectionColor() {
      return VertexHandlerConfig.selectionColor;
    }
    /**
     * Returns {@link VertexHandlerConfig.selectionStrokeWidth}.
     */
    getSelectionStrokeWidth() {
      return VertexHandlerConfig.selectionStrokeWidth;
    }
    /**
     * Returns {@link VertexHandlerConfig.selectionDashed}.
     */
    isSelectionDashed() {
      return VertexHandlerConfig.selectionDashed;
    }
    /**
     * Creates a sizer handle for the specified cursor and index and returns
     * the new {@link RectangleShape} that represents the handle.
     */
    createSizer(cursor, index, size = HandleConfig.size, fillColor = HandleConfig.fillColor) {
      const bounds = new Rectangle_default(0, 0, size, size);
      const sizer = this.createSizerShape(bounds, index, fillColor);
      if (sizer.bounds && sizer.isHtmlAllowed() && this.state.text && this.state.text.node.parentNode === this.graph.container) {
        sizer.bounds.height -= 1;
        sizer.bounds.width -= 1;
        sizer.dialect = "strictHtml";
        sizer.init(this.graph.container);
      } else {
        sizer.dialect = this.graph.dialect !== "svg" ? "mixedHtml" : "svg";
        sizer.init(this.graph.getView().getOverlayPane());
      }
      InternalEvent_default.redirectMouseEvents(sizer.node, this.graph, this.state);
      if (this.graph.isEnabled()) {
        sizer.setCursor(cursor);
      }
      if (!this.isSizerVisible(index)) {
        sizer.visible = false;
      }
      return sizer;
    }
    /**
     * Returns `true` if the sizer for the given index is visible.
     *
     * This implementation returns `true` for all given indices.
     */
    isSizerVisible(_index) {
      return true;
    }
    /**
     * Creates the shape used for the sizer handle for the specified bounds an
     * index. Only images and rectangles should be returned if support for HTML
     * labels with not foreign objects is required.
     */
    createSizerShape(bounds, index, fillColor = HandleConfig.fillColor) {
      if (this.handleImage) {
        bounds = new Rectangle_default(bounds.x, bounds.y, this.handleImage.width, this.handleImage.height);
        const shape = new ImageShape_default(bounds, this.handleImage.src);
        shape.preserveImageAspect = false;
        return shape;
      }
      const strokeColor = HandleConfig.strokeColor;
      if (index === InternalEvent_default.ROTATION_HANDLE) {
        return new EllipseShape_default(bounds, fillColor, strokeColor);
      }
      return new RectangleShape_default(bounds, fillColor, strokeColor);
    }
    /**
     * Helper method to create an {@link Rectangle} around the given center point
     * with a width and height of 2*s or 6, if no s is given.
     */
    moveSizerTo(shape, x, y) {
      if (shape && shape.bounds) {
        shape.bounds.x = Math.floor(x - shape.bounds.width / 2);
        shape.bounds.y = Math.floor(y - shape.bounds.height / 2);
        if (shape.node && shape.node.style.display !== "none") {
          shape.redraw();
        }
      }
    }
    /**
     * Returns the index of the handle for the given event. This returns the index
     * of the sizer from where the event originated or {@link InternalEvent.LABEL_HANDLE}.
     */
    getHandleForEvent(me) {
      const tol = !isMouseEvent(me.getEvent()) ? this.tolerance : 1;
      const hit = this.allowHandleBoundsCheck && tol > 0 ? new Rectangle_default(me.getGraphX() - tol, me.getGraphY() - tol, 2 * tol, 2 * tol) : null;
      const checkShape = (shape) => {
        const st = shape && shape.constructor !== ImageShape_default && this.allowHandleBoundsCheck ? shape.strokeWidth + shape.svgStrokeTolerance : null;
        const real = st ? new Rectangle_default(me.getGraphX() - Math.floor(st / 2), me.getGraphY() - Math.floor(st / 2), st, st) : hit;
        return shape && shape.bounds && (me.isSource(shape) || real && intersects(shape.bounds, real) && shape.node.style.display !== "none" && shape.node.style.visibility !== "hidden");
      };
      if (checkShape(this.rotationShape)) {
        return InternalEvent_default.ROTATION_HANDLE;
      }
      if (checkShape(this.labelShape)) {
        return InternalEvent_default.LABEL_HANDLE;
      }
      for (let i = 0; i < this.sizers.length; i += 1) {
        if (checkShape(this.sizers[i])) {
          return i;
        }
      }
      if (this.customHandles != null && this.isCustomHandleEvent(me)) {
        for (let i = this.customHandles.length - 1; i >= 0; i--) {
          if (checkShape(this.customHandles[i].shape)) {
            return InternalEvent_default.CUSTOM_HANDLE - i;
          }
        }
      }
      return null;
    }
    /**
     * Returns `true` if the given event allows custom handles to be changed.
     *
     * This implementation returns `true`.
     */
    isCustomHandleEvent(me) {
      return true;
    }
    /**
     * Handles the event if a handle has been clicked. By consuming the
     * event all subsequent events of the gesture are redirected to this
     * handler.
     */
    mouseDown(_sender, me) {
      if (!me.isConsumed() && this.graph.isEnabled()) {
        const handle = this.getHandleForEvent(me);
        if (!isNullish(handle)) {
          this.start(me.getGraphX(), me.getGraphY(), handle);
          me.consume();
        }
      }
    }
    /**
     * Called if {@link livePreview} is enabled to check if a border should be painted.
     *
     * This implementation returns `true` if the shape is transparent.
     */
    isLivePreviewBorder() {
      return this.state.shape && this.state.shape.fill === NONE && this.state.shape.stroke === NONE;
    }
    /**
     * Starts the handling of the mouse gesture.
     */
    start(x, y, index) {
      var _a2;
      this.livePreviewActive = this.livePreview && this.state.cell.getChildCount() === 0;
      this.inTolerance = true;
      this.childOffsetX = 0;
      this.childOffsetY = 0;
      this.index = index;
      this.startX = x;
      this.startY = y;
      if (this.index <= InternalEvent_default.CUSTOM_HANDLE && this.isGhostPreview()) {
        this.ghostPreview = this.createGhostPreview();
      } else {
        const parent = this.state.cell.getParent();
        if (this.state.view.currentRoot !== parent && parent && (parent.isVertex() || parent.isEdge())) {
          this.parentState = this.state.view.graph.view.getState(parent);
        }
        this.selectionBorder.node.style.display = index === InternalEvent_default.ROTATION_HANDLE ? "inline" : "none";
        if (!this.livePreviewActive || this.isLivePreviewBorder()) {
          this.preview = this.createSelectionShape(this.bounds);
          if (!(Client_default.IS_SVG && ((_a2 = this.state.style.rotation) != null ? _a2 : 0) != 0) && this.state.text != null && this.state.text.node.parentNode === this.graph.container) {
            this.preview.dialect = "strictHtml";
            this.preview.init(this.graph.container);
          } else {
            this.preview.dialect = "svg";
            this.preview.init(this.graph.view.getOverlayPane());
          }
        }
        if (index === InternalEvent_default.ROTATION_HANDLE) {
          const pos = this.getRotationHandlePosition();
          const dx = pos.x - this.state.getCenterX();
          const dy = pos.y - this.state.getCenterY();
          this.startAngle = dx !== 0 ? Math.atan(dy / dx) * 180 / Math.PI + 90 : 0;
          this.startDist = Math.sqrt(dx * dx + dy * dy);
        }
        if (this.livePreviewActive) {
          this.hideSizers();
          if (index === InternalEvent_default.ROTATION_HANDLE && this.rotationShape) {
            this.rotationShape.node.style.display = "";
          } else if (index === InternalEvent_default.LABEL_HANDLE && this.labelShape) {
            this.labelShape.node.style.display = "";
          } else if (this.sizers[index]) {
            this.sizers[index].node.style.display = "";
          } else if (index <= InternalEvent_default.CUSTOM_HANDLE) {
            this.customHandles[InternalEvent_default.CUSTOM_HANDLE - index].setVisible(true);
          }
          const edges = this.state.cell.getEdges();
          this.edgeHandlers = [];
          const selectionCellsHandler = this.graph.getPlugin("SelectionCellsHandler");
          for (let i = 0; i < edges.length; i += 1) {
            const handler = selectionCellsHandler == null ? void 0 : selectionCellsHandler.getHandler(edges[i]);
            if (handler) {
              this.edgeHandlers.push(handler);
            }
          }
        }
      }
    }
    /**
     * Starts the handling of the mouse gesture.
     */
    createGhostPreview() {
      const shape = this.graph.cellRenderer.createShape(this.state);
      shape.init(this.graph.view.getOverlayPane());
      shape.scale = this.state.view.scale;
      shape.bounds = this.bounds;
      shape.outline = true;
      return shape;
    }
    /**
     * Shortcut to {@link hideSizers}.
     */
    setHandlesVisible(visible) {
      this.handlesVisible = visible;
      for (let i = 0; i < this.sizers.length; i += 1) {
        this.sizers[i].node.style.display = visible ? "" : "none";
      }
      for (let i = 0; i < this.customHandles.length; i += 1) {
        this.customHandles[i].setVisible(visible);
      }
    }
    /**
     * Hides all sizers except.
     *
     * Starts the handling of the mouse gesture.
     */
    hideSizers() {
      this.setHandlesVisible(false);
    }
    /**
     * Checks if the coordinates for the given event are within the
     * {@link AbstractGraph.tolerance}. If the event is a mouse event then the tolerance is
     * ignored.
     */
    checkTolerance(me) {
      if (this.inTolerance && this.startX !== null && this.startY !== null) {
        if (isMouseEvent(me.getEvent()) || Math.abs(me.getGraphX() - this.startX) > this.graph.getEventTolerance() || Math.abs(me.getGraphY() - this.startY) > this.graph.getEventTolerance()) {
          this.inTolerance = false;
        }
      }
    }
    /**
     * Hook for subclasses do show details while the handler is active.
     */
    updateHint(me) {
      return;
    }
    /**
     * Hooks for subclasses to hide details when the handler gets inactive.
     */
    removeHint() {
      return;
    }
    /**
     * Hook for rounding the angle. This uses {@link Math.round}.
     */
    roundAngle(angle) {
      return Math.round(angle * 10) / 10;
    }
    /**
     * Hook for rounding the unscaled width or height. This uses {@link Math.round}.
     */
    roundLength(length) {
      return Math.round(length * 100) / 100;
    }
    /**
     * Handles the event by updating the preview.
     */
    mouseMove(_sender, me) {
      if (!me.isConsumed() && this.index != null) {
        this.checkTolerance(me);
        if (!this.inTolerance) {
          if (this.index <= InternalEvent_default.CUSTOM_HANDLE) {
            if (this.customHandles != null) {
              this.customHandles[InternalEvent_default.CUSTOM_HANDLE - this.index].processEvent(me);
              this.customHandles[InternalEvent_default.CUSTOM_HANDLE - this.index].active = true;
              if (this.ghostPreview != null) {
                this.ghostPreview.apply(this.state);
                this.ghostPreview.strokeWidth = this.getSelectionStrokeWidth() / this.ghostPreview.scale / this.ghostPreview.scale;
                this.ghostPreview.isDashed = this.isSelectionDashed();
                this.ghostPreview.stroke = this.getSelectionColor();
                this.ghostPreview.redraw();
                if (this.selectionBounds != null) {
                  this.selectionBorder.node.style.display = "none";
                }
              } else {
                if (this.movePreviewToFront) {
                  this.moveToFront();
                }
                this.customHandles[InternalEvent_default.CUSTOM_HANDLE - this.index].positionChanged();
              }
            }
          } else if (this.index === InternalEvent_default.LABEL_HANDLE) {
            this.moveLabel(me);
          } else {
            if (this.index === InternalEvent_default.ROTATION_HANDLE) {
              this.rotateVertex(me);
            } else {
              this.resizeVertex(me);
            }
            this.updateHint(me);
          }
        }
        me.consume();
      } else if (!this.graph.isMouseDown && !isNullish(this.getHandleForEvent(me))) {
        me.consume(false);
      }
    }
    /**
     * Returns `true` if a ghost preview should be used for custom handles.
     */
    isGhostPreview() {
      return this.state.cell.getChildCount() > 0;
    }
    /**
     * Moves the vertex.
     */
    moveLabel(me) {
      const point = new Point_default(me.getGraphX(), me.getGraphY());
      const tr = this.graph.view.translate;
      const { scale } = this.graph.view;
      if (this.graph.isGridEnabledEvent(me.getEvent())) {
        point.x = (this.graph.snap(point.x / scale - tr.x) + tr.x) * scale;
        point.y = (this.graph.snap(point.y / scale - tr.y) + tr.y) * scale;
      }
      const index = this.rotationShape ? this.sizers.length - 2 : this.sizers.length - 1;
      this.moveSizerTo(this.sizers[index], point.x, point.y);
    }
    /**
     * Rotates the vertex.
     */
    rotateVertex(me) {
      const point = new Point_default(me.getGraphX(), me.getGraphY());
      let dx = this.state.x + this.state.width / 2 - point.x;
      let dy = this.state.y + this.state.height / 2 - point.y;
      this.currentAlpha = dx !== 0 ? Math.atan(dy / dx) * 180 / Math.PI + 90 : dy < 0 ? 180 : 0;
      if (dx > 0) {
        this.currentAlpha -= 180;
      }
      this.currentAlpha -= this.startAngle;
      if (this.rotationRaster && this.graph.isGridEnabledEvent(me.getEvent())) {
        let raster;
        dx = point.x - this.state.getCenterX();
        dy = point.y - this.state.getCenterY();
        const dist = Math.sqrt(dx * dx + dy * dy);
        if (dist - this.startDist < 2) {
          raster = 15;
        } else if (dist - this.startDist < 25) {
          raster = 5;
        } else {
          raster = 1;
        }
        this.currentAlpha = Math.round(this.currentAlpha / raster) * raster;
      } else {
        this.currentAlpha = this.roundAngle(this.currentAlpha);
      }
      this.selectionBorder.rotation = this.currentAlpha;
      this.selectionBorder.redraw();
      if (this.livePreviewActive) {
        this.redrawHandles();
      }
    }
    /**
     * Resizes the vertex.
     */
    resizeVertex(me) {
      var _a2;
      const ct = new Point_default(this.state.getCenterX(), this.state.getCenterY());
      const alpha = toRadians((_a2 = this.state.style.rotation) != null ? _a2 : 0);
      const point = new Point_default(me.getGraphX(), me.getGraphY());
      const tr = this.graph.view.translate;
      const { scale } = this.graph.view;
      let cos = Math.cos(-alpha);
      let sin = Math.sin(-alpha);
      let dx = point.x - this.startX;
      let dy = point.y - this.startY;
      const tx = cos * dx - sin * dy;
      const ty = sin * dx + cos * dy;
      dx = tx;
      dy = ty;
      const geo = this.state.cell.getGeometry();
      if (geo && this.index !== null) {
        this.unscaledBounds = this.union(geo, dx / scale, dy / scale, this.index, this.graph.isGridEnabledEvent(me.getEvent()), 1, new Point_default(0, 0), this.isConstrainedEvent(me), this.isCenteredEvent(this.state, me));
      }
      if (geo && !geo.relative) {
        let max = this.graph.getMaximumGraphBounds();
        if (max != null && this.parentState != null) {
          max = Rectangle_default.fromRectangle(max);
          max.x -= (this.parentState.x - tr.x * scale) / scale;
          max.y -= (this.parentState.y - tr.y * scale) / scale;
        }
        if (this.graph.isConstrainChild(this.state.cell)) {
          let tmp = this.graph.getCellContainmentArea(this.state.cell);
          if (tmp != null) {
            const overlap = this.graph.getOverlap(this.state.cell);
            if (overlap > 0) {
              tmp = Rectangle_default.fromRectangle(tmp);
              tmp.x -= tmp.width * overlap;
              tmp.y -= tmp.height * overlap;
              tmp.width += 2 * tmp.width * overlap;
              tmp.height += 2 * tmp.height * overlap;
            }
            if (!max) {
              max = tmp;
            } else {
              max = Rectangle_default.fromRectangle(max);
              max.intersect(tmp);
            }
          }
        }
        if (max && this.unscaledBounds) {
          if (this.unscaledBounds.x < max.x) {
            this.unscaledBounds.width -= max.x - this.unscaledBounds.x;
            this.unscaledBounds.x = max.x;
          }
          if (this.unscaledBounds.y < max.y) {
            this.unscaledBounds.height -= max.y - this.unscaledBounds.y;
            this.unscaledBounds.y = max.y;
          }
          if (this.unscaledBounds.x + this.unscaledBounds.width > max.x + max.width) {
            this.unscaledBounds.width -= this.unscaledBounds.x + this.unscaledBounds.width - max.x - max.width;
          }
          if (this.unscaledBounds.y + this.unscaledBounds.height > max.y + max.height) {
            this.unscaledBounds.height -= this.unscaledBounds.y + this.unscaledBounds.height - max.y - max.height;
          }
        }
      }
      if (this.unscaledBounds) {
        const old = this.bounds;
        this.bounds = new Rectangle_default((this.parentState ? this.parentState.x : tr.x * scale) + this.unscaledBounds.x * scale, (this.parentState ? this.parentState.y : tr.y * scale) + this.unscaledBounds.y * scale, this.unscaledBounds.width * scale, this.unscaledBounds.height * scale);
        if (geo && geo.relative && this.parentState) {
          this.bounds.x += this.state.x - this.parentState.x;
          this.bounds.y += this.state.y - this.parentState.y;
        }
        cos = Math.cos(alpha);
        sin = Math.sin(alpha);
        const c2 = new Point_default(this.bounds.getCenterX(), this.bounds.getCenterY());
        dx = c2.x - ct.x;
        dy = c2.y - ct.y;
        const dx2 = cos * dx - sin * dy;
        const dy2 = sin * dx + cos * dy;
        const dx3 = dx2 - dx;
        const dy3 = dy2 - dy;
        const dx4 = this.bounds.x - this.state.x;
        const dy4 = this.bounds.y - this.state.y;
        const dx5 = cos * dx4 - sin * dy4;
        const dy5 = sin * dx4 + cos * dy4;
        this.bounds.x += dx3;
        this.bounds.y += dy3;
        this.unscaledBounds.x = this.roundLength(this.unscaledBounds.x + dx3 / scale);
        this.unscaledBounds.y = this.roundLength(this.unscaledBounds.y + dy3 / scale);
        this.unscaledBounds.width = this.roundLength(this.unscaledBounds.width);
        this.unscaledBounds.height = this.roundLength(this.unscaledBounds.height);
        if (!this.state.cell.isCollapsed() && (dx3 !== 0 || dy3 !== 0)) {
          this.childOffsetX = this.state.x - this.bounds.x + dx5;
          this.childOffsetY = this.state.y - this.bounds.y + dy5;
        } else {
          this.childOffsetX = 0;
          this.childOffsetY = 0;
        }
        if (!old.equals(this.bounds)) {
          if (this.livePreviewActive) {
            this.updateLivePreview(me);
          }
          if (this.preview != null) {
            this.drawPreview();
          } else {
            this.updateParentHighlight();
          }
        }
      }
    }
    /**
     * Repaints the live preview.
     */
    updateLivePreview(me) {
      const { scale } = this.graph.view;
      const tr = this.graph.view.translate;
      const tempState = this.state.clone();
      this.state.x = this.bounds.x;
      this.state.y = this.bounds.y;
      this.state.origin = new Point_default(this.state.x / scale - tr.x, this.state.y / scale - tr.y);
      this.state.width = this.bounds.width;
      this.state.height = this.bounds.height;
      let off = this.state.absoluteOffset;
      off = new Point_default(off.x, off.y);
      this.state.absoluteOffset.x = 0;
      this.state.absoluteOffset.y = 0;
      const geo = this.state.cell.getGeometry();
      if (geo != null) {
        const offset = geo.offset || this.EMPTY_POINT;
        if (offset != null && !geo.relative) {
          this.state.absoluteOffset.x = this.state.view.scale * offset.x;
          this.state.absoluteOffset.y = this.state.view.scale * offset.y;
        }
        this.state.view.updateVertexLabelOffset(this.state);
      }
      this.state.view.graph.cellRenderer.redraw(this.state, true);
      this.state.view.invalidate(this.state.cell);
      this.state.invalid = false;
      this.state.view.validate();
      this.redrawHandles();
      if (this.movePreviewToFront) {
        this.moveToFront();
      }
      if (this.state.control != null && this.state.control.node != null) {
        this.state.control.node.style.visibility = "hidden";
      }
      this.state.setState(tempState);
    }
    /**
     * Handles the event by applying the changes to the geometry.
     */
    moveToFront() {
      if (this.state.text && this.state.text.node && this.state.text.node.nextSibling || this.state.shape && this.state.shape.node && this.state.shape.node.nextSibling && (!this.state.text || this.state.shape.node.nextSibling !== this.state.text.node)) {
        if (this.state.shape && this.state.shape.node && this.state.shape.node.parentNode) {
          this.state.shape.node.parentNode.appendChild(this.state.shape.node);
        }
        if (this.state.text && this.state.text.node && this.state.text.node.parentNode) {
          this.state.text.node.parentNode.appendChild(this.state.text.node);
        }
      }
    }
    /**
     * Handles the event by applying the changes to the geometry.
     */
    mouseUp(_sender, me) {
      if (this.index != null && this.state != null) {
        const point = new Point_default(me.getGraphX(), me.getGraphY());
        const { index } = this;
        this.index = null;
        if (this.ghostPreview == null) {
          this.state.view.invalidate(this.state.cell, false, false);
          this.state.view.validate();
        }
        this.graph.batchUpdate(() => {
          var _a2, _b;
          if (index <= InternalEvent_default.CUSTOM_HANDLE) {
            if (this.customHandles != null) {
              const style = this.state.view.graph.getCellStyle(this.state.cell);
              this.customHandles[InternalEvent_default.CUSTOM_HANDLE - index].active = false;
              this.customHandles[InternalEvent_default.CUSTOM_HANDLE - index].execute(me);
              if (this.customHandles != null && this.customHandles[InternalEvent_default.CUSTOM_HANDLE - index] != null) {
                this.state.style = style;
                this.customHandles[InternalEvent_default.CUSTOM_HANDLE - index].positionChanged();
              }
            }
          } else if (index === InternalEvent_default.ROTATION_HANDLE) {
            if (this.currentAlpha != null) {
              const delta = this.currentAlpha - ((_a2 = this.state.style.rotation) != null ? _a2 : 0);
              if (delta !== 0) {
                this.rotateCell(this.state.cell, delta);
              }
            } else {
              this.rotateClick();
            }
          } else {
            const gridEnabled = this.graph.isGridEnabledEvent(me.getEvent());
            const alpha = toRadians((_b = this.state.style.rotation) != null ? _b : 0);
            const cos = Math.cos(-alpha);
            const sin = Math.sin(-alpha);
            let dx = point.x - this.startX;
            let dy = point.y - this.startY;
            const tx = cos * dx - sin * dy;
            const ty = sin * dx + cos * dy;
            dx = tx;
            dy = ty;
            const s = this.graph.view.scale;
            const recurse = this.isRecursiveResize(this.state, me);
            this.resizeCell(this.state.cell, this.roundLength(dx / s), this.roundLength(dy / s), index, gridEnabled, this.isConstrainedEvent(me), recurse);
          }
        });
        me.consume();
        this.reset();
        this.redrawHandles();
      }
    }
    /**
     * Returns the `recursiveResize` status of the given state.
     * @param state the given {@link CellState}. This implementation takes the value of this state.
     * @param me the mouse event.
     */
    isRecursiveResize(state, me) {
      return this.graph.isRecursiveResize(this.state);
    }
    /**
     * Hook for subclasses to implement a single click on the rotation handle.
     * This code is executed as part of the model transaction.
     *
     * This implementation is empty.
     */
    rotateClick() {
      return;
    }
    /**
     * Rotates the given cell and its children by the given angle in degrees.
     *
     * @param cell {@link Cell} to be rotated.
     * @param angle Angle in degrees.
     * @param parent if set, consider the parent in the rotation computation.
     */
    rotateCell(cell, angle, parent) {
      var _a2;
      if (angle !== 0) {
        const model = this.graph.getDataModel();
        if (cell.isVertex() || cell.isEdge()) {
          if (!cell.isEdge()) {
            const style = this.graph.getCurrentCellStyle(cell);
            const total = ((_a2 = style.rotation) != null ? _a2 : 0) + angle;
            this.graph.setCellStyles("rotation", total, [cell]);
          }
          let geo = cell.getGeometry();
          if (geo && parent) {
            const pgeo = parent.getGeometry();
            if (pgeo != null && !parent.isEdge()) {
              geo = geo.clone();
              geo.rotate(angle, new Point_default(pgeo.width / 2, pgeo.height / 2));
              model.setGeometry(cell, geo);
            }
            if (cell.isVertex() && !geo.relative || cell.isEdge()) {
              const childCount = cell.getChildCount();
              for (let i = 0; i < childCount; i += 1) {
                this.rotateCell(cell.getChildAt(i), angle, cell);
              }
            }
          }
        }
      }
    }
    /**
     * Resets the state of this handler.
     */
    reset() {
      if (this.index !== null && this.sizers[this.index].node.style.display === "none") {
        this.sizers[this.index].node.style.display = "";
      }
      this.index = null;
      this.currentAlpha = null;
      if (this.preview) {
        this.preview.destroy();
        this.preview = null;
      }
      if (this.ghostPreview) {
        this.ghostPreview.destroy();
        this.ghostPreview = null;
      }
      if (this.livePreviewActive) {
        for (let i = 0; i < this.sizers.length; i += 1) {
          this.sizers[i].node.style.display = "";
        }
        if (this.state.control && this.state.control.node) {
          this.state.control.node.style.visibility = "";
        }
      }
      for (let i = 0; i < this.customHandles.length; i += 1) {
        if (this.customHandles[i].active) {
          this.customHandles[i].active = false;
          this.customHandles[i].reset();
        } else {
          this.customHandles[i].setVisible(true);
        }
      }
      this.selectionBorder.node.style.display = "inline";
      this.selectionBounds = this.getSelectionBounds(this.state);
      this.bounds = new Rectangle_default(this.selectionBounds.x, this.selectionBounds.y, this.selectionBounds.width, this.selectionBounds.height);
      this.drawPreview();
      this.removeHint();
      this.redrawHandles();
      this.edgeHandlers = [];
      this.handlesVisible = true;
      this.unscaledBounds = null;
    }
    /**
     * Uses the given vector to change the bounds of the given cell
     * in the graph using {@link AbstractGraph.resizeCell}.
     */
    resizeCell(cell, dx, dy, index, gridEnabled, constrained, recurse) {
      var _a2;
      let geo = cell.getGeometry();
      if (geo) {
        if (index === InternalEvent_default.LABEL_HANDLE && this.labelShape && this.labelShape.bounds) {
          const alpha = -toRadians((_a2 = this.state.style.rotation) != null ? _a2 : 0);
          const cos = Math.cos(alpha);
          const sin = Math.sin(alpha);
          const { scale } = this.graph.view;
          const pt = getRotatedPoint(new Point_default(Math.round((this.labelShape.bounds.getCenterX() - this.startX) / scale), Math.round((this.labelShape.bounds.getCenterY() - this.startY) / scale)), cos, sin);
          geo = geo.clone();
          if (geo.offset == null) {
            geo.offset = pt;
          } else {
            geo.offset.x += pt.x;
            geo.offset.y += pt.y;
          }
          this.graph.model.setGeometry(cell, geo);
        } else if (this.unscaledBounds) {
          const { scale } = this.graph.view;
          if (this.childOffsetX !== 0 || this.childOffsetY !== 0) {
            this.moveChildren(cell, Math.round(this.childOffsetX / scale), Math.round(this.childOffsetY / scale));
          }
          this.graph.resizeCell(cell, this.unscaledBounds, recurse);
        }
      }
    }
    /**
     * Moves the children of the given cell by the given vector.
     */
    moveChildren(cell, dx, dy) {
      const model = this.graph.getDataModel();
      const childCount = cell.getChildCount();
      for (let i = 0; i < childCount; i += 1) {
        const child = cell.getChildAt(i);
        let geo = child.getGeometry();
        if (geo != null) {
          geo = geo.clone();
          geo.translate(dx, dy);
          model.setGeometry(child, geo);
        }
      }
    }
    /**
     * Returns the union of the given bounds and location for the specified
     * handle index.
     *
     * To override this to limit the size of vertex via a minWidth/-Height style,
     * the following code can be used.
     *
     * ```javascript
     * const vertexHandlerUnion = union;
     * vertexHandler.union = (bounds, dx, dy, index, gridEnabled, scale, tr, constrained) => {
     *   const result = vertexHandlerUnion.apply(this, arguments);
     *
     *   result.width = Math.max(result.width, this.state.style.minWidth ?? 0));
     *   result.height = Math.max(result.height, this.state.style.minHeight ?? 0));
     *
     *   return result;
     * };
     * ```
     *
     * The minWidth/-Height style can then be used as follows:
     *
     * ```javascript
     * graph.insertVertex({
     *   parent,
     *   value: 'Hello,',
     *   position: [20, 20],
     *   size: [80, 30],
     *   style: {
     *     minWidth: 100,
     *     minHeight: 100,
     *   },
     * });
     * ```
     *
     * To override this to update the height for a wrapped text if the width of a vertex is
     * changed, the following can be used.
     *
     * ```javascript
     * const vertexHandlerUnion = union;
     * vertexHandler.union = (bounds, dx, dy, index, gridEnabled, scale, tr, constrained) => {
     *   const result = vertexHandlerUnion.apply(this, arguments);
     *   const s = this.state;
     *
     *   if (this.graph.isHtmlLabel(s.cell)
     *        && (index == 3 || index == 4)
     *        && s.text != null && s.style.whiteSpace == 'wrap') {
     *     const label = this.graph.getLabel(s.cell);
     *     const fontSize = s.style.fontSize ?? StyleDefaultsConfig.fontSize;
     *     const ww = result.width / s.view.scale - s.text.spacingRight - s.text.spacingLeft
     *
     *     result.height = styleUtils.getSizeForString(label, fontSize, s.style.fontFamily, ww).height;
     *   }
     *
     *   return result;
     * };
     * ```
     */
    union(bounds, dx, dy, index, gridEnabled, scale, tr, constrained, centered) {
      gridEnabled = gridEnabled && this.graph.isGridEnabled();
      if (this.singleSizer) {
        let x = bounds.x + bounds.width + dx;
        let y = bounds.y + bounds.height + dy;
        if (gridEnabled) {
          x = this.graph.snap(x / scale) * scale;
          y = this.graph.snap(y / scale) * scale;
        }
        const rect = new Rectangle_default(bounds.x, bounds.y, 0, 0);
        rect.add(new Rectangle_default(x, y, 0, 0));
        return rect;
      }
      const w0 = bounds.width;
      const h0 = bounds.height;
      let left = bounds.x - tr.x * scale;
      let right = left + w0;
      let top = bounds.y - tr.y * scale;
      let bottom = top + h0;
      const cx = left + w0 / 2;
      const cy = top + h0 / 2;
      if (index > 4) {
        bottom += dy;
        if (gridEnabled) {
          bottom = this.graph.snap(bottom / scale) * scale;
        } else {
          bottom = Math.round(bottom / scale) * scale;
        }
      } else if (index < 3) {
        top += dy;
        if (gridEnabled) {
          top = this.graph.snap(top / scale) * scale;
        } else {
          top = Math.round(top / scale) * scale;
        }
      }
      if (index === 0 || index === 3 || index === 5) {
        left += dx;
        if (gridEnabled) {
          left = this.graph.snap(left / scale) * scale;
        } else {
          left = Math.round(left / scale) * scale;
        }
      } else if (index === 2 || index === 4 || index === 7) {
        right += dx;
        if (gridEnabled) {
          right = this.graph.snap(right / scale) * scale;
        } else {
          right = Math.round(right / scale) * scale;
        }
      }
      let width = right - left;
      let height = bottom - top;
      if (constrained) {
        const geo = this.state.cell.getGeometry();
        if (geo != null) {
          const aspect = geo.width / geo.height;
          if (index === 1 || index === 2 || index === 7 || index === 6) {
            width = height * aspect;
          } else {
            height = width / aspect;
          }
          if (index === 0) {
            left = right - width;
            top = bottom - height;
          }
        }
      }
      if (centered) {
        width += width - w0;
        height += height - h0;
        const cdx = cx - (left + width / 2);
        const cdy = cy - (top + height / 2);
        left += cdx;
        top += cdy;
        right += cdx;
        bottom += cdy;
      }
      if (width < 0) {
        left += width;
        width = Math.abs(width);
      }
      if (height < 0) {
        top += height;
        height = Math.abs(height);
      }
      const result = new Rectangle_default(left + tr.x * scale, top + tr.y * scale, width, height);
      if (this.minBounds != null) {
        result.width = Math.max(result.width, this.minBounds.x * scale + this.minBounds.width * scale + Math.max(0, this.x0 * scale - result.x));
        result.height = Math.max(result.height, this.minBounds.y * scale + this.minBounds.height * scale + Math.max(0, this.y0 * scale - result.y));
      }
      return result;
    }
    /**
     * Redraws the handles and the preview.
     */
    redraw(ignoreHandles) {
      this.selectionBounds = this.getSelectionBounds(this.state);
      this.bounds = new Rectangle_default(this.selectionBounds.x, this.selectionBounds.y, this.selectionBounds.width, this.selectionBounds.height);
      this.drawPreview();
      if (!ignoreHandles) {
        this.redrawHandles();
      }
    }
    /**
     * Returns the padding to be used for drawing handles for the current <bounds>.
     */
    getHandlePadding() {
      const result = new Point_default(0, 0);
      let tol = this.tolerance;
      if (this.sizers.length > 0 && this.sizers[0].bounds && (this.bounds.width < 2 * this.sizers[0].bounds.width + 2 * tol || this.bounds.height < 2 * this.sizers[0].bounds.height + 2 * tol)) {
        tol /= 2;
        result.x = this.sizers[0].bounds.width + tol;
        result.y = this.sizers[0].bounds.height + tol;
      }
      return result;
    }
    /**
     * Returns the bounds used to paint the resize handles.
     */
    getSizerBounds() {
      return this.bounds;
    }
    /**
     * Redraws the handles. To hide certain handles the following code can be used.
     *
     * ```javascript
     * redrawHandles()
     * {
     *   mxVertexHandlerRedrawHandles.apply(this, arguments);
     *
     *   if (this.sizers != null && this.sizers.length > 7)
     *   {
     *     this.sizers[1].node.style.display = 'none';
     *     this.sizers[6].node.style.display = 'none';
     *   }
     * };
     * ```
     */
    redrawHandles() {
      var _a2, _b, _c, _d;
      let s = this.getSizerBounds();
      const tol = this.tolerance;
      this.horizontalOffset = 0;
      this.verticalOffset = 0;
      for (let i = 0; i < this.customHandles.length; i += 1) {
        const shape = this.customHandles[i].shape;
        if (shape) {
          const temp = shape.node.style.display;
          this.customHandles[i].redraw();
          shape.node.style.display = temp;
          shape.node.style.visibility = this.handlesVisible && this.isCustomHandleVisible(this.customHandles[i]) ? "" : "hidden";
        }
      }
      if (this.sizers.length > 0 && this.sizers[0]) {
        if (this.index === null && this.manageSizers && this.sizers.length >= 8) {
          const padding = this.getHandlePadding();
          this.horizontalOffset = padding.x;
          this.verticalOffset = padding.y;
          if (this.horizontalOffset !== 0 || this.verticalOffset !== 0) {
            s = new Rectangle_default(s.x, s.y, s.width, s.height);
            s.x -= this.horizontalOffset / 2;
            s.width += this.horizontalOffset;
            s.y -= this.verticalOffset / 2;
            s.height += this.verticalOffset;
          }
          if (this.sizers.length >= 8) {
            if (this.sizers[0].bounds && (s.width < 2 * this.sizers[0].bounds.width + 2 * tol || s.height < 2 * this.sizers[0].bounds.height + 2 * tol)) {
              this.sizers[0].node.style.display = "none";
              this.sizers[2].node.style.display = "none";
              this.sizers[5].node.style.display = "none";
              this.sizers[7].node.style.display = "none";
            } else if (this.handlesVisible) {
              this.sizers[0].node.style.display = "";
              this.sizers[2].node.style.display = "";
              this.sizers[5].node.style.display = "";
              this.sizers[7].node.style.display = "";
            }
          }
        }
        const r = s.x + s.width;
        const b = s.y + s.height;
        if (this.singleSizer) {
          this.moveSizerTo(this.sizers[0], r, b);
        } else {
          const cx = s.x + s.width / 2;
          const cy = s.y + s.height / 2;
          if (this.sizers.length >= 8) {
            const crs = [
              "nw-resize",
              "n-resize",
              "ne-resize",
              "e-resize",
              "se-resize",
              "s-resize",
              "sw-resize",
              "w-resize"
            ];
            const alpha = toRadians((_a2 = this.state.style.rotation) != null ? _a2 : 0);
            const cos = Math.cos(alpha);
            const sin = Math.sin(alpha);
            const da = Math.round(alpha * 4 / Math.PI);
            const ct = new Point_default(s.getCenterX(), s.getCenterY());
            let pt = getRotatedPoint(new Point_default(s.x, s.y), cos, sin, ct);
            this.moveSizerTo(this.sizers[0], pt.x, pt.y);
            this.sizers[0].setCursor(crs[mod(0 + da, crs.length)]);
            pt.x = cx;
            pt.y = s.y;
            pt = getRotatedPoint(pt, cos, sin, ct);
            this.moveSizerTo(this.sizers[1], pt.x, pt.y);
            this.sizers[1].setCursor(crs[mod(1 + da, crs.length)]);
            pt.x = r;
            pt.y = s.y;
            pt = getRotatedPoint(pt, cos, sin, ct);
            this.moveSizerTo(this.sizers[2], pt.x, pt.y);
            this.sizers[2].setCursor(crs[mod(2 + da, crs.length)]);
            pt.x = s.x;
            pt.y = cy;
            pt = getRotatedPoint(pt, cos, sin, ct);
            this.moveSizerTo(this.sizers[3], pt.x, pt.y);
            this.sizers[3].setCursor(crs[mod(7 + da, crs.length)]);
            pt.x = r;
            pt.y = cy;
            pt = getRotatedPoint(pt, cos, sin, ct);
            this.moveSizerTo(this.sizers[4], pt.x, pt.y);
            this.sizers[4].setCursor(crs[mod(3 + da, crs.length)]);
            pt.x = s.x;
            pt.y = b;
            pt = getRotatedPoint(pt, cos, sin, ct);
            this.moveSizerTo(this.sizers[5], pt.x, pt.y);
            this.sizers[5].setCursor(crs[mod(6 + da, crs.length)]);
            pt.x = cx;
            pt.y = b;
            pt = getRotatedPoint(pt, cos, sin, ct);
            this.moveSizerTo(this.sizers[6], pt.x, pt.y);
            this.sizers[6].setCursor(crs[mod(5 + da, crs.length)]);
            pt.x = r;
            pt.y = b;
            pt = getRotatedPoint(pt, cos, sin, ct);
            this.moveSizerTo(this.sizers[7], pt.x, pt.y);
            this.sizers[7].setCursor(crs[mod(4 + da, crs.length)]);
            pt.x = cx + this.state.absoluteOffset.x;
            pt.y = cy + this.state.absoluteOffset.y;
            pt = getRotatedPoint(pt, cos, sin, ct);
            this.moveSizerTo(this.sizers[8], pt.x, pt.y);
          } else if (this.state.width >= 2 && this.state.height >= 2) {
            this.moveSizerTo(this.sizers[0], cx + this.state.absoluteOffset.x, cy + this.state.absoluteOffset.y);
          } else {
            this.moveSizerTo(this.sizers[0], this.state.x, this.state.y);
          }
        }
      }
      if (this.rotationShape) {
        const alpha = toRadians((_c = (_b = this.currentAlpha) != null ? _b : this.state.style.rotation) != null ? _c : 0);
        const cos = Math.cos(alpha);
        const sin = Math.sin(alpha);
        const ct = new Point_default(this.state.getCenterX(), this.state.getCenterY());
        const pt = getRotatedPoint(this.getRotationHandlePosition(), cos, sin, ct);
        if (this.rotationShape.node != null) {
          this.moveSizerTo(this.rotationShape, pt.x, pt.y);
          this.rotationShape.node.style.visibility = this.state.view.graph.isEditing() || !this.handlesVisible ? "hidden" : "";
        }
      }
      if (this.selectionBorder != null) {
        this.selectionBorder.rotation = (_d = this.state.style.rotation) != null ? _d : 0;
      }
      if (this.edgeHandlers != null) {
        for (let i = 0; i < this.edgeHandlers.length; i += 1) {
          this.edgeHandlers[i].redraw();
        }
      }
    }
    /**
     * Returns true if the given custom handle is visible.
     */
    isCustomHandleVisible(handle) {
      return !this.graph.isEditing() && this.state.view.graph.getSelectionCount() === 1;
    }
    /**
     * Returns an {@link Point} that defines the rotation handle position.
     */
    getRotationHandlePosition() {
      return new Point_default(this.bounds.x + this.bounds.width / 2, this.bounds.y + this.rotationHandleVSpacing);
    }
    /**
     * Returns `true` if the parent highlight should be visible.
     *
     * This implementation always returns `true`.
     */
    isParentHighlightVisible() {
      const parent = this.state.cell.getParent();
      return parent ? !this.graph.isCellSelected(parent) : false;
    }
    /**
     * Updates the highlight of the parent if {@link parentHighlightEnabled} is `true`.
     */
    updateParentHighlight() {
      var _a2;
      if (!this.isDestroyed()) {
        const visible = this.isParentHighlightVisible();
        const parent = this.state.cell.getParent();
        const pstate = parent ? this.graph.view.getState(parent) : null;
        if (this.parentHighlight) {
          if (parent && parent.isVertex() && visible) {
            const b = this.parentHighlight.bounds;
            if (pstate && b && (b.x !== pstate.x || b.y !== pstate.y || b.width !== pstate.width || b.height !== pstate.height)) {
              this.parentHighlight.bounds = Rectangle_default.fromRectangle(pstate);
              this.parentHighlight.redraw();
            }
          } else {
            if (pstate != null && pstate.parentHighlight === this.parentHighlight) {
              pstate.parentHighlight = null;
            }
            this.parentHighlight.destroy();
            this.parentHighlight = null;
          }
        } else if (this.parentHighlightEnabled && visible) {
          if (parent && parent.isVertex() && pstate != null && pstate.parentHighlight == null) {
            this.parentHighlight = this.createParentHighlightShape(pstate);
            this.parentHighlight.dialect = "svg";
            this.parentHighlight.pointerEvents = false;
            this.parentHighlight.rotation = (_a2 = pstate.style.rotation) != null ? _a2 : 0;
            this.parentHighlight.init(this.graph.getView().getOverlayPane());
            this.parentHighlight.redraw();
            pstate.parentHighlight = this.parentHighlight;
          }
        }
      }
    }
    /**
     * Redraws the preview.
     */
    drawPreview() {
      var _a2;
      if (this.preview != null) {
        this.preview.bounds = this.bounds;
        if (this.preview.node.parentNode === this.graph.container) {
          this.preview.bounds.width = Math.max(0, this.preview.bounds.width - 1);
          this.preview.bounds.height = Math.max(0, this.preview.bounds.height - 1);
        }
        this.preview.rotation = (_a2 = this.state.style.rotation) != null ? _a2 : 0;
        this.preview.redraw();
      }
      this.selectionBorder.bounds = this.getSelectionBorderBounds();
      this.selectionBorder.redraw();
      this.updateParentHighlight();
    }
    /**
     * Returns the bounds for the selection border.
     */
    getSelectionBorderBounds() {
      return this.bounds;
    }
    /**
     * Returns `true` if this handler was destroyed or not initialized.
     */
    isDestroyed() {
      return this.selectionBorder == null;
    }
    /**
     * Destroys the handler and all its resources and DOM nodes.
     */
    onDestroy() {
      this.state.view.graph.removeListener(this.escapeHandler);
      this.escapeHandler = () => {
        return;
      };
      if (this.preview) {
        this.preview.destroy();
        this.preview = null;
      }
      if (this.parentHighlight) {
        const parent = this.state.cell.getParent();
        const pstate = parent ? this.graph.view.getState(parent) : null;
        if (pstate && pstate.parentHighlight === this.parentHighlight) {
          pstate.parentHighlight = null;
        }
        this.parentHighlight.destroy();
        this.parentHighlight = null;
      }
      if (this.ghostPreview) {
        this.ghostPreview.destroy();
        this.ghostPreview = null;
      }
      if (this.selectionBorder) {
        this.selectionBorder.destroy();
      }
      this.labelShape = null;
      this.removeHint();
      for (let i = 0; i < this.sizers.length; i += 1) {
        this.sizers[i].destroy();
      }
      this.sizers = [];
      for (let i = 0; i < this.customHandles.length; i += 1) {
        this.customHandles[i].destroy();
      }
      this.customHandles = [];
    }
  };
  var VertexHandler_default = VertexHandler;

  // node_modules/@maxgraph/core/lib/esm/internal/i18n-utils.js
  function isI18nEnabled() {
    return GlobalConfig.i18n.isEnabled();
  }
  function translate(key, params, defaultValue) {
    return GlobalConfig.i18n.get(key, params, defaultValue);
  }

  // node_modules/@maxgraph/core/lib/esm/view/handler/ElbowEdgeHandler.js
  var ElbowEdgeHandler = class extends EdgeHandler_default {
    constructor(state) {
      super(state);
      this.flipEnabled = true;
      this.doubleClickOrientationResource = isI18nEnabled() ? "doubleClickOrientation" : "";
    }
    /**
     * Overrides {@link EdgeHandler.createBends} to create custom bends.
     */
    createBends() {
      const bends = [];
      let bend = this.createHandleShape(0);
      this.initBend(bend);
      bend.setCursor(EdgeHandlerConfig.cursorTerminal);
      bends.push(bend);
      bends.push(this.createVirtualBend((evt) => {
        if (!isConsumed(evt) && this.flipEnabled) {
          this.graph.flipEdge(this.state.cell);
          InternalEvent_default.consume(evt);
        }
      }));
      this.points.push(new Point_default(0, 0));
      bend = this.createHandleShape(2);
      this.initBend(bend);
      bend.setCursor(EdgeHandlerConfig.cursorTerminal);
      bends.push(bend);
      return bends;
    }
    /**
     * Creates a virtual bend that supports double-clicking and calls {@link AbstractGraph.flipEdge}.
     */
    createVirtualBend(dblClickHandler) {
      const bend = this.createHandleShape();
      this.initBend(bend, dblClickHandler);
      bend.setCursor(this.getCursorForBend());
      if (!this.graph.isCellBendable(this.state.cell)) {
        bend.node.style.display = "none";
      }
      return bend;
    }
    /**
     * Returns the cursor to be used for the bend.
     */
    getCursorForBend() {
      return this.state.style.edgeStyle === "topToBottomEdgeStyle" || this.state.style.edgeStyle === "elbowEdgeStyle" && this.state.style.elbow === "vertical" ? "row-resize" : "col-resize";
    }
    /**
     * Returns the tooltip for the given node.
     */
    getTooltipForNode(node) {
      let tip = null;
      if (this.bends != null && this.bends[1] != null && (node === this.bends[1].node || node.parentNode === this.bends[1].node)) {
        tip = this.doubleClickOrientationResource;
        tip = translate(tip) || tip;
      }
      return tip;
    }
    /**
     * Converts the given point in-place from screen to unscaled, untranslated
     * graph coordinates and applies the grid.
     *
     * @param point {@link Point} to be converted.
     * @param gridEnabled Boolean that specifies if the grid should be applied.
     */
    convertPoint(point, gridEnabled) {
      const scale = this.graph.getView().getScale();
      const tr = this.graph.getView().getTranslate();
      const { origin } = this.state;
      if (gridEnabled) {
        point.x = this.graph.snap(point.x);
        point.y = this.graph.snap(point.y);
      }
      point.x = Math.round(point.x / scale - tr.x - origin.x);
      point.y = Math.round(point.y / scale - tr.y - origin.y);
      return point;
    }
    /**
     * Updates and redraws the inner bends.
     *
     * @param p0 {@link Point} that represents the location of the first point.
     * @param pe {@link Point} that represents the location of the last point.
     */
    redrawInnerBends(p0, pe) {
      const g = this.state.cell.getGeometry();
      const pts = this.state.absolutePoints;
      let pt = null;
      if (pts.length > 1) {
        p0 = pts[1];
        pe = pts[pts.length - 2];
      } else if (g.points != null && g.points.length > 0) {
        pt = pts[0];
      }
      if (pt == null) {
        pt = new Point_default(p0.x + (pe.x - p0.x) / 2, p0.y + (pe.y - p0.y) / 2);
      } else {
        pt = new Point_default(this.graph.getView().scale * (pt.x + this.graph.getView().translate.x + this.state.origin.x), this.graph.getView().scale * (pt.y + this.graph.getView().translate.y + this.state.origin.y));
      }
      const b = this.bends[1].bounds;
      let w = b.width;
      let h = b.height;
      let bounds = new Rectangle_default(Math.round(pt.x - w / 2), Math.round(pt.y - h / 2), w, h);
      if (this.manageLabelHandle) {
        this.checkLabelHandle(bounds);
      } else if (this.handleImage == null && this.labelShape.visible && this.labelShape.bounds && intersects(bounds, this.labelShape.bounds)) {
        w = HandleConfig.size + 3;
        h = w;
        bounds = new Rectangle_default(Math.floor(pt.x - w / 2), Math.floor(pt.y - h / 2), w, h);
      }
      this.bends[1].bounds = bounds;
      this.bends[1].redraw();
      if (this.manageLabelHandle) {
        this.checkLabelHandle(this.bends[1].bounds);
      }
    }
  };
  var ElbowEdgeHandler_default = ElbowEdgeHandler;

  // node_modules/@maxgraph/core/lib/esm/view/handler/EdgeSegmentHandler.js
  var EdgeSegmentHandler = class extends ElbowEdgeHandler_default {
    constructor(state) {
      super(state);
      this.points = [];
    }
    /**
     * Returns the current absolute points.
     */
    getCurrentPoints() {
      let pts = this.state.absolutePoints;
      const tol = Math.max(1, this.graph.view.scale);
      if (pts.length === 2 && pts[0] && pts[1] || pts.length === 3 && pts[0] && pts[1] && pts[2] && (Math.abs(pts[0].x - pts[1].x) < tol && Math.abs(pts[1].x - pts[2].x) < tol || Math.abs(pts[0].y - pts[1].y) < tol && Math.abs(pts[1].y - pts[2].y) < tol)) {
        const cx = pts[0].x + (pts[pts.length - 1].x - pts[0].x) / 2;
        const cy = pts[0].y + (pts[pts.length - 1].y - pts[0].y) / 2;
        pts = [pts[0], new Point_default(cx, cy), new Point_default(cx, cy), pts[pts.length - 1]];
      }
      return pts;
    }
    /**
     * Updates the given preview state taking into account the state of the constraint handler.
     */
    getPreviewPoints(point) {
      if (this.isSource || this.isTarget) {
        return super.getPreviewPoints(point);
      }
      const pts = this.getCurrentPoints();
      let last = this.convertPoint(pts[0].clone(), false);
      point = this.convertPoint(point.clone(), false);
      let result = [];
      for (let i = 1; i < pts.length; i += 1) {
        const pt = this.convertPoint(pts[i].clone(), false);
        if (i === this.index) {
          if (Math.round(last.x - pt.x) === 0) {
            last.x = point.x;
            pt.x = point.x;
          }
          if (Math.round(last.y - pt.y) === 0) {
            last.y = point.y;
            pt.y = point.y;
          }
        }
        if (i < pts.length - 1) {
          result.push(pt);
        }
        last = pt;
      }
      if (result.length === 1) {
        const source = this.state.getVisibleTerminalState(true);
        const target = this.state.getVisibleTerminalState(false);
        const scale = this.state.view.getScale();
        const tr = this.state.view.getTranslate();
        const x = result[0].x * scale + tr.x;
        const y = result[0].y * scale + tr.y;
        if (source != null && contains(source, x, y) || target != null && contains(target, x, y)) {
          result = [point, point];
        }
      }
      return result;
    }
    /**
     * Overridden to perform optimization of the edge style result.
     */
    updatePreviewState(edge, point, terminalState, me) {
      super.updatePreviewState(edge, point, terminalState, me);
      if (!this.isSource && !this.isTarget) {
        point = this.convertPoint(point.clone(), false);
        const pts = edge.absolutePoints;
        let pt0 = pts[0];
        let pt1 = pts[1];
        let result = [];
        for (let i = 2; i < pts.length; i += 1) {
          const pt2 = pts[i];
          if ((Math.round(pt0.x - pt1.x) !== 0 || Math.round(pt1.x - pt2.x) !== 0) && (Math.round(pt0.y - pt1.y) !== 0 || Math.round(pt1.y - pt2.y) !== 0)) {
            result.push(this.convertPoint(pt1.clone(), false));
          }
          pt0 = pt1;
          pt1 = pt2;
        }
        const source = this.state.getVisibleTerminalState(true);
        const target = this.state.getVisibleTerminalState(false);
        const rpts = this.state.absolutePoints;
        const end = pts[pts.length - 1];
        if (result.length === 0 && pts[0] && end && (Math.round(pts[0].x - end.x) === 0 || Math.round(pts[0].y - end.y) === 0)) {
          result = [point, point];
        } else if (pts.length === 5 && result.length === 2 && source != null && target != null && rpts != null && Math.round(rpts[0].x - rpts[rpts.length - 1].x) === 0) {
          const view = this.graph.getView();
          const scale = view.getScale();
          const tr = view.getTranslate();
          let y0 = view.getRoutingCenterY(source) / scale - tr.y;
          const sc = this.graph.getConnectionConstraint(edge, source, true);
          if (sc != null) {
            const pt = this.graph.getConnectionPoint(source, sc);
            if (pt != null) {
              this.convertPoint(pt, false);
              y0 = pt.y;
            }
          }
          let ye = view.getRoutingCenterY(target) / scale - tr.y;
          const tc = this.graph.getConnectionConstraint(edge, target, false);
          if (tc) {
            const pt = this.graph.getConnectionPoint(target, tc);
            if (pt != null) {
              this.convertPoint(pt, false);
              ye = pt.y;
            }
          }
          result = [new Point_default(point.x, y0), new Point_default(point.x, ye)];
        }
        this.points = result;
        edge.view.updateFixedTerminalPoints(edge, source, target);
        edge.view.updatePoints(edge, this.points, source, target);
        edge.view.updateFloatingTerminalPoints(edge, source, target);
      }
    }
    /**
     * Overriden to merge edge segments.
     */
    connect(edge, terminal, isSource, isClone, me) {
      const model = this.graph.getDataModel();
      let geo = edge.getGeometry();
      let result = null;
      if (geo != null && geo.points != null && geo.points.length > 0) {
        const pts = this.abspoints;
        let pt0 = pts[0];
        let pt1 = pts[1];
        result = [];
        for (let i = 2; i < pts.length; i += 1) {
          const pt2 = pts[i];
          if (pt0 && pt1 && pt2 && (Math.round(pt0.x - pt1.x) !== 0 || Math.round(pt1.x - pt2.x) !== 0) && (Math.round(pt0.y - pt1.y) !== 0 || Math.round(pt1.y - pt2.y) !== 0)) {
            result.push(this.convertPoint(pt1.clone(), false));
          }
          pt0 = pt1;
          pt1 = pt2;
        }
      }
      this.graph.batchUpdate(() => {
        if (result != null) {
          geo = edge.getGeometry();
          if (geo != null) {
            geo = geo.clone();
            geo.points = result;
            model.setGeometry(edge, geo);
          }
        }
        edge = super.connect(edge, terminal, isSource, isClone, me);
      });
      return edge;
    }
    /**
     * Returns no tooltips.
     */
    getTooltipForNode(node) {
      return null;
    }
    /**
     * Adds custom bends for the center of each segment.
     */
    start(x, y, index) {
      super.start(x, y, index);
      if (this.bends != null && this.bends[index] != null && !this.isSource && !this.isTarget) {
        setOpacity(this.bends[index].node, 100);
      }
    }
    /**
     * Adds custom bends for the center of each segment.
     */
    createBends() {
      const bends = [];
      let bend = this.createHandleShape(0);
      this.initBend(bend);
      bend.setCursor(EdgeHandlerConfig.cursorTerminal);
      bends.push(bend);
      const pts = this.getCurrentPoints();
      if (this.graph.isCellBendable(this.state.cell)) {
        if (this.points == null) {
          this.points = [];
        }
        for (let i = 0; i < pts.length - 1; i += 1) {
          bend = this.createVirtualBend();
          bends.push(bend);
          let horizontal = Math.round(pts[i].x - pts[i + 1].x) === 0;
          if (Math.round(pts[i].y - pts[i + 1].y) === 0 && i < pts.length - 2) {
            horizontal = Math.round(pts[i].x - pts[i + 2].x) === 0;
          }
          bend.setCursor(horizontal ? "col-resize" : "row-resize");
          this.points.push(new Point_default(0, 0));
        }
      }
      bend = this.createHandleShape(pts.length);
      this.initBend(bend);
      bend.setCursor(EdgeHandlerConfig.cursorTerminal);
      bends.push(bend);
      return bends;
    }
    /**
     * Overridden to invoke <refresh> before the redraw.
     */
    redraw() {
      this.refresh();
      super.redraw();
    }
    /**
     * Updates the position of the custom bends.
     */
    redrawInnerBends(p0, pe) {
      if (this.graph.isCellBendable(this.state.cell)) {
        const pts = this.getCurrentPoints();
        if (pts != null && pts.length > 1) {
          let straight = false;
          if (pts.length === 4 && pts[0] && pts[1] && pts[2] && pts[3] && Math.round(pts[1].x - pts[2].x) === 0 && Math.round(pts[1].y - pts[2].y) === 0) {
            straight = true;
            if (Math.round(pts[0].y - pts[pts.length - 1].y) === 0) {
              const cx = pts[0].x + (pts[pts.length - 1].x - pts[0].x) / 2;
              pts[1] = new Point_default(cx, pts[1].y);
              pts[2] = new Point_default(cx, pts[2].y);
            } else {
              const cy = pts[0].y + (pts[pts.length - 1].y - pts[0].y) / 2;
              pts[1] = new Point_default(pts[1].x, cy);
              pts[2] = new Point_default(pts[2].x, cy);
            }
          }
          for (let i = 0; i < pts.length - 1; i += 1) {
            if (this.bends[i + 1] != null) {
              p0 = pts[i];
              pe = pts[i + 1];
              const pt = new Point_default(p0.x + (pe.x - p0.x) / 2, p0.y + (pe.y - p0.y) / 2);
              const b = this.bends[i + 1].bounds;
              this.bends[i + 1].bounds = new Rectangle_default(Math.floor(pt.x - b.width / 2), Math.floor(pt.y - b.height / 2), b.width, b.height);
              this.bends[i + 1].redraw();
              if (this.manageLabelHandle) {
                this.checkLabelHandle(this.bends[i + 1].bounds);
              }
            }
          }
          if (straight) {
            setOpacity(this.bends[1].node, EdgeHandlerConfig.virtualBendOpacity);
            setOpacity(this.bends[3].node, EdgeHandlerConfig.virtualBendOpacity);
          }
        }
      }
    }
  };
  var EdgeSegmentHandler_default = EdgeSegmentHandler;

  // node_modules/@maxgraph/core/lib/esm/util/cellArrayUtils.js
  var getTopmostCells = (cells) => {
    const coveredEntries = /* @__PURE__ */ new Map();
    const tmp = [];
    for (let i = 0; i < cells.length; i += 1) {
      coveredEntries.set(cells[i], true);
    }
    for (let i = 0; i < cells.length; i += 1) {
      const cell = cells[i];
      let topmost = true;
      let parent = cell.getParent();
      while (parent != null) {
        if (coveredEntries.get(parent)) {
          topmost = false;
          break;
        }
        parent = parent.getParent();
      }
      if (topmost) {
        tmp.push(cell);
      }
    }
    return tmp;
  };
  var cloneCells = (cells, includeChildren = true, mapping = {}) => {
    const clones = [];
    for (const cell of cells) {
      clones.push(cloneCellImpl(cell, mapping, includeChildren));
    }
    for (let i = 0; i < clones.length; i += 1) {
      if (clones[i] != null) {
        restoreClone(clones[i], cells[i], mapping);
      }
    }
    return clones;
  };
  var cloneCellImpl = (cell, mapping = {}, includeChildren = false) => {
    const identity = ObjectIdentity_default.get(cell);
    let clone2 = mapping ? mapping[identity] : null;
    if (clone2 == null) {
      clone2 = cell.clone();
      mapping[identity] = clone2;
      if (includeChildren) {
        const childCount = cell.getChildCount();
        for (let i = 0; i < childCount; i += 1) {
          const cloneChild = cloneCellImpl(cell.getChildAt(i), mapping, true);
          clone2.insert(cloneChild);
        }
      }
    }
    return clone2;
  };
  var restoreClone = (clone2, cell, mapping) => {
    const source = cell.getTerminal(true);
    if (source != null) {
      const tmp = mapping[ObjectIdentity_default.get(source)];
      if (tmp != null) {
        tmp.insertEdge(clone2, true);
      }
    }
    const target = cell.getTerminal(false);
    if (target != null) {
      const tmp = mapping[ObjectIdentity_default.get(target)];
      if (tmp != null) {
        tmp.insertEdge(clone2, false);
      }
    }
    const childCount = clone2.getChildCount();
    for (let i = 0; i < childCount; i += 1) {
      restoreClone(clone2.getChildAt(i), cell.getChildAt(i), mapping);
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/mixin/CellsMixin.js
  var CellsMixin = {
    cellsResizable: true,
    cellsBendable: true,
    cellsSelectable: true,
    cellsDisconnectable: true,
    autoSizeCells: false,
    autoSizeCellsOnAdd: false,
    cellsLocked: false,
    cellsCloneable: true,
    cellsDeletable: true,
    cellsMovable: true,
    extendParents: true,
    extendParentsOnAdd: true,
    extendParentsOnMove: false,
    getBoundingBox(cells) {
      let result = null;
      if (cells.length > 0) {
        for (const cell of cells) {
          if (cell.isVertex() || cell.isEdge()) {
            const bbox = this.getView().getBoundingBox(this.getView().getState(cell), true);
            if (bbox) {
              if (!result) {
                result = Rectangle_default.fromRectangle(bbox);
              } else {
                result.add(bbox);
              }
            }
          }
        }
      }
      return result;
    },
    removeStateForCell(cell) {
      for (const child of cell.getChildren()) {
        this.removeStateForCell(child);
      }
      this.getView().invalidate(cell, false, true);
      this.getView().removeState(cell);
    },
    /*****************************************************************************
     * Group: Cell styles
     *****************************************************************************/
    getCurrentCellStyle(cell, ignoreState = false) {
      const state = ignoreState ? null : this.getView().getState(cell);
      return state ? state.style : this.getCellStyle(cell);
    },
    getCellStyle(cell) {
      const cellStyle = cell.getStyle();
      const stylesheet = this.getStylesheet();
      const defaultStyle = cell.isEdge() ? stylesheet.getDefaultEdgeStyle() : stylesheet.getDefaultVertexStyle();
      const style = this.postProcessCellStyle(stylesheet.getCellStyle(cellStyle, defaultStyle != null ? defaultStyle : {}));
      return style;
    },
    postProcessCellStyle(style) {
      var _a2, _b;
      if (!style.image) {
        return style;
      }
      const key = style.image;
      let image = (_b = (_a2 = this.getPlugin("image-bundle")) == null ? void 0 : _a2.getImageFromBundles(key)) != null ? _b : null;
      if (image) {
        style.image = image;
      } else {
        image = key;
      }
      if (image && image.substring(0, 11) === "data:image/") {
        if (image.substring(0, 20) === "data:image/svg+xml,<") {
          image = image.substring(0, 19) + encodeURIComponent(image.substring(19));
        } else if (image.substring(0, 22) !== "data:image/svg+xml,%3C") {
          const comma = image.indexOf(",");
          if (comma > 0 && image.substring(comma - 7, comma + 1) !== ";base64,") {
            image = `${image.substring(0, comma)};base64,${image.substring(comma + 1)}`;
          }
        }
        style.image = image;
      }
      return style;
    },
    setCellStyle(style, cells) {
      cells = cells != null ? cells : this.getSelectionCells();
      this.batchUpdate(() => {
        for (const cell of cells) {
          this.getDataModel().setStyle(cell, style);
        }
      });
    },
    toggleCellStyle(key, defaultValue = false, cell) {
      cell = cell != null ? cell : this.getSelectionCell();
      return this.toggleCellStyles(key, defaultValue, [cell]);
    },
    toggleCellStyles(key, defaultValue = false, cells) {
      var _a2;
      let value = false;
      cells = cells != null ? cells : this.getSelectionCells();
      if (cells.length > 0) {
        const style = this.getCurrentCellStyle(cells[0]);
        value = !((_a2 = style[key]) != null ? _a2 : defaultValue);
        this.setCellStyles(key, value, cells);
      }
      return value;
    },
    setCellStyles(key, value, cells) {
      cells = cells != null ? cells : this.getSelectionCells();
      setCellStyles(this.getDataModel(), cells, key, value);
    },
    toggleCellStyleFlags(key, flag, cells) {
      cells = cells != null ? cells : this.getSelectionCells();
      this.setCellStyleFlags(key, flag, null, cells);
    },
    setCellStyleFlags(key, flag, value = null, cells) {
      cells = cells != null ? cells : this.getSelectionCells();
      if (cells.length > 0) {
        if (value === null) {
          const style = this.getCurrentCellStyle(cells[0]);
          const current = style[key] || 0;
          value = !((current & flag) === flag);
        }
        setCellStyleFlags(this.getDataModel(), cells, key, flag, value);
      }
    },
    /*****************************************************************************
     * Group: Cell alignment and orientation
     *****************************************************************************/
    alignCells(align, cells, param = null) {
      cells = cells != null ? cells : this.getSelectionCells();
      if (cells.length > 1) {
        if (param === null) {
          for (const cell of cells) {
            const state = this.getView().getState(cell);
            if (state && !cell.isEdge()) {
              if (param === null) {
                switch (align) {
                  case "center":
                    param = state.x + state.width / 2;
                    break;
                  case "right":
                    param = state.x + state.width;
                    break;
                  case "top":
                    param = state.y;
                    break;
                  case "middle":
                    param = state.y + state.height / 2;
                    break;
                  case "bottom":
                    param = state.y + state.height;
                    break;
                  default:
                    param = state.x;
                    break;
                }
              } else
                switch (align) {
                  case "right": {
                    param = Math.max(param, state.x + state.width);
                    break;
                  }
                  case "top": {
                    param = Math.min(param, state.y);
                    break;
                  }
                  case "bottom": {
                    param = Math.max(param, state.y + state.height);
                    break;
                  }
                  default: {
                    param = Math.min(param, state.x);
                  }
                }
            }
          }
        }
        if (param !== null) {
          const s = this.getView().scale;
          this.batchUpdate(() => {
            const p = param;
            for (const cell of cells) {
              const state = this.getView().getState(cell);
              if (state != null) {
                let geo = cell.getGeometry();
                if (geo != null && !cell.isEdge()) {
                  geo = geo.clone();
                  switch (align) {
                    case "center": {
                      geo.x += (p - state.x - state.width / 2) / s;
                      break;
                    }
                    case "right": {
                      geo.x += (p - state.x - state.width) / s;
                      break;
                    }
                    case "top": {
                      geo.y += (p - state.y) / s;
                      break;
                    }
                    case "middle": {
                      geo.y += (p - state.y - state.height / 2) / s;
                      break;
                    }
                    case "bottom": {
                      geo.y += (p - state.y - state.height) / s;
                      break;
                    }
                    default: {
                      geo.x += (p - state.x) / s;
                    }
                  }
                  this.resizeCell(cell, geo);
                }
              }
            }
            this.fireEvent(new EventObject_default(InternalEvent_default.ALIGN_CELLS, { align, cells }));
          });
        }
      }
      return cells;
    },
    /*****************************************************************************
     * Group: Cell cloning, insertion and removal
     *****************************************************************************/
    cloneCell(cell, allowInvalidEdges = false, mapping = {}, keepPosition = false) {
      return this.cloneCells([cell], allowInvalidEdges, mapping, keepPosition)[0];
    },
    cloneCells(cells, allowInvalidEdges = true, mapping = {}, keepPosition = false) {
      let clones;
      const dict = /* @__PURE__ */ new Map();
      const tmp = [];
      for (const cell of cells) {
        dict.set(cell, true);
        tmp.push(cell);
      }
      if (tmp.length > 0) {
        const { scale } = this.getView();
        const trans = this.getView().translate;
        const out = [];
        clones = cloneCells(cells, true, mapping);
        for (let i = 0; i < cells.length; i += 1) {
          const cell = cells[i];
          const clone2 = clones[i];
          if (!allowInvalidEdges && clone2.isEdge() && this.getEdgeValidationError(clone2, clone2.getTerminal(true), clone2.getTerminal(false)) !== null) {
          } else {
            out.push(clone2);
            const g = clone2.getGeometry();
            if (g) {
              const state = this.getView().getState(cell);
              const parent = cell.getParent();
              const pstate = parent ? this.getView().getState(parent) : null;
              if (state && pstate) {
                const dx = keepPosition ? 0 : pstate.origin.x;
                const dy = keepPosition ? 0 : pstate.origin.y;
                if (clone2.isEdge()) {
                  const pts = state.absolutePoints;
                  let src = cell.getTerminal(true);
                  while (src && !dict.get(src)) {
                    src = src.getParent();
                  }
                  if (!src && pts[0]) {
                    g.setTerminalPoint(new Point_default(pts[0].x / scale - trans.x, pts[0].y / scale - trans.y), true);
                  }
                  let trg = cell.getTerminal(false);
                  while (trg && !dict.get(trg)) {
                    trg = trg.getParent();
                  }
                  const n = pts.length - 1;
                  const p = pts[n];
                  if (!trg && p) {
                    g.setTerminalPoint(new Point_default(p.x / scale - trans.x, p.y / scale - trans.y), false);
                  }
                  const { points } = g;
                  if (points) {
                    for (const point of points) {
                      point.x += dx;
                      point.y += dy;
                    }
                  }
                } else {
                  g.translate(dx, dy);
                }
              }
            }
          }
        }
        clones = out;
      } else {
        clones = [];
      }
      return clones;
    },
    addCell(cell, parent = null, index = null, source = null, target = null) {
      return this.addCells([cell], parent, index, source, target)[0];
    },
    addCells(cells, parent = null, index = null, source = null, target = null, absolute = false) {
      const p = parent != null ? parent : this.getDefaultParent();
      const i = index != null ? index : p.getChildCount();
      this.batchUpdate(() => {
        this.cellsAdded(cells, p, i, source, target, absolute, true);
        this.fireEvent(new EventObject_default(InternalEvent_default.ADD_CELLS, { cells, p, i, source, target }));
      });
      return cells;
    },
    cellsAdded(cells, parent, index, source = null, target = null, absolute = false, constrain = false, extend = true) {
      this.batchUpdate(() => {
        const parentState = absolute ? this.getView().getState(parent) : null;
        const o1 = parentState ? parentState.origin : null;
        const zero = new Point_default(0, 0);
        cells.forEach((cell, i) => {
          const previous = cell.getParent();
          if (o1 && cell !== parent && parent !== previous) {
            const oldState = previous ? this.getView().getState(previous) : null;
            const o2 = oldState ? oldState.origin : zero;
            let geo = cell.getGeometry();
            if (geo) {
              const dx = o2.x - o1.x;
              const dy = o2.y - o1.y;
              geo = geo.clone();
              geo.translate(dx, dy);
              if (!geo.relative && cell.isVertex() && !this.isAllowNegativeCoordinates()) {
                geo.x = Math.max(0, geo.x);
                geo.y = Math.max(0, geo.y);
              }
              this.getDataModel().setGeometry(cell, geo);
            }
          }
          if (parent === previous && index + i > parent.getChildCount()) {
            index--;
          }
          this.getDataModel().add(parent, cell, index + i);
          if (this.autoSizeCellsOnAdd) {
            this.autoSizeCell(cell, true);
          }
          if ((!extend || extend) && this.isExtendParentsOnAdd(cell) && this.isExtendParent(cell)) {
            this.extendParent(cell);
          }
          if (!constrain || constrain) {
            this.constrainChild(cell);
          }
          if (source) {
            this.cellConnected(cell, source, true);
          }
          if (target) {
            this.cellConnected(cell, target, false);
          }
        });
        this.fireEvent(new EventObject_default(InternalEvent_default.CELLS_ADDED, {
          cells,
          parent,
          index,
          source,
          target,
          absolute
        }));
      });
    },
    autoSizeCell(cell, recurse = true) {
      if (recurse) {
        for (const child of cell.getChildren()) {
          this.autoSizeCell(child);
        }
      }
      if (cell.isVertex() && this.isAutoSizeCell(cell)) {
        this.updateCellSize(cell);
      }
    },
    removeCells(cells = null, includeEdges = true) {
      if (!cells) {
        cells = this.getDeletableCells(this.getSelectionCells());
      }
      if (includeEdges) {
        cells = this.getDeletableCells(this.addAllEdges(cells));
      } else {
        cells = cells.slice();
        const edges = this.getDeletableCells(this.getAllEdges(cells));
        const dict = /* @__PURE__ */ new Map();
        for (const cell of cells) {
          dict.set(cell, true);
        }
        for (const edge of edges) {
          if (!this.getView().getState(edge) && !dict.get(edge)) {
            dict.set(edge, true);
            cells.push(edge);
          }
        }
      }
      this.batchUpdate(() => {
        this.cellsRemoved(cells);
        this.fireEvent(new EventObject_default(InternalEvent_default.REMOVE_CELLS, { cells, includeEdges }));
      });
      return cells != null ? cells : [];
    },
    cellsRemoved(cells) {
      if (cells.length > 0) {
        const { scale } = this.getView();
        const tr = this.getView().translate;
        this.batchUpdate(() => {
          const dict = /* @__PURE__ */ new Map();
          for (const cell of cells) {
            dict.set(cell, true);
          }
          for (const cell of cells) {
            const edges = this.getAllEdges([cell]);
            const disconnectTerminal = (edge, source) => {
              let geo = edge.getGeometry();
              if (geo) {
                const terminal = edge.getTerminal(source);
                let connected = false;
                let tmp = terminal;
                while (tmp) {
                  if (cell === tmp) {
                    connected = true;
                    break;
                  }
                  tmp = tmp.getParent();
                }
                if (connected) {
                  geo = geo.clone();
                  const state = this.getView().getState(edge);
                  if (state) {
                    const pts = state.absolutePoints;
                    const n = source ? 0 : pts.length - 1;
                    const p = pts[n];
                    geo.setTerminalPoint(new Point_default(p.x / scale - tr.x - state.origin.x, p.y / scale - tr.y - state.origin.y), source);
                  } else if (terminal) {
                    const tstate = this.getView().getState(terminal);
                    if (tstate) {
                      geo.setTerminalPoint(new Point_default(tstate.getCenterX() / scale - tr.x, tstate.getCenterY() / scale - tr.y), source);
                    }
                  }
                  this.getDataModel().setGeometry(edge, geo);
                  this.getDataModel().setTerminal(edge, null, source);
                }
              }
            };
            for (const edge of edges) {
              if (!dict.get(edge)) {
                dict.set(edge, true);
                disconnectTerminal(edge, true);
                disconnectTerminal(edge, false);
              }
            }
            this.getDataModel().remove(cell);
          }
          this.fireEvent(new EventObject_default(InternalEvent_default.CELLS_REMOVED, { cells }));
        });
      }
    },
    /*****************************************************************************
     * Group: Cell visibility
     *****************************************************************************/
    toggleCells(show = false, cells, includeEdges = true) {
      cells = cells != null ? cells : this.getSelectionCells();
      if (includeEdges) {
        cells = this.addAllEdges(cells);
      }
      this.batchUpdate(() => {
        this.cellsToggled(cells, show);
        this.fireEvent(new EventObject_default(InternalEvent_default.TOGGLE_CELLS, { show, cells, includeEdges }));
      });
      return cells;
    },
    cellsToggled(cells, show = false) {
      if (cells.length > 0) {
        this.batchUpdate(() => {
          for (const cell of cells) {
            this.getDataModel().setVisible(cell, show);
          }
        });
      }
    },
    /*****************************************************************************
     * Group: Cell sizing
     *****************************************************************************/
    updateCellSize(cell, ignoreChildren = false) {
      this.batchUpdate(() => {
        this.cellSizeUpdated(cell, ignoreChildren);
        this.fireEvent(new EventObject_default(InternalEvent_default.UPDATE_CELL_SIZE, { cell, ignoreChildren }));
      });
      return cell;
    },
    cellSizeUpdated(cell, ignoreChildren = false) {
      this.batchUpdate(() => {
        var _a2, _b;
        const size = this.getPreferredSizeForCell(cell);
        let geo = cell.getGeometry();
        if (size && geo) {
          const collapsed = cell.isCollapsed();
          geo = geo.clone();
          if (this.isSwimlane(cell)) {
            const style = this.getCellStyle(cell);
            const cellStyle = cell.getStyle();
            if ((_a2 = style.horizontal) != null ? _a2 : true) {
              cellStyle.startSize = size.height + 8;
              if (collapsed) {
                geo.height = size.height + 8;
              }
              geo.width = size.width;
            } else {
              cellStyle.startSize = size.width + 8;
              if (collapsed) {
                geo.width = size.width + 8;
              }
              geo.height = size.height;
            }
            this.getDataModel().setStyle(cell, cellStyle);
          } else {
            const state = this.getView().createState(cell);
            const align = (_b = state.style.align) != null ? _b : "center";
            if (align === "right") {
              geo.x += geo.width - size.width;
            } else if (align === "center") {
              geo.x += Math.round((geo.width - size.width) / 2);
            }
            const valign = state.getVerticalAlign();
            if (valign === "bottom") {
              geo.y += geo.height - size.height;
            } else if (valign === "middle") {
              geo.y += Math.round((geo.height - size.height) / 2);
            }
            geo.width = size.width;
            geo.height = size.height;
          }
          if (!ignoreChildren && !collapsed) {
            const bounds = this.getView().getBounds(cell.getChildren());
            if (bounds != null) {
              const tr = this.getView().translate;
              const { scale } = this.getView();
              const width = (bounds.x + bounds.width) / scale - geo.x - tr.x;
              const height = (bounds.y + bounds.height) / scale - geo.y - tr.y;
              geo.width = Math.max(geo.width, width);
              geo.height = Math.max(geo.height, height);
            }
          }
          this.cellsResized([cell], [geo], false);
        }
      });
    },
    getPreferredSizeForCell(cell, textWidth = null) {
      var _a2;
      let result = null;
      const state = this.getView().createState(cell);
      const { style } = state;
      if (!cell.isEdge()) {
        const fontSize = style.fontSize || StyleDefaultsConfig.fontSize;
        let dx = 0;
        let dy = 0;
        if (state.getImageSrc() || style.image) {
          if (style.shape === "label") {
            if (style.verticalAlign === "middle") {
              dx += style.imageWidth || StyleDefaultsConfig.imageSize;
            }
            if (style.align !== "center") {
              dy += style.imageHeight || StyleDefaultsConfig.imageSize;
            }
          }
        }
        dx += 2 * (style.spacing || 0);
        dx += style.spacingLeft || 0;
        dx += style.spacingRight || 0;
        dy += 2 * (style.spacing || 0);
        dy += style.spacingTop || 0;
        dy += style.spacingBottom || 0;
        const image = this.getFoldingImage(state);
        if (image) {
          dx += image.width + 8;
        }
        let value = this.getCellRenderer().getLabelValue(state);
        if (value && value.length > 0) {
          if (!this.isHtmlLabel(state.cell)) {
            value = htmlEntities(value, false);
          }
          value = value.replace(/\n/g, "<br>");
          const size = getSizeForString(value, fontSize, style.fontFamily, textWidth, style.fontStyle);
          let width = size.width + dx;
          let height = size.height + dy;
          if (!((_a2 = style.horizontal) != null ? _a2 : true)) {
            const tmp = height;
            height = width;
            width = tmp;
          }
          if (this.isGridEnabled()) {
            width = this.snap(width + this.getGridSize() / 2);
            height = this.snap(height + this.getGridSize() / 2);
          }
          result = new Rectangle_default(0, 0, width, height);
        } else {
          const gs2 = 4 * this.getGridSize();
          result = new Rectangle_default(0, 0, gs2, gs2);
        }
      }
      return result;
    },
    resizeCell(cell, bounds, recurse = false) {
      return this.resizeCells([cell], [bounds], recurse)[0];
    },
    resizeCells(cells, bounds, recurse) {
      recurse = recurse != null ? recurse : this.isRecursiveResize();
      this.batchUpdate(() => {
        const prev = this.cellsResized(cells, bounds, recurse);
        this.fireEvent(new EventObject_default(InternalEvent_default.RESIZE_CELLS, { cells, bounds, prev }));
      });
      return cells;
    },
    cellsResized(cells, bounds, recurse = false) {
      const prev = [];
      if (cells.length === bounds.length) {
        this.batchUpdate(() => {
          cells.forEach((cell, i) => {
            prev.push(this.cellResized(cell, bounds[i], false, recurse));
            if (this.isExtendParent(cell)) {
              this.extendParent(cell);
            }
            this.constrainChild(cell);
          });
          if (this.isResetEdgesOnResize()) {
            this.resetEdges(cells);
          }
          this.fireEvent(new EventObject_default(InternalEvent_default.CELLS_RESIZED, { cells, bounds, prev }));
        });
      }
      return prev;
    },
    cellResized(cell, bounds, ignoreRelative = false, recurse = false) {
      const prev = cell.getGeometry();
      if (prev && (prev.x !== bounds.x || prev.y !== bounds.y || prev.width !== bounds.width || prev.height !== bounds.height)) {
        const geo = prev.clone();
        if (!ignoreRelative && geo.relative) {
          const { offset } = geo;
          if (offset) {
            offset.x += bounds.x - geo.x;
            offset.y += bounds.y - geo.y;
          }
        } else {
          geo.x = bounds.x;
          geo.y = bounds.y;
        }
        geo.width = bounds.width;
        geo.height = bounds.height;
        if (!geo.relative && cell.isVertex() && !this.isAllowNegativeCoordinates()) {
          geo.x = Math.max(0, geo.x);
          geo.y = Math.max(0, geo.y);
        }
        this.batchUpdate(() => {
          if (recurse) {
            this.resizeChildCells(cell, geo);
          }
          this.getDataModel().setGeometry(cell, geo);
          this.constrainChildCells(cell);
        });
      }
      return prev;
    },
    resizeChildCells(cell, newGeo) {
      const geo = cell.getGeometry();
      if (geo) {
        const dx = geo.width !== 0 ? newGeo.width / geo.width : 1;
        const dy = geo.height !== 0 ? newGeo.height / geo.height : 1;
        for (const child of cell.getChildren()) {
          this.scaleCell(child, dx, dy, true);
        }
      }
    },
    constrainChildCells(cell) {
      for (const child of cell.getChildren()) {
        this.constrainChild(child);
      }
    },
    scaleCell(cell, dx, dy, recurse = false) {
      let geo = cell.getGeometry();
      if (geo) {
        const style = this.getCurrentCellStyle(cell);
        geo = geo.clone();
        const { x } = geo;
        const { y } = geo;
        const w = geo.width;
        const h = geo.height;
        geo.scale(dx, dy, style.aspect === "fixed");
        if (style.resizeWidth) {
          geo.width = w * dx;
        } else if (!style.resizeWidth) {
          geo.width = w;
        }
        if (style.resizeHeight) {
          geo.height = h * dy;
        } else if (!style.resizeHeight) {
          geo.height = h;
        }
        if (!this.isCellMovable(cell)) {
          geo.x = x;
          geo.y = y;
        }
        if (!this.isCellResizable(cell)) {
          geo.width = w;
          geo.height = h;
        }
        if (cell.isVertex()) {
          this.cellResized(cell, geo, true, recurse);
        } else {
          this.getDataModel().setGeometry(cell, geo);
        }
      }
    },
    extendParent(cell) {
      const parent = cell.getParent();
      let p = parent ? parent.getGeometry() : null;
      if (parent && p && !parent.isCollapsed()) {
        const geo = cell.getGeometry();
        if (geo && !geo.relative && (p.width < geo.x + geo.width || p.height < geo.y + geo.height)) {
          p = p.clone();
          p.width = Math.max(p.width, geo.x + geo.width);
          p.height = Math.max(p.height, geo.y + geo.height);
          this.cellsResized([parent], [p], false);
        }
      }
    },
    // *************************************************************************************
    // Group: Cell moving
    // *************************************************************************************
    importCells(cells, dx, dy, target = null, evt = null, mapping = {}) {
      return this.moveCells(cells, dx, dy, true, target, evt, mapping);
    },
    moveCells(cells, dx = 0, dy = 0, clone2 = false, target = null, evt = null, mapping = {}) {
      if (dx !== 0 || dy !== 0 || clone2 || target) {
        cells = getTopmostCells(cells);
        const origCells = cells;
        this.batchUpdate(() => {
          const dict = /* @__PURE__ */ new Map();
          for (const cell of cells) {
            dict.set(cell, true);
          }
          const isSelected = (cell) => {
            while (cell) {
              if (dict.get(cell)) {
                return true;
              }
              cell = cell.getParent();
            }
            return false;
          };
          const checked = [];
          for (const cell of cells) {
            const geo = cell.getGeometry();
            const parent = cell.getParent();
            if (!geo || !geo.relative || parent && !parent.isEdge() || parent && !isSelected(parent.getTerminal(true)) && !isSelected(parent.getTerminal(false))) {
              checked.push(cell);
            }
          }
          cells = checked;
          if (clone2) {
            cells = this.cloneCells(cells, this.isCloneInvalidEdges(), mapping);
            if (!target) {
              target = this.getDefaultParent();
            }
          }
          const previous = this.isAllowNegativeCoordinates();
          if (target) {
            this.setAllowNegativeCoordinates(true);
          }
          this.cellsMoved(cells, dx, dy, !clone2 && this.isDisconnectOnMove() && this.isAllowDanglingEdges(), !target, this.isExtendParentsOnMove() && !target);
          this.setAllowNegativeCoordinates(previous);
          if (target) {
            const index = target.getChildCount();
            this.cellsAdded(cells, target, index, null, null, true);
            if (clone2) {
              cells.forEach((cell, i) => {
                const geo = cell.getGeometry();
                const parent = origCells[i].getParent();
                if (geo && geo.relative && parent && parent.isEdge() && this.getDataModel().contains(parent)) {
                  this.getDataModel().add(parent, cell);
                }
              });
            }
          }
          this.fireEvent(new EventObject_default(InternalEvent_default.MOVE_CELLS, {
            cells,
            dx,
            dy,
            clone: clone2,
            target,
            event: evt
          }));
        });
      }
      return cells;
    },
    cellsMoved(cells, dx, dy, disconnect = false, constrain = false, extend = false) {
      if (dx !== 0 || dy !== 0) {
        this.batchUpdate(() => {
          if (disconnect) {
            this.disconnectGraph(cells);
          }
          for (const cell of cells) {
            this.translateCell(cell, dx, dy);
            if (extend && this.isExtendParent(cell)) {
              this.extendParent(cell);
            } else if (constrain) {
              this.constrainChild(cell);
            }
          }
          if (this.isResetEdgesOnMove()) {
            this.resetEdges(cells);
          }
          this.fireEvent(new EventObject_default(InternalEvent_default.CELLS_MOVED, { cells, dx, dy, disconnect }));
        });
      }
    },
    translateCell(cell, dx, dy) {
      var _a2;
      let geometry = cell.getGeometry();
      if (geometry) {
        geometry = geometry.clone();
        geometry.translate(dx, dy);
        if (!geometry.relative && cell.isVertex() && !this.isAllowNegativeCoordinates()) {
          geometry.x = Math.max(0, geometry.x);
          geometry.y = Math.max(0, geometry.y);
        }
        if (geometry.relative && !cell.isEdge()) {
          const parent = cell.getParent();
          let angle = 0;
          if (parent.isVertex()) {
            const style = this.getCurrentCellStyle(parent);
            angle = (_a2 = style.rotation) != null ? _a2 : 0;
          }
          if (angle !== 0) {
            const rad = toRadians(-angle);
            const cos = Math.cos(rad);
            const sin = Math.sin(rad);
            const pt = getRotatedPoint(new Point_default(dx, dy), cos, sin, new Point_default(0, 0));
            dx = pt.x;
            dy = pt.y;
          }
          if (!geometry.offset) {
            geometry.offset = new Point_default(dx, dy);
          } else {
            geometry.offset.x = geometry.offset.x + dx;
            geometry.offset.y = geometry.offset.y + dy;
          }
        }
        this.getDataModel().setGeometry(cell, geometry);
      }
    },
    getCellContainmentArea(cell) {
      var _a2, _b, _c;
      if (!cell.isEdge()) {
        const parent = cell.getParent();
        if (parent && parent !== this.getDefaultParent()) {
          const g = parent.getGeometry();
          if (g) {
            let x = 0;
            let y = 0;
            let w = g.width;
            let h = g.height;
            if (this.isSwimlane(parent)) {
              const size = this.getStartSize(parent);
              const style = this.getCurrentCellStyle(parent);
              const dir = (_a2 = style.direction) != null ? _a2 : "east";
              const flipH = (_b = style.flipH) != null ? _b : false;
              const flipV = (_c = style.flipV) != null ? _c : false;
              if (dir === "south" || dir === "north") {
                const tmp = size.width;
                size.width = size.height;
                size.height = tmp;
              }
              if (dir === "east" && !flipV || dir === "north" && !flipH || dir === "west" && flipV || dir === "south" && flipH) {
                x = size.width;
                y = size.height;
              }
              w -= size.width;
              h -= size.height;
            }
            return new Rectangle_default(x, y, w, h);
          }
        }
      }
      return null;
    },
    constrainChild(cell, sizeFirst = true) {
      let geo = cell.getGeometry();
      if (geo && (this.isConstrainRelativeChildren() || !geo.relative)) {
        const parent = cell.getParent();
        let max = this.getMaximumGraphBounds();
        if (max && parent) {
          const off = this.getBoundingBoxFromGeometry([parent], false);
          if (off) {
            max = Rectangle_default.fromRectangle(max);
            max.x -= off.x;
            max.y -= off.y;
          }
        }
        if (this.isConstrainChild(cell)) {
          let tmp = this.getCellContainmentArea(cell);
          if (tmp) {
            const overlap = this.getOverlap(cell);
            if (overlap > 0) {
              tmp = Rectangle_default.fromRectangle(tmp);
              tmp.x -= tmp.width * overlap;
              tmp.y -= tmp.height * overlap;
              tmp.width += 2 * tmp.width * overlap;
              tmp.height += 2 * tmp.height * overlap;
            }
            if (!max) {
              max = tmp;
            } else {
              max = Rectangle_default.fromRectangle(max);
              max.intersect(tmp);
            }
          }
        }
        if (max) {
          const cells = [cell];
          if (!cell.isCollapsed()) {
            const desc = cell.getDescendants();
            for (const descItem of desc) {
              if (descItem.isVisible()) {
                cells.push(descItem);
              }
            }
          }
          const bbox = this.getBoundingBoxFromGeometry(cells, false);
          if (bbox) {
            geo = geo.clone();
            let dx = 0;
            if (geo.width > max.width) {
              dx = geo.width - max.width;
              geo.width -= dx;
            }
            if (bbox.x + bbox.width > max.x + max.width) {
              dx -= bbox.x + bbox.width - max.x - max.width - dx;
            }
            let dy = 0;
            if (geo.height > max.height) {
              dy = geo.height - max.height;
              geo.height -= dy;
            }
            if (bbox.y + bbox.height > max.y + max.height) {
              dy -= bbox.y + bbox.height - max.y - max.height - dy;
            }
            if (bbox.x < max.x) {
              dx -= bbox.x - max.x;
            }
            if (bbox.y < max.y) {
              dy -= bbox.y - max.y;
            }
            if (dx !== 0 || dy !== 0) {
              if (geo.relative) {
                if (!geo.offset) {
                  geo.offset = new Point_default();
                }
                geo.offset.x += dx;
                geo.offset.y += dy;
              } else {
                geo.x += dx;
                geo.y += dy;
              }
            }
            this.getDataModel().setGeometry(cell, geo);
          }
        }
      }
    },
    /*****************************************************************************
     * Group: Cell retrieval
     *****************************************************************************/
    getChildCells(parent, vertices = false, edges = false) {
      parent = parent != null ? parent : this.getDefaultParent();
      const cells = parent.getChildCells(vertices, edges);
      const result = [];
      for (const cell of cells) {
        if (cell.isVisible()) {
          result.push(cell);
        }
      }
      return result;
    },
    getCellAt(x, y, parent = null, vertices = true, edges = true, ignoreFn = null) {
      if (!parent) {
        parent = this.getCurrentRoot();
        if (!parent) {
          parent = this.getDataModel().getRoot();
        }
      }
      if (parent) {
        const childCount = parent.getChildCount();
        for (let i = childCount - 1; i >= 0; i--) {
          const cell = parent.getChildAt(i);
          const result = this.getCellAt(x, y, cell, vertices, edges, ignoreFn);
          if (result) {
            return result;
          }
          if (cell.isVisible() && (edges && cell.isEdge() || vertices && cell.isVertex())) {
            const state = this.getView().getState(cell);
            if (state && (!ignoreFn || !ignoreFn(state, x, y)) && this.intersects(state, x, y)) {
              return cell;
            }
          }
        }
      }
      return null;
    },
    getCells(x, y, width, height, parent = null, result = [], intersection2 = null, ignoreFn = null, includeDescendants = false) {
      var _a2;
      if (width > 0 || height > 0 || intersection2) {
        const model = this.getDataModel();
        const right = x + width;
        const bottom = y + height;
        if (!parent) {
          parent = this.getCurrentRoot();
          if (!parent) {
            parent = model.getRoot();
          }
        }
        if (parent) {
          for (const cell of parent.getChildren()) {
            const state = this.getView().getState(cell);
            if (state && cell.isVisible() && (!ignoreFn || !ignoreFn(state))) {
              const deg = (_a2 = state.style.rotation) != null ? _a2 : 0;
              let box = state;
              if (deg !== 0) {
                box = getBoundingBox(box, deg);
              }
              const hit = intersection2 && cell.isVertex() && intersects(intersection2, box) || !intersection2 && (cell.isEdge() || cell.isVertex()) && box.x >= x && box.y + box.height <= bottom && box.y >= y && box.x + box.width <= right;
              if (hit) {
                result.push(cell);
              }
              if (!hit || includeDescendants) {
                this.getCells(x, y, width, height, cell, result, intersection2, ignoreFn, includeDescendants);
              }
            }
          }
        }
      }
      return result;
    },
    getCellsBeyond(x0, y0, parent = null, rightHalfpane = false, bottomHalfpane = false) {
      const result = [];
      if (rightHalfpane || bottomHalfpane) {
        if (!parent) {
          parent = this.getDefaultParent();
        }
        if (parent) {
          for (const child of parent.getChildren()) {
            const state = this.getView().getState(child);
            if (child.isVisible() && state) {
              if ((!rightHalfpane || state.x >= x0) && (!bottomHalfpane || state.y >= y0)) {
                result.push(child);
              }
            }
          }
        }
      }
      return result;
    },
    intersects(state, x, y) {
      var _a2;
      const pts = state.absolutePoints;
      if (pts.length > 0) {
        const t2 = this.getEventTolerance() * this.getEventTolerance();
        let pt = pts[0];
        for (let i = 1; i < pts.length; i += 1) {
          const next = pts[i];
          if (pt && next) {
            const dist = ptSegDistSq(pt.x, pt.y, next.x, next.y, x, y);
            if (dist <= t2) {
              return true;
            }
          }
          pt = next;
        }
      } else {
        const alpha = toRadians((_a2 = state.style.rotation) != null ? _a2 : 0);
        if (alpha !== 0) {
          const cos = Math.cos(-alpha);
          const sin = Math.sin(-alpha);
          const cx = new Point_default(state.getCenterX(), state.getCenterY());
          const pt = getRotatedPoint(new Point_default(x, y), cos, sin, cx);
          x = pt.x;
          y = pt.y;
        }
        if (contains(state, x, y)) {
          return true;
        }
      }
      return false;
    },
    isValidAncestor(cell, parent, recurse = false) {
      return recurse ? parent.isAncestor(cell) : (cell == null ? void 0 : cell.getParent()) === parent;
    },
    /*****************************************************************************
     * Group: Graph behaviour
     *****************************************************************************/
    isCellLocked(cell) {
      const geometry = cell.getGeometry();
      return this.isCellsLocked() || !!geometry && cell.isVertex() && geometry.relative;
    },
    isCellsLocked() {
      return this.cellsLocked;
    },
    setCellsLocked(value) {
      this.cellsLocked = value;
    },
    getCloneableCells(cells) {
      return this.getDataModel().filterCells(cells, (cell) => {
        return this.isCellCloneable(cell);
      });
    },
    isCellCloneable(cell) {
      var _a2;
      return this.isCellsCloneable() && ((_a2 = this.getCurrentCellStyle(cell).cloneable) != null ? _a2 : true);
    },
    isCellsCloneable() {
      return this.cellsCloneable;
    },
    setCellsCloneable(value) {
      this.cellsCloneable = value;
    },
    getExportableCells(cells) {
      return this.getDataModel().filterCells(cells, (cell) => {
        return this.canExportCell(cell);
      });
    },
    canExportCell(_cell = null) {
      return this.isExportEnabled();
    },
    getImportableCells(cells) {
      return this.getDataModel().filterCells(cells, (cell) => {
        return this.canImportCell(cell);
      });
    },
    canImportCell(cell = null) {
      return this.isImportEnabled();
    },
    isCellSelectable(_cell) {
      return this.isCellsSelectable();
    },
    isCellsSelectable() {
      return this.cellsSelectable;
    },
    setCellsSelectable(value) {
      this.cellsSelectable = value;
    },
    getDeletableCells(cells) {
      return this.getDataModel().filterCells(cells, (cell) => {
        return this.isCellDeletable(cell);
      });
    },
    isCellDeletable(cell) {
      var _a2;
      return this.isCellsDeletable() && ((_a2 = this.getCurrentCellStyle(cell).deletable) != null ? _a2 : true);
    },
    isCellsDeletable() {
      return this.cellsDeletable;
    },
    setCellsDeletable(value) {
      this.cellsDeletable = value;
    },
    isCellRotatable(cell) {
      var _a2;
      return (_a2 = this.getCurrentCellStyle(cell).rotatable) != null ? _a2 : true;
    },
    getMovableCells(cells) {
      return this.getDataModel().filterCells(cells, (cell) => {
        return this.isCellMovable(cell);
      });
    },
    isCellMovable(cell) {
      var _a2;
      return this.isCellsMovable() && !this.isCellLocked(cell) && ((_a2 = this.getCurrentCellStyle(cell).movable) != null ? _a2 : true);
    },
    isCellsMovable() {
      return this.cellsMovable;
    },
    setCellsMovable(value) {
      this.cellsMovable = value;
    },
    isCellResizable(cell) {
      var _a2;
      return this.isCellsResizable() && !this.isCellLocked(cell) && ((_a2 = this.getCurrentCellStyle(cell).resizable) != null ? _a2 : true);
    },
    isCellsResizable() {
      return this.cellsResizable;
    },
    setCellsResizable(value) {
      this.cellsResizable = value;
    },
    isCellBendable(cell) {
      var _a2;
      return this.isCellsBendable() && !this.isCellLocked(cell) && ((_a2 = this.getCurrentCellStyle(cell).bendable) != null ? _a2 : true);
    },
    isCellsBendable() {
      return this.cellsBendable;
    },
    setCellsBendable(value) {
      this.cellsBendable = value;
    },
    isAutoSizeCell(cell) {
      var _a2;
      return this.isAutoSizeCells() || ((_a2 = this.getCurrentCellStyle(cell).autoSize) != null ? _a2 : false);
    },
    isAutoSizeCells() {
      return this.autoSizeCells;
    },
    setAutoSizeCells(value) {
      this.autoSizeCells = value;
    },
    isExtendParent(cell) {
      return !cell.isEdge() && this.isExtendParents();
    },
    isExtendParents() {
      return this.extendParents;
    },
    setExtendParents(value) {
      this.extendParents = value;
    },
    isExtendParentsOnAdd(cell) {
      return this.extendParentsOnAdd;
    },
    setExtendParentsOnAdd(value) {
      this.extendParentsOnAdd = value;
    },
    isExtendParentsOnMove() {
      return this.extendParentsOnMove;
    },
    setExtendParentsOnMove(value) {
      this.extendParentsOnMove = value;
    },
    /*****************************************************************************
     * Group: Graph appearance
     *****************************************************************************/
    getCursorForCell(_cell) {
      return null;
    },
    /*****************************************************************************
     * Group: Graph display
     *****************************************************************************/
    getCellBounds(cell, includeEdges = false, includeDescendants = false) {
      let cells = [cell];
      if (includeEdges) {
        cells = cells.concat(cell.getEdges());
      }
      let result = this.getView().getBounds(cells);
      if (includeDescendants) {
        for (const child of cell.getChildren()) {
          const tmp = this.getCellBounds(child, includeEdges, true);
          if (result && tmp) {
            result.add(tmp);
          } else {
            result = tmp;
          }
        }
      }
      return result;
    },
    getBoundingBoxFromGeometry(cells, includeEdges = false) {
      var _a2;
      let result = null;
      let tmp = null;
      for (const cell of cells) {
        if (includeEdges || cell.isVertex()) {
          const geo = cell.getGeometry();
          if (geo) {
            let bbox = null;
            if (cell.isEdge()) {
              const addPoint = (pt) => {
                if (pt) {
                  if (!tmp) {
                    tmp = new Rectangle_default(pt.x, pt.y, 0, 0);
                  } else {
                    tmp.add(new Rectangle_default(pt.x, pt.y, 0, 0));
                  }
                }
              };
              if (!cell.getTerminal(true)) {
                addPoint(geo.getTerminalPoint(true));
              }
              if (!cell.getTerminal(false)) {
                addPoint(geo.getTerminalPoint(false));
              }
              const pts = geo.points;
              if (pts && pts.length > 0) {
                tmp = new Rectangle_default(pts[0].x, pts[0].y, 0, 0);
                for (let j = 1; j < pts.length; j++) {
                  addPoint(pts[j]);
                }
              }
              bbox = tmp;
            } else {
              const parent = cell.getParent();
              if (geo.relative && parent) {
                if (parent.isVertex() && parent !== this.getView().currentRoot) {
                  tmp = this.getBoundingBoxFromGeometry([parent], false);
                  if (tmp) {
                    bbox = new Rectangle_default(geo.x * tmp.width, geo.y * tmp.height, geo.width, geo.height);
                    if (cells.includes(parent)) {
                      bbox.x += tmp.x;
                      bbox.y += tmp.y;
                    }
                  }
                }
              } else {
                bbox = Rectangle_default.fromRectangle(geo);
                if (parent && parent.isVertex() && cells.includes(parent)) {
                  tmp = this.getBoundingBoxFromGeometry([parent], false);
                  if (tmp) {
                    bbox.x += tmp.x;
                    bbox.y += tmp.y;
                  }
                }
              }
              if (bbox && geo.offset) {
                bbox.x += geo.offset.x;
                bbox.y += geo.offset.y;
              }
              const style = this.getCurrentCellStyle(cell);
              if (bbox) {
                const angle = (_a2 = style.rotation) != null ? _a2 : 0;
                if (angle !== 0) {
                  bbox = getBoundingBox(bbox, angle);
                }
              }
            }
            if (bbox) {
              if (!result) {
                result = Rectangle_default.fromRectangle(bbox);
              } else {
                result.add(bbox);
              }
            }
          }
        }
      }
      return result;
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/mixin/ConnectionsMixin.js
  var ConnectionsMixin = {
    /*****************************************************************************
     * Group: Cell connecting and connection constraints
     *****************************************************************************/
    constrainChildren: true,
    constrainRelativeChildren: false,
    disconnectOnMove: true,
    cellsDisconnectable: true,
    getOutlineConstraint(point, terminalState, me) {
      if (terminalState.shape) {
        const bounds = this.getView().getPerimeterBounds(terminalState);
        const direction = terminalState.style.direction;
        if (direction === "north" || direction === "south") {
          bounds.x += bounds.width / 2 - bounds.height / 2;
          bounds.y += bounds.height / 2 - bounds.width / 2;
          const tmp = bounds.width;
          bounds.width = bounds.height;
          bounds.height = tmp;
        }
        const alpha = toRadians(terminalState.shape.getShapeRotation());
        if (alpha !== 0) {
          const cos = Math.cos(-alpha);
          const sin = Math.sin(-alpha);
          const ct = new Point_default(bounds.getCenterX(), bounds.getCenterY());
          point = getRotatedPoint(point, cos, sin, ct);
        }
        let sx = 1;
        let sy = 1;
        let dx = 0;
        let dy = 0;
        if (terminalState.cell.isVertex()) {
          let flipH = terminalState.style.flipH;
          let flipV = terminalState.style.flipV;
          if (direction === "north" || direction === "south") {
            const tmp = flipH;
            flipH = flipV;
            flipV = tmp;
          }
          if (flipH) {
            sx = -1;
            dx = -bounds.width;
          }
          if (flipV) {
            sy = -1;
            dy = -bounds.height;
          }
        }
        point = new Point_default((point.x - bounds.x) * sx - dx + bounds.x, (point.y - bounds.y) * sy - dy + bounds.y);
        const x = bounds.width === 0 ? 0 : Math.round((point.x - bounds.x) * 1e3 / bounds.width) / 1e3;
        const y = bounds.height === 0 ? 0 : Math.round((point.y - bounds.y) * 1e3 / bounds.height) / 1e3;
        return new ConnectionConstraint_default(new Point_default(x, y), false);
      }
      return null;
    },
    getAllConnectionConstraints(terminal, _source) {
      var _a2, _b, _c;
      return (_c = (_b = (_a2 = terminal == null ? void 0 : terminal.shape) == null ? void 0 : _a2.stencil) == null ? void 0 : _b.constraints) != null ? _c : null;
    },
    getConnectionConstraint(edge, terminal, source = false) {
      let point = null;
      const x = edge.style[source ? "exitX" : "entryX"];
      if (x !== void 0) {
        const y = edge.style[source ? "exitY" : "entryY"];
        if (y !== void 0) {
          point = new Point_default(x, y);
        }
      }
      let perimeter = false;
      let dx = 0;
      let dy = 0;
      if (point) {
        perimeter = edge.style[source ? "exitPerimeter" : "entryPerimeter"] || false;
        dx = edge.style[source ? "exitDx" : "entryDx"];
        dy = edge.style[source ? "exitDy" : "entryDy"];
        dx = Number.isFinite(dx) ? dx : 0;
        dy = Number.isFinite(dy) ? dy : 0;
      }
      return new ConnectionConstraint_default(point, perimeter, null, dx, dy);
    },
    setConnectionConstraint(edge, terminal, source = false, constraint = null) {
      if (constraint) {
        this.batchUpdate(() => {
          if (!constraint || !constraint.point) {
            this.setCellStyles(source ? "exitX" : "entryX", null, [edge]);
            this.setCellStyles(source ? "exitY" : "entryY", null, [edge]);
            this.setCellStyles(source ? "exitDx" : "entryDx", null, [edge]);
            this.setCellStyles(source ? "exitDy" : "entryDy", null, [edge]);
            this.setCellStyles(source ? "exitPerimeter" : "entryPerimeter", null, [edge]);
          } else if (constraint.point) {
            this.setCellStyles(source ? "exitX" : "entryX", constraint.point.x, [edge]);
            this.setCellStyles(source ? "exitY" : "entryY", constraint.point.y, [edge]);
            this.setCellStyles(source ? "exitDx" : "entryDx", constraint.dx, [edge]);
            this.setCellStyles(source ? "exitDy" : "entryDy", constraint.dy, [edge]);
            if (!constraint.perimeter) {
              this.setCellStyles(source ? "exitPerimeter" : "entryPerimeter", false, [
                edge
              ]);
            } else {
              this.setCellStyles(source ? "exitPerimeter" : "entryPerimeter", true, [edge]);
            }
          }
        });
      }
    },
    getConnectionPoint(vertex, constraint, round = true) {
      let point = null;
      if (constraint.point) {
        const bounds = this.getView().getPerimeterBounds(vertex);
        const cx = new Point_default(bounds.getCenterX(), bounds.getCenterY());
        const direction = vertex.style.direction;
        let r1 = 0;
        if (vertex.style.anchorPointDirection) {
          switch (direction) {
            case "north": {
              r1 += 270;
              break;
            }
            case "west": {
              r1 += 180;
              break;
            }
            case "south": {
              r1 += 90;
              break;
            }
          }
          if (direction === "north" || direction === "south") {
            bounds.rotate90();
          }
        }
        const { scale } = this.getView();
        point = new Point_default(bounds.x + constraint.point.x * bounds.width + constraint.dx * scale, bounds.y + constraint.point.y * bounds.height + constraint.dy * scale);
        let r2 = vertex.style.rotation || 0;
        if (constraint.perimeter) {
          if (r1 !== 0) {
            let cos = 0;
            let sin = 0;
            switch (r1) {
              case 90: {
                sin = 1;
                break;
              }
              case 180: {
                cos = -1;
                break;
              }
              case 270: {
                sin = -1;
                break;
              }
            }
            point = getRotatedPoint(point, cos, sin, cx);
          }
          point = this.getView().getPerimeterPoint(vertex, point, false);
        } else {
          r2 += r1;
          if (vertex.cell.isVertex()) {
            let flipH = vertex.style.flipH;
            let flipV = vertex.style.flipV;
            if (direction === "north" || direction === "south") {
              const temp = flipH;
              flipH = flipV;
              flipV = temp;
            }
            if (flipH) {
              point.x = 2 * bounds.getCenterX() - point.x;
            }
            if (flipV) {
              point.y = 2 * bounds.getCenterY() - point.y;
            }
          }
        }
        if (r2 !== 0 && point) {
          const rad = toRadians(r2);
          const cos = Math.cos(rad);
          const sin = Math.sin(rad);
          point = getRotatedPoint(point, cos, sin, cx);
        }
      }
      if (round && point) {
        point.x = Math.round(point.x);
        point.y = Math.round(point.y);
      }
      return point;
    },
    connectCell(edge, terminal = null, source = false, constraint = null) {
      this.batchUpdate(() => {
        const previous = edge.getTerminal(source);
        this.cellConnected(edge, terminal, source, constraint);
        this.fireEvent(new EventObject_default(InternalEvent_default.CONNECT_CELL, "edge", edge, "terminal", terminal, "source", source, "previous", previous));
      });
      return edge;
    },
    cellConnected(edge, terminal, source = false, constraint = null) {
      this.batchUpdate(() => {
        const previous = edge.getTerminal(source);
        this.setConnectionConstraint(edge, terminal, source, constraint);
        if (this.isPortsEnabled()) {
          let id = null;
          if (terminal && this.isPort(terminal)) {
            id = terminal.getId();
            terminal = this.getTerminalForPort(terminal, source);
          }
          const key = source ? "sourcePort" : "targetPort";
          this.setCellStyles(key, id, [edge]);
        }
        this.getDataModel().setTerminal(edge, terminal, source);
        if (this.isResetEdgesOnConnect()) {
          this.resetEdge(edge);
        }
        this.fireEvent(new EventObject_default(InternalEvent_default.CELL_CONNECTED, "edge", edge, "terminal", terminal, "source", source, "previous", previous));
      });
    },
    disconnectGraph(cells) {
      this.batchUpdate(() => {
        const { scale, translate: tr } = this.getView();
        const dict = /* @__PURE__ */ new Map();
        for (let i = 0; i < cells.length; i += 1) {
          dict.set(cells[i], true);
        }
        for (const cell of cells) {
          if (cell.isEdge()) {
            let geo = cell.getGeometry();
            if (geo) {
              const state = this.getView().getState(cell);
              const parent = cell.getParent();
              const pstate = parent ? this.getView().getState(parent) : null;
              if (state && pstate) {
                geo = geo.clone();
                const dx = -pstate.origin.x;
                const dy = -pstate.origin.y;
                const pts = state.absolutePoints;
                let src = cell.getTerminal(true);
                if (src && this.isCellDisconnectable(cell, src, true)) {
                  while (src && !dict.get(src)) {
                    src = src.getParent();
                  }
                  if (!src && pts[0]) {
                    geo.setTerminalPoint(new Point_default(pts[0].x / scale - tr.x + dx, pts[0].y / scale - tr.y + dy), true);
                    this.getDataModel().setTerminal(cell, null, true);
                  }
                }
                let trg = cell.getTerminal(false);
                if (trg && this.isCellDisconnectable(cell, trg, false)) {
                  while (trg && !dict.get(trg)) {
                    trg = trg.getParent();
                  }
                  if (!trg) {
                    const n = pts.length - 1;
                    const p = pts[n];
                    if (p) {
                      geo.setTerminalPoint(new Point_default(p.x / scale - tr.x + dx, p.y / scale - tr.y + dy), false);
                      this.getDataModel().setTerminal(cell, null, false);
                    }
                  }
                }
                this.getDataModel().setGeometry(cell, geo);
              }
            }
          }
        }
      });
    },
    getConnections(cell, parent = null) {
      return this.getEdges(cell, parent, true, true, false);
    },
    isConstrainChild(cell) {
      return this.isConstrainChildren() && !!cell.getParent() && !cell.getParent().isEdge();
    },
    isConstrainChildren() {
      return this.constrainChildren;
    },
    setConstrainChildren(value) {
      this.constrainChildren = value;
    },
    isConstrainRelativeChildren() {
      return this.constrainRelativeChildren;
    },
    setConstrainRelativeChildren(value) {
      this.constrainRelativeChildren = value;
    },
    /*****************************************************************************
     * Group: Graph behaviour
     *****************************************************************************/
    isDisconnectOnMove() {
      return this.disconnectOnMove;
    },
    setDisconnectOnMove(value) {
      this.disconnectOnMove = value;
    },
    isCellDisconnectable(cell, terminal = null, source = false) {
      return this.isCellsDisconnectable() && !this.isCellLocked(cell);
    },
    isCellsDisconnectable() {
      return this.cellsDisconnectable;
    },
    setCellsDisconnectable(value) {
      this.cellsDisconnectable = value;
    },
    isValidSource(cell) {
      return cell == null && this.isAllowDanglingEdges() || cell != null && (!cell.isEdge() || this.isConnectableEdges()) && cell.isConnectable();
    },
    isValidTarget(cell) {
      return this.isValidSource(cell);
    },
    isValidConnection(source, target) {
      return this.isValidSource(source) && this.isValidTarget(target);
    },
    setConnectable(connectable) {
      const connectionHandler = this.getPlugin("ConnectionHandler");
      connectionHandler == null ? void 0 : connectionHandler.setEnabled(connectable);
    },
    isConnectable() {
      var _a2;
      const connectionHandler = this.getPlugin("ConnectionHandler");
      return (_a2 = connectionHandler == null ? void 0 : connectionHandler.isEnabled()) != null ? _a2 : false;
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/mixin/DragDropMixin.js
  var DragDropMixin = {
    dropEnabled: false,
    splitEnabled: true,
    autoScroll: true,
    isAutoScroll() {
      return this.autoScroll;
    },
    autoExtend: true,
    isAutoExtend() {
      return this.autoExtend;
    },
    /*****************************************************************************
     * Group: Graph behaviour
     *****************************************************************************/
    isDropEnabled() {
      return this.dropEnabled;
    },
    setDropEnabled(value) {
      this.dropEnabled = value;
    },
    /*****************************************************************************
     * Group: Split behaviour
     *****************************************************************************/
    isSplitEnabled() {
      return this.splitEnabled;
    },
    setSplitEnabled(value) {
      this.splitEnabled = value;
    },
    isSplitTarget(target, cells = [], evt) {
      if (target.isEdge() && cells.length === 1 && cells[0].isConnectable() && !this.getEdgeValidationError(target, target.getTerminal(true), cells[0])) {
        const src = target.getTerminal(true);
        const trg = target.getTerminal(false);
        return !cells[0].isAncestor(src) && !cells[0].isAncestor(trg);
      }
      return false;
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/mixin/EdgeMixin.js
  var EdgeMixin = {
    resetEdgesOnResize: false,
    isResetEdgesOnResize() {
      return this.resetEdgesOnResize;
    },
    resetEdgesOnMove: false,
    isResetEdgesOnMove() {
      return this.resetEdgesOnMove;
    },
    resetEdgesOnConnect: true,
    isResetEdgesOnConnect() {
      return this.resetEdgesOnConnect;
    },
    connectableEdges: false,
    allowDanglingEdges: true,
    cloneInvalidEdges: false,
    edgeLabelsMovable: true,
    // ***************************************************************************
    // Group: Graph Behaviour
    // ***************************************************************************
    isEdgeLabelsMovable() {
      return this.edgeLabelsMovable;
    },
    setEdgeLabelsMovable(value) {
      this.edgeLabelsMovable = value;
    },
    setAllowDanglingEdges(value) {
      this.allowDanglingEdges = value;
    },
    isAllowDanglingEdges() {
      return this.allowDanglingEdges;
    },
    setConnectableEdges(value) {
      this.connectableEdges = value;
    },
    isConnectableEdges() {
      return this.connectableEdges;
    },
    setCloneInvalidEdges(value) {
      this.cloneInvalidEdges = value;
    },
    isCloneInvalidEdges() {
      return this.cloneInvalidEdges;
    },
    // ***************************************************************************
    // Group: Cell alignment and orientation
    // ***************************************************************************
    flipEdge(edge) {
      if (this.alternateEdgeStyle) {
        this.batchUpdate(() => {
          const style = edge.getStyle();
          if (Object.keys(style).length) {
            this.getDataModel().setStyle(edge, this.alternateEdgeStyle);
          } else {
            this.getDataModel().setStyle(edge, {});
          }
          this.resetEdge(edge);
          this.fireEvent(new EventObject_default(InternalEvent_default.FLIP_EDGE, { edge }));
        });
      }
      return edge;
    },
    splitEdge(edge, cells, newEdge, dx = 0, dy = 0, x, y, parent = null) {
      parent = parent != null ? parent : edge.getParent();
      const source = edge.getTerminal(true);
      this.batchUpdate(() => {
        if (!newEdge) {
          newEdge = this.cloneCell(edge);
          const state = this.getView().getState(edge);
          let geo = newEdge.getGeometry();
          if (geo && state) {
            const t = this.getView().translate;
            const s = this.getView().scale;
            const idx = findNearestSegment(state, (dx + t.x) * s, (dy + t.y) * s);
            geo.points = geo.points.slice(0, idx);
            geo = edge.getGeometry();
            if (geo) {
              geo = geo.clone();
              geo.points = geo.points.slice(idx);
              this.getDataModel().setGeometry(edge, geo);
            }
          }
        }
        this.cellsMoved(cells, dx, dy, false, false);
        this.cellsAdded(cells, parent, parent ? parent.getChildCount() : 0, null, null, true);
        this.cellsAdded([newEdge], parent, parent ? parent.getChildCount() : 0, source, cells[0], false);
        this.cellConnected(edge, cells[0], true);
        this.fireEvent(new EventObject_default(InternalEvent_default.SPLIT_EDGE, { edge, cells, newEdge, dx, dy }));
      });
      return newEdge;
    },
    insertEdge(...args) {
      let parent;
      let id;
      let value;
      let source;
      let target;
      let style;
      if (args.length === 1 && typeof args[0] === "object") {
        const params = args[0];
        parent = params.parent;
        id = params.id;
        value = params.value;
        source = params.source;
        target = params.target;
        style = params.style;
      } else {
        [parent, id, value, source, target, style] = args;
      }
      const edge = this.createEdge(parent, id, value, source, target, style);
      return this.addEdge(edge, parent, source, target);
    },
    createEdge(_parent = null, id, value, _source = null, _target = null, style = {}) {
      const edge = new Cell_default(value, new Geometry_default(), style);
      edge.setId(id);
      edge.setEdge(true);
      edge.geometry.relative = true;
      return edge;
    },
    addEdge(edge, parent = null, source = null, target = null, index = null) {
      return this.addCell(edge, parent, index, source, target);
    },
    // ***************************************************************************
    // Group: Folding
    // ***************************************************************************
    addAllEdges(cells) {
      const allCells = cells.slice();
      return removeDuplicates(allCells.concat(this.getAllEdges(cells)));
    },
    getAllEdges(cells) {
      let edges = [];
      if (cells) {
        for (let i = 0; i < cells.length; i += 1) {
          const edgeCount = cells[i].getEdgeCount();
          for (let j = 0; j < edgeCount; j++) {
            edges.push(cells[i].getEdgeAt(j));
          }
          const children = cells[i].getChildren();
          edges = edges.concat(this.getAllEdges(children));
        }
      }
      return edges;
    },
    getIncomingEdges(cell, parent = null) {
      return this.getEdges(cell, parent, true, false, false);
    },
    getOutgoingEdges(cell, parent = null) {
      return this.getEdges(cell, parent, false, true, false);
    },
    getEdges(cell, parent = null, incoming = true, outgoing = true, includeLoops = true, recurse = false) {
      let edges = [];
      const isCollapsed = cell.isCollapsed();
      const childCount = cell.getChildCount();
      for (let i = 0; i < childCount; i += 1) {
        const child = cell.getChildAt(i);
        if (isCollapsed || !child.isVisible()) {
          edges = edges.concat(child.getEdges(incoming, outgoing));
        }
      }
      edges = edges.concat(cell.getEdges(incoming, outgoing));
      const result = [];
      for (let i = 0; i < edges.length; i += 1) {
        const state = this.getView().getState(edges[i]);
        const source = state ? state.getVisibleTerminal(true) : this.getView().getVisibleTerminal(edges[i], true);
        const target = state ? state.getVisibleTerminal(false) : this.getView().getVisibleTerminal(edges[i], false);
        if (includeLoops && source === target || source !== target && (incoming && target === cell && (!parent || this.isValidAncestor(source, parent, recurse)) || outgoing && source === cell && (!parent || this.isValidAncestor(target, parent, recurse)))) {
          result.push(edges[i]);
        }
      }
      return result;
    },
    // ***************************************************************************
    // Group: Cell retrieval
    // ***************************************************************************
    getChildEdges(parent) {
      return this.getChildCells(parent, false, true);
    },
    getEdgesBetween(source, target, directed = false) {
      const edges = this.getEdges(source);
      const result = [];
      for (let i = 0; i < edges.length; i += 1) {
        const state = this.getView().getState(edges[i]);
        const src = state ? state.getVisibleTerminal(true) : this.getView().getVisibleTerminal(edges[i], true);
        const trg = state ? state.getVisibleTerminal(false) : this.getView().getVisibleTerminal(edges[i], false);
        if (src === source && trg === target || !directed && src === target && trg === source) {
          result.push(edges[i]);
        }
      }
      return result;
    },
    // ***************************************************************************
    // Group: Cell moving
    // ***************************************************************************
    resetEdges(cells) {
      const dict = /* @__PURE__ */ new Map();
      for (let i = 0; i < cells.length; i += 1) {
        dict.set(cells[i], true);
      }
      this.batchUpdate(() => {
        for (let i = 0; i < cells.length; i += 1) {
          const edges = cells[i].getEdges();
          for (let j = 0; j < edges.length; j++) {
            const state = this.getView().getState(edges[j]);
            const source = state ? state.getVisibleTerminal(true) : this.getView().getVisibleTerminal(edges[j], true);
            const target = state ? state.getVisibleTerminal(false) : this.getView().getVisibleTerminal(edges[j], false);
            if (!dict.get(source) || !dict.get(target)) {
              this.resetEdge(edges[j]);
            }
          }
          this.resetEdges(cells[i].getChildren());
        }
      });
    },
    resetEdge(edge) {
      let geo = edge.getGeometry();
      if (geo && geo.points && geo.points.length > 0) {
        geo = geo.clone();
        geo.points = [];
        this.getDataModel().setGeometry(edge, geo);
      }
      return edge;
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/mixin/EditingMixin.js
  var EditingMixin = {
    cellsEditable: true,
    /*****************************************************************************
     * Group: Cell in-place editing
     *****************************************************************************/
    startEditing(evt) {
      this.startEditingAtCell(null, evt);
    },
    startEditingAtCell(cell = null, evt) {
      if (!evt || !isMultiTouchEvent(evt)) {
        if (!cell) {
          cell = this.getSelectionCell();
          if (cell && !this.isCellEditable(cell)) {
            cell = null;
          }
        } else {
          this.fireEvent(new EventObject_default(InternalEvent_default.START_EDITING, { cell, event: evt }));
          const cellEditorHandler = this.getPlugin("CellEditorHandler");
          cellEditorHandler == null ? void 0 : cellEditorHandler.startEditing(cell, evt);
          this.fireEvent(new EventObject_default(InternalEvent_default.EDITING_STARTED, { cell, event: evt }));
        }
      }
    },
    getEditingValue(cell, evt) {
      return this.convertValueToString(cell);
    },
    stopEditing(cancel = false) {
      const cellEditorHandler = this.getPlugin("CellEditorHandler");
      cellEditorHandler == null ? void 0 : cellEditorHandler.stopEditing(cancel);
      this.fireEvent(new EventObject_default(InternalEvent_default.EDITING_STOPPED, { cancel }));
    },
    labelChanged(cell, value, evt) {
      this.batchUpdate(() => {
        const old = cell.value;
        this.cellLabelChanged(cell, value, this.isAutoSizeCell(cell));
        this.fireEvent(new EventObject_default(InternalEvent_default.LABEL_CHANGED, {
          cell,
          value,
          old,
          event: evt
        }));
      });
      return cell;
    },
    cellLabelChanged(cell, value, autoSize = false) {
      this.batchUpdate(() => {
        this.getDataModel().setValue(cell, value);
        if (autoSize) {
          this.cellSizeUpdated(cell, false);
        }
      });
    },
    /*****************************************************************************
     * Group: Graph behaviour
     *****************************************************************************/
    isEditing(cell = null) {
      const cellEditorHandler = this.getPlugin("CellEditorHandler");
      const editingCell = cellEditorHandler == null ? void 0 : cellEditorHandler.getEditingCell();
      return !cell ? !!editingCell : cell === editingCell;
    },
    isCellEditable(cell) {
      var _a2;
      return this.isCellsEditable() && !this.isCellLocked(cell) && ((_a2 = this.getCurrentCellStyle(cell).editable) != null ? _a2 : true);
    },
    isCellsEditable() {
      return this.cellsEditable;
    },
    setCellsEditable(value) {
      this.cellsEditable = value;
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/mixin/EventsMixin.js
  var EventsMixin = {
    // TODO: Document me!
    lastTouchEvent: null,
    doubleClickCounter: 0,
    lastTouchCell: null,
    fireDoubleClick: null,
    tapAndHoldThread: null,
    lastMouseX: null,
    lastMouseY: null,
    isMouseTrigger: null,
    ignoreMouseEvents: null,
    mouseMoveRedirect: null,
    mouseUpRedirect: null,
    lastEvent: null,
    // FIXME: Check if this can be more specific - DOM events or mxEventObjects!
    escapeEnabled: true,
    invokesStopCellEditing: true,
    enterStopsCellEditing: false,
    isMouseDown: false,
    nativeDblClickEnabled: true,
    doubleTapEnabled: true,
    doubleTapTimeout: 500,
    doubleTapTolerance: 25,
    lastTouchX: 0,
    lastTouchY: 0,
    lastTouchTime: 0,
    tapAndHoldEnabled: true,
    tapAndHoldDelay: 500,
    tapAndHoldInProgress: false,
    tapAndHoldValid: false,
    initialTouchX: 0,
    initialTouchY: 0,
    tolerance: 4,
    isNativeDblClickEnabled() {
      return this.nativeDblClickEnabled;
    },
    getEventTolerance() {
      return this.tolerance;
    },
    setEventTolerance(tolerance) {
      this.tolerance = tolerance;
    },
    escape(evt) {
      this.fireEvent(new EventObject_default(InternalEvent_default.ESCAPE, { event: evt }));
    },
    click(me) {
      const evt = me.getEvent();
      let cell = me.getCell();
      const mxe = new EventObject_default(InternalEvent_default.CLICK, { event: evt, cell });
      if (me.isConsumed()) {
        mxe.consume();
      }
      this.fireEvent(mxe);
      if (this.isEnabled() && !isConsumed(evt) && !mxe.isConsumed()) {
        if (cell) {
          if (this.isTransparentClickEvent(evt)) {
            let active = false;
            const tmp = this.getCellAt(me.graphX, me.graphY, null, false, false, (state) => {
              const selected = this.isCellSelected(state.cell);
              active = active || selected;
              return !active || selected || state.cell !== cell && state.cell.isAncestor(cell);
            });
            if (tmp) {
              cell = tmp;
            }
          }
        } else if (this.isSwimlaneSelectionEnabled()) {
          cell = this.getSwimlaneAt(me.getGraphX(), me.getGraphY());
          if (cell != null && (!this.isToggleEvent(evt) || !isAltDown(evt))) {
            let temp = cell;
            let swimlanes = [];
            while (temp != null) {
              temp = temp.getParent();
              const state = this.getView().getState(temp);
              if (this.isSwimlane(temp) && state != null) {
                swimlanes.push(temp);
              }
            }
            if (swimlanes.length > 0) {
              swimlanes = swimlanes.reverse();
              swimlanes.splice(0, 0, cell);
              swimlanes.push(cell);
              for (let i = 0; i < swimlanes.length - 1; i += 1) {
                if (this.isCellSelected(swimlanes[i])) {
                  cell = swimlanes[this.isToggleEvent(evt) ? i : i + 1];
                }
              }
            }
          }
        }
        if (cell) {
          this.selectCellForEvent(cell, evt);
        } else if (!this.isToggleEvent(evt)) {
          this.clearSelection();
        }
      }
      return false;
    },
    dblClick(evt, cell = null) {
      const mxe = new EventObject_default(InternalEvent_default.DOUBLE_CLICK, { event: evt, cell });
      this.fireEvent(mxe);
      if (this.isEnabled() && !isConsumed(evt) && !mxe.isConsumed() && cell && this.isCellEditable(cell) && !this.isEditing(cell)) {
        this.startEditingAtCell(cell, evt);
        InternalEvent_default.consume(evt);
      }
    },
    tapAndHold(me) {
      const evt = me.getEvent();
      const mxe = new EventObject_default(InternalEvent_default.TAP_AND_HOLD, {
        event: evt,
        cell: me.getCell()
      });
      const panningHandler = this.getPlugin("PanningHandler");
      const connectionHandler = this.getPlugin("ConnectionHandler");
      this.fireEvent(mxe);
      if (mxe.isConsumed()) {
        panningHandler && (panningHandler.panningTrigger = false);
      }
      if (this.isEnabled() && !isConsumed(evt) && !mxe.isConsumed() && connectionHandler && connectionHandler.isEnabled()) {
        const cell = connectionHandler.marker.getCell(me);
        if (cell) {
          const state = this.getView().getState(cell);
          if (state) {
            connectionHandler.marker.currentColor = connectionHandler.marker.validColor;
            connectionHandler.marker.markedState = state;
            connectionHandler.marker.mark();
            connectionHandler.first = new Point_default(me.getGraphX(), me.getGraphY());
            connectionHandler.edgeState = connectionHandler.createEdgeState(me);
            connectionHandler.previous = state;
            connectionHandler.fireEvent(new EventObject_default(InternalEvent_default.START, { state: connectionHandler.previous }));
          }
        }
      }
    },
    addMouseListener(listener) {
      this.mouseListeners.push(listener);
    },
    removeMouseListener(listener) {
      for (let i = 0; i < this.mouseListeners.length; i += 1) {
        if (this.mouseListeners[i] === listener) {
          this.mouseListeners.splice(i, 1);
          break;
        }
      }
    },
    updateMouseEvent(me, evtName) {
      const pt = convertPoint(this.getContainer(), me.getX(), me.getY());
      me.graphX = pt.x - this.getPanDx();
      me.graphY = pt.y - this.getPanDy();
      if (!me.getCell() && this.isMouseDown && evtName === InternalEvent_default.MOUSE_MOVE) {
        const cell = this.getCellAt(pt.x, pt.y, null, true, true, (state) => {
          return !state.shape || state.shape.paintBackground !== this.paintBackground || state.style.pointerEvents || state.shape.fill !== NONE;
        });
        me.state = cell ? this.getView().getState(cell) : null;
      }
      return me;
    },
    getStateForTouchEvent(evt) {
      const x = getClientX(evt);
      const y = getClientY(evt);
      const pt = convertPoint(this.getContainer(), x, y);
      const cell = this.getCellAt(pt.x, pt.y);
      return cell ? this.getView().getState(cell) : null;
    },
    isEventIgnored(evtName, me, sender) {
      const mouseEvent = isMouseEvent(me.getEvent());
      let result = false;
      if (me.getEvent() === this.lastEvent) {
        result = true;
      } else {
        this.lastEvent = me.getEvent();
      }
      const eventSource = this.getEventSource();
      if (eventSource && evtName !== InternalEvent_default.MOUSE_MOVE) {
        InternalEvent_default.removeGestureListeners(eventSource, null, this.mouseMoveRedirect, this.mouseUpRedirect);
        this.mouseMoveRedirect = null;
        this.mouseUpRedirect = null;
        this.setEventSource(null);
      } else if (!Client_default.IS_GC && eventSource && me.getSource() !== eventSource) {
        result = true;
      } else if (eventSource && Client_default.IS_TOUCH && evtName === InternalEvent_default.MOUSE_DOWN && !mouseEvent && !isPenEvent(me.getEvent())) {
        this.setEventSource(me.getSource());
        this.mouseMoveRedirect = (evt) => {
          this.fireMouseEvent(InternalEvent_default.MOUSE_MOVE, new InternalMouseEvent_default(evt, this.getStateForTouchEvent(evt)));
        };
        this.mouseUpRedirect = (evt) => {
          this.fireMouseEvent(InternalEvent_default.MOUSE_UP, new InternalMouseEvent_default(evt, this.getStateForTouchEvent(evt)));
        };
        InternalEvent_default.addGestureListeners(eventSource, null, this.mouseMoveRedirect, this.mouseUpRedirect);
      }
      if (this.isSyntheticEventIgnored(evtName, me, sender)) {
        result = true;
      }
      if (!isPopupTrigger(this.lastEvent) && evtName !== InternalEvent_default.MOUSE_MOVE && this.lastEvent.detail === 2) {
        return true;
      }
      if (evtName === InternalEvent_default.MOUSE_UP && this.isMouseDown) {
        this.isMouseDown = false;
      } else if (evtName === InternalEvent_default.MOUSE_DOWN && !this.isMouseDown) {
        this.isMouseDown = true;
        this.isMouseTrigger = mouseEvent;
      } else if (!result && ((!Client_default.IS_FF || evtName !== InternalEvent_default.MOUSE_MOVE) && this.isMouseDown && this.isMouseTrigger !== mouseEvent || evtName === InternalEvent_default.MOUSE_DOWN && this.isMouseDown || evtName === InternalEvent_default.MOUSE_UP && !this.isMouseDown)) {
        result = true;
      }
      if (!result && evtName === InternalEvent_default.MOUSE_DOWN) {
        this.lastMouseX = me.getX();
        this.lastMouseY = me.getY();
      }
      return result;
    },
    isSyntheticEventIgnored(evtName, me, sender) {
      let result = false;
      const mouseEvent = isMouseEvent(me.getEvent());
      if (this.ignoreMouseEvents && mouseEvent && evtName !== InternalEvent_default.MOUSE_MOVE) {
        this.ignoreMouseEvents = evtName !== InternalEvent_default.MOUSE_UP;
        result = true;
      } else if (Client_default.IS_FF && !mouseEvent && evtName === InternalEvent_default.MOUSE_UP) {
        this.ignoreMouseEvents = true;
      }
      return result;
    },
    isEventSourceIgnored(evtName, me) {
      const source = me.getSource();
      if (!source)
        return true;
      const name = source.nodeName ? source.nodeName.toLowerCase() : "";
      const candidate = !isMouseEvent(me.getEvent()) || isLeftMouseButton(me.getEvent());
      return evtName === InternalEvent_default.MOUSE_DOWN && candidate && (name === "select" || name === "option" || name === "input" && // @ts-ignore type could exist
      source.type !== "checkbox" && // @ts-ignore type could exist
      source.type !== "radio" && // @ts-ignore type could exist
      source.type !== "button" && // @ts-ignore type could exist
      source.type !== "submit" && // @ts-ignore type could exist
      source.type !== "file");
    },
    getEventState(state) {
      return state;
    },
    fireMouseEvent(evtName, me, sender) {
      sender = sender != null ? sender : this;
      if (this.isEventSourceIgnored(evtName, me)) {
        const tooltipHandler = this.getPlugin("TooltipHandler");
        if (tooltipHandler) {
          tooltipHandler.hide();
        }
        return;
      }
      me = this.updateMouseEvent(me, evtName);
      if (!this.nativeDblClickEnabled && !isPopupTrigger(me.getEvent()) || this.doubleTapEnabled && Client_default.IS_TOUCH && (isTouchEvent(me.getEvent()) || isPenEvent(me.getEvent()))) {
        const currentTime = (/* @__PURE__ */ new Date()).getTime();
        if (evtName === InternalEvent_default.MOUSE_DOWN) {
          if (this.lastTouchEvent && this.lastTouchEvent !== me.getEvent() && currentTime - this.lastTouchTime < this.doubleTapTimeout && Math.abs(this.lastTouchX - me.getX()) < this.doubleTapTolerance && Math.abs(this.lastTouchY - me.getY()) < this.doubleTapTolerance && this.doubleClickCounter < 2) {
            this.doubleClickCounter += 1;
            let doubleClickFired = false;
            if (evtName === InternalEvent_default.MOUSE_UP) {
              if (me.getCell() === this.lastTouchCell && this.lastTouchCell) {
                this.lastTouchTime = 0;
                const cell = this.lastTouchCell;
                this.lastTouchCell = null;
                this.dblClick(me.getEvent(), cell);
                doubleClickFired = true;
              }
            } else {
              this.fireDoubleClick = true;
              this.lastTouchTime = 0;
            }
            if (doubleClickFired) {
              InternalEvent_default.consume(me.getEvent());
              return;
            }
          } else if (!this.lastTouchEvent || this.lastTouchEvent !== me.getEvent()) {
            this.lastTouchCell = me.getCell();
            this.lastTouchX = me.getX();
            this.lastTouchY = me.getY();
            this.lastTouchTime = currentTime;
            this.lastTouchEvent = me.getEvent();
            this.doubleClickCounter = 0;
          }
        } else if ((this.isMouseDown || evtName === InternalEvent_default.MOUSE_UP) && this.fireDoubleClick) {
          this.fireDoubleClick = false;
          const cell = this.lastTouchCell;
          this.lastTouchCell = null;
          this.isMouseDown = false;
          const valid = cell || (isTouchEvent(me.getEvent()) || isPenEvent(me.getEvent())) && (Client_default.IS_GC || Client_default.IS_SF);
          if (valid && Math.abs(this.lastTouchX - me.getX()) < this.doubleTapTolerance && Math.abs(this.lastTouchY - me.getY()) < this.doubleTapTolerance) {
            this.dblClick(me.getEvent(), cell);
          } else {
            InternalEvent_default.consume(me.getEvent());
          }
          return;
        }
      }
      if (!this.isEventIgnored(evtName, me, sender)) {
        const state = me.getState();
        me.state = state ? this.getEventState(state) : null;
        this.fireEvent(new EventObject_default(InternalEvent_default.FIRE_MOUSE_EVENT, { eventName: evtName, event: me }));
        if (Client_default.IS_SF || Client_default.IS_GC || me.getEvent().target !== this.getContainer()) {
          const container = this.getContainer();
          if (evtName === InternalEvent_default.MOUSE_MOVE && this.isMouseDown && this.isAutoScroll() && !isMultiTouchEvent(me.getEvent())) {
            this.scrollPointToVisible(me.getGraphX(), me.getGraphY(), this.isAutoExtend());
          } else if (evtName === InternalEvent_default.MOUSE_UP && this.isIgnoreScrollbars() && this.isTranslateToScrollPosition() && (container.scrollLeft !== 0 || container.scrollTop !== 0)) {
            const s = this.getView().scale;
            const tr = this.getView().translate;
            this.getView().setTranslate(tr.x - container.scrollLeft / s, tr.y - container.scrollTop / s);
            container.scrollLeft = 0;
            container.scrollTop = 0;
          }
          const mouseListeners = this.mouseListeners;
          if (!me.getEvent().preventDefault) {
            me.getEvent().returnValue = true;
          }
          for (const l of mouseListeners) {
            switch (evtName) {
              case InternalEvent_default.MOUSE_DOWN: {
                l.mouseDown(sender, me);
                break;
              }
              case InternalEvent_default.MOUSE_MOVE: {
                l.mouseMove(sender, me);
                break;
              }
              case InternalEvent_default.MOUSE_UP: {
                l.mouseUp(sender, me);
                break;
              }
            }
          }
          if (evtName === InternalEvent_default.MOUSE_UP) {
            this.click(me);
          }
        }
        if ((isTouchEvent(me.getEvent()) || isPenEvent(me.getEvent())) && evtName === InternalEvent_default.MOUSE_DOWN && this.tapAndHoldEnabled && !this.tapAndHoldInProgress) {
          this.tapAndHoldInProgress = true;
          this.initialTouchX = me.getGraphX();
          this.initialTouchY = me.getGraphY();
          const handler = () => {
            if (this.tapAndHoldValid) {
              this.tapAndHold(me);
            }
            this.tapAndHoldInProgress = false;
            this.tapAndHoldValid = false;
          };
          if (this.tapAndHoldThread) {
            window.clearTimeout(this.tapAndHoldThread);
          }
          this.tapAndHoldThread = window.setTimeout(handler, this.tapAndHoldDelay);
          this.tapAndHoldValid = true;
        } else if (evtName === InternalEvent_default.MOUSE_UP) {
          this.tapAndHoldInProgress = false;
          this.tapAndHoldValid = false;
        } else if (this.tapAndHoldValid) {
          this.tapAndHoldValid = Math.abs(this.initialTouchX - me.getGraphX()) < this.tolerance && Math.abs(this.initialTouchY - me.getGraphY()) < this.tolerance;
        }
        const cellEditorHandler = this.getPlugin("CellEditorHandler");
        if (evtName === InternalEvent_default.MOUSE_DOWN && this.isEditing() && !(cellEditorHandler == null ? void 0 : cellEditorHandler.isEventSource(me.getEvent()))) {
          this.stopEditing(!this.isInvokesStopCellEditing());
        }
        this.consumeMouseEvent(evtName, me, sender);
      }
    },
    consumeMouseEvent(evtName, me, sender) {
      sender = sender != null ? sender : this;
      if (evtName === InternalEvent_default.MOUSE_DOWN && isTouchEvent(me.getEvent())) {
        me.consume(false);
      }
    },
    fireGestureEvent(evt, cell = null) {
      this.lastTouchTime = 0;
      this.fireEvent(new EventObject_default(InternalEvent_default.GESTURE, { event: evt, cell }));
    },
    sizeDidChange() {
      const bounds = this.getGraphBounds();
      const border = this.getBorder();
      let width = Math.max(0, bounds.x) + bounds.width + 2 * border;
      let height = Math.max(0, bounds.y) + bounds.height + 2 * border;
      const minimumContainerSize = this.getMinimumContainerSize();
      if (minimumContainerSize) {
        width = Math.max(width, minimumContainerSize.width);
        height = Math.max(height, minimumContainerSize.height);
      }
      if (this.isResizeContainer()) {
        this.doResizeContainer(width, height);
      }
      if (this.isPreferPageSize() || this.isPageVisible()) {
        const size = this.getPreferredPageSize(bounds, Math.max(1, width), Math.max(1, height));
        width = size.width * this.getView().scale;
        height = size.height * this.getView().scale;
      }
      const minimumGraphSize = this.getMinimumGraphSize();
      if (minimumGraphSize) {
        width = Math.max(width, minimumGraphSize.width * this.getView().scale);
        height = Math.max(height, minimumGraphSize.height * this.getView().scale);
      }
      width = Math.ceil(width);
      height = Math.ceil(height);
      const root = this.getView().getDrawPane().ownerSVGElement;
      if (root) {
        root.style.minWidth = `${Math.max(1, width)}px`;
        root.style.minHeight = `${Math.max(1, height)}px`;
        root.style.width = "100%";
        root.style.height = "100%";
      }
      this.updatePageBreaks(this.isPageBreaksVisible(), width, height);
      this.fireEvent(new EventObject_default(InternalEvent_default.SIZE, { bounds }));
    },
    isCloneEvent(evt) {
      return isControlDown(evt);
    },
    isTransparentClickEvent(evt) {
      return false;
    },
    isToggleEvent(evt) {
      return Client_default.IS_MAC ? isMetaDown(evt) : isControlDown(evt);
    },
    isGridEnabledEvent(evt) {
      return !isAltDown(evt);
    },
    isConstrainedEvent(evt) {
      return isShiftDown(evt);
    },
    isIgnoreTerminalEvent(_evt) {
      return false;
    },
    getPointForEvent(evt, addOffset = true) {
      const p = convertPoint(this.getContainer(), getClientX(evt), getClientY(evt));
      const s = this.getView().scale;
      const tr = this.getView().translate;
      const off = addOffset ? this.getGridSize() / 2 : 0;
      p.x = this.snap(p.x / s - tr.x - off);
      p.y = this.snap(p.y / s - tr.y - off);
      return p;
    },
    isEscapeEnabled() {
      return this.escapeEnabled;
    },
    setEscapeEnabled(value) {
      this.escapeEnabled = value;
    },
    isInvokesStopCellEditing() {
      return this.invokesStopCellEditing;
    },
    setInvokesStopCellEditing(value) {
      this.invokesStopCellEditing = value;
    },
    isEnterStopsCellEditing() {
      return this.enterStopsCellEditing;
    },
    setEnterStopsCellEditing(value) {
      this.enterStopsCellEditing = value;
    },
    getCursorForMouseEvent(me) {
      const cell = me.getCell();
      return cell ? this.getCursorForCell(cell) : null;
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/mixin/FoldingMixin.js
  var FoldingMixin = {
    collapseExpandResource: isI18nEnabled() ? "collapse-expand" : "",
    getCollapseExpandResource() {
      return this.collapseExpandResource;
    },
    isFoldingEnabled() {
      return this.options.foldingEnabled;
    },
    getFoldableCells(cells, collapse = false) {
      return this.getDataModel().filterCells(cells, (cell) => {
        return this.isCellFoldable(cell, collapse);
      });
    },
    isCellFoldable(cell, _collapse) {
      var _a2;
      return cell.getChildCount() > 0 && ((_a2 = this.getCurrentCellStyle(cell).foldable) != null ? _a2 : true);
    },
    getFoldingImage(state) {
      if (state != null && this.isFoldingEnabled() && !state.cell.isEdge()) {
        const tmp = state.cell.isCollapsed();
        if (this.isCellFoldable(state.cell, !tmp)) {
          return tmp ? this.options.collapsedImage : this.options.expandedImage;
        }
      }
      return null;
    },
    foldCells(collapse = false, recurse = false, cells = null, checkFoldable = false, _evt = null) {
      if (cells == null) {
        cells = this.getFoldableCells(this.getSelectionCells(), collapse);
      }
      this.stopEditing(false);
      this.batchUpdate(() => {
        this.cellsFolded(cells, collapse, recurse, checkFoldable);
        this.fireEvent(new EventObject_default(InternalEvent_default.FOLD_CELLS, "collapse", collapse, "recurse", recurse, "cells", cells));
      });
      return cells;
    },
    cellsFolded(cells = null, collapse = false, recurse = false, checkFoldable = false) {
      if (cells != null && cells.length > 0) {
        this.batchUpdate(() => {
          for (let i = 0; i < cells.length; i += 1) {
            if ((!checkFoldable || this.isCellFoldable(cells[i], collapse)) && collapse !== cells[i].isCollapsed()) {
              this.getDataModel().setCollapsed(cells[i], collapse);
              this.swapBounds(cells[i], collapse);
              if (this.isExtendParent(cells[i])) {
                this.extendParent(cells[i]);
              }
              if (recurse) {
                const children = cells[i].getChildren();
                this.cellsFolded(children, collapse, recurse);
              }
              this.constrainChild(cells[i]);
            }
          }
          this.fireEvent(new EventObject_default(InternalEvent_default.CELLS_FOLDED, { cells, collapse, recurse }));
        });
      }
    },
    swapBounds(cell, willCollapse = false) {
      let geo = cell.getGeometry();
      if (geo != null) {
        geo = geo.clone();
        this.updateAlternateBounds(cell, geo, willCollapse);
        geo.swap();
        this.getDataModel().setGeometry(cell, geo);
      }
    },
    updateAlternateBounds(cell = null, geo = null, _willCollapse = false) {
      var _a2;
      if (cell != null && geo != null) {
        const style = this.getCurrentCellStyle(cell);
        if (geo.alternateBounds == null) {
          let bounds = geo;
          if (this.options.collapseToPreferredSize) {
            const tmp = this.getPreferredSizeForCell(cell);
            if (tmp != null) {
              bounds = tmp;
              const startSize = (_a2 = style.startSize) != null ? _a2 : 0;
              if (startSize > 0) {
                bounds.height = Math.max(bounds.height, startSize);
              }
            }
          }
          geo.alternateBounds = new Rectangle_default(0, 0, bounds.width, bounds.height);
        }
        if (geo.alternateBounds != null) {
          geo.alternateBounds.x = geo.x;
          geo.alternateBounds.y = geo.y;
          const alpha = toRadians(style.rotation || 0);
          if (alpha !== 0) {
            const dx = geo.alternateBounds.getCenterX() - geo.getCenterX();
            const dy = geo.alternateBounds.getCenterY() - geo.getCenterY();
            const cos = Math.cos(alpha);
            const sin = Math.sin(alpha);
            const dx2 = cos * dx - sin * dy;
            const dy2 = sin * dx + cos * dy;
            geo.alternateBounds.x += dx2 - dx;
            geo.alternateBounds.y += dy2 - dy;
          }
        }
      }
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/mixin/GroupingMixin.js
  var GroupingMixin = {
    groupCells(group, border = 0, cells) {
      if (!cells)
        cells = sortCells(this.getSelectionCells(), true);
      if (!cells)
        cells = this.getCellsForGroup(cells);
      if (group == null) {
        group = this.createGroupCell(cells);
      }
      const bounds = this.getBoundsForGroup(group, cells, border);
      if (cells.length > 1 && bounds != null) {
        let parent = group.getParent();
        if (parent == null) {
          parent = cells[0].getParent();
        }
        this.batchUpdate(() => {
          if (group.getGeometry() == null) {
            this.getDataModel().setGeometry(group, new Geometry_default());
          }
          let index = parent.getChildCount();
          this.cellsAdded([group], parent, index, null, null, false, false, false);
          index = group.getChildCount();
          this.cellsAdded(cells, group, index, null, null, false, false, false);
          this.cellsMoved(cells, -bounds.x, -bounds.y, false, false, false);
          this.cellsResized([group], [bounds], false);
          this.fireEvent(new EventObject_default(InternalEvent_default.GROUP_CELLS, { group, border, cells }));
        });
      }
      return group;
    },
    getCellsForGroup(cells) {
      const result = [];
      if (cells != null && cells.length > 0) {
        const parent = cells[0].getParent();
        result.push(cells[0]);
        for (let i = 1; i < cells.length; i += 1) {
          if (cells[i].getParent() === parent) {
            result.push(cells[i]);
          }
        }
      }
      return result;
    },
    getBoundsForGroup(group, children, border) {
      const result = this.getBoundingBoxFromGeometry(children, true);
      if (result != null) {
        if (this.isSwimlane(group)) {
          const size = this.getStartSize(group);
          result.x -= size.width;
          result.y -= size.height;
          result.width += size.width;
          result.height += size.height;
        }
        if (border != null) {
          result.x -= border;
          result.y -= border;
          result.width += 2 * border;
          result.height += 2 * border;
        }
      }
      return result;
    },
    createGroupCell(cells) {
      const group = new Cell_default("");
      group.setVertex(true);
      group.setConnectable(false);
      return group;
    },
    ungroupCells(cells) {
      let result = [];
      if (cells == null) {
        cells = this.getCellsForUngroup();
      }
      if (cells != null && cells.length > 0) {
        this.batchUpdate(() => {
          const _cells = cells;
          for (let i = 0; i < _cells.length; i += 1) {
            let children = _cells[i].getChildren();
            if (children != null && children.length > 0) {
              children = children.slice();
              const parent = _cells[i].getParent();
              const index = parent.getChildCount();
              this.cellsAdded(children, parent, index, null, null, true);
              result = result.concat(children);
              for (const child of children) {
                const state = this.getView().getState(child);
                let geo = child.getGeometry();
                if (state != null && geo != null && geo.relative) {
                  geo = geo.clone();
                  geo.x = state.origin.x;
                  geo.y = state.origin.y;
                  geo.relative = false;
                  this.getDataModel().setGeometry(child, geo);
                }
              }
            }
          }
          this.removeCellsAfterUngroup(_cells);
          this.fireEvent(new EventObject_default(InternalEvent_default.UNGROUP_CELLS, { cells }));
        });
      }
      return result;
    },
    getCellsForUngroup() {
      const cells = this.getSelectionCells();
      const tmp = [];
      for (let i = 0; i < cells.length; i += 1) {
        if (cells[i].isVertex() && cells[i].getChildCount() > 0) {
          tmp.push(cells[i]);
        }
      }
      return tmp;
    },
    removeCellsAfterUngroup(cells) {
      this.cellsRemoved(this.addAllEdges(cells));
    },
    removeCellsFromParent(cells) {
      if (cells == null) {
        cells = this.getSelectionCells();
      }
      this.batchUpdate(() => {
        const parent = this.getDefaultParent();
        const index = parent.getChildCount();
        this.cellsAdded(cells, parent, index, null, null, true);
        this.fireEvent(new EventObject_default(InternalEvent_default.REMOVE_CELLS_FROM_PARENT, { cells }));
      });
      return cells;
    },
    updateGroupBounds(cells, border = 0, moveGroup = false, topBorder = 0, rightBorder = 0, bottomBorder = 0, leftBorder = 0) {
      if (cells == null) {
        cells = this.getSelectionCells();
      }
      border = border != null ? border : 0;
      moveGroup = moveGroup != null ? moveGroup : false;
      topBorder = topBorder != null ? topBorder : 0;
      rightBorder = rightBorder != null ? rightBorder : 0;
      bottomBorder = bottomBorder != null ? bottomBorder : 0;
      leftBorder = leftBorder != null ? leftBorder : 0;
      this.batchUpdate(() => {
        for (let i = cells.length - 1; i >= 0; i--) {
          let geo = cells[i].getGeometry();
          if (geo == null) {
            continue;
          }
          const children = this.getChildCells(cells[i]);
          if (children != null && children.length > 0) {
            const bounds = this.getBoundingBoxFromGeometry(children, true);
            if (bounds != null && bounds.width > 0 && bounds.height > 0) {
              const size = this.isSwimlane(cells[i]) ? this.getActualStartSize(cells[i], true) : new Rectangle_default();
              geo = geo.clone();
              if (moveGroup) {
                geo.x = Math.round(geo.x + bounds.x - border - size.x - leftBorder);
                geo.y = Math.round(geo.y + bounds.y - border - size.y - topBorder);
              }
              geo.width = Math.round(bounds.width + 2 * border + size.x + leftBorder + rightBorder + size.width);
              geo.height = Math.round(bounds.height + 2 * border + size.y + topBorder + bottomBorder + size.height);
              this.getDataModel().setGeometry(cells[i], geo);
              this.moveCells(children, border + size.x - bounds.x + leftBorder, border + size.y - bounds.y + topBorder);
            }
          }
        }
      });
      return cells;
    },
    /*****************************************************************************
     * Group: Drilldown
     *****************************************************************************/
    enterGroup(cell) {
      cell = cell || this.getSelectionCell();
      if (cell != null && this.isValidRoot(cell)) {
        this.getView().setCurrentRoot(cell);
        this.clearSelection();
      }
    },
    exitGroup() {
      const root = this.getDataModel().getRoot();
      const current = this.getCurrentRoot();
      if (current != null) {
        let next = current.getParent();
        while (next !== root && !this.isValidRoot(next) && next.getParent() !== root) {
          next = next.getParent();
        }
        if (next === root || next.getParent() === root) {
          this.getView().setCurrentRoot(null);
        } else {
          this.getView().setCurrentRoot(next);
        }
        const state = this.getView().getState(current);
        if (state != null) {
          this.setSelectionCell(current);
        }
      }
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/mixin/LabelMixin.js
  var LabelMixin = {
    getLabel(cell) {
      var _a2;
      let result = "";
      if (this.isLabelsVisible() && cell) {
        const style = this.getCurrentCellStyle(cell);
        if (!((_a2 = style.noLabel) != null ? _a2 : false)) {
          result = this.convertValueToString(cell);
        }
      }
      return result;
    },
    isHtmlLabel(_cell) {
      return this.isHtmlLabels();
    },
    labelsVisible: true,
    isLabelsVisible() {
      return this.labelsVisible;
    },
    htmlLabels: false,
    isHtmlLabels() {
      return this.htmlLabels;
    },
    setHtmlLabels(value) {
      this.htmlLabels = value;
    },
    isWrapping(cell) {
      return this.getCurrentCellStyle(cell).whiteSpace === "wrap";
    },
    isLabelClipped(cell) {
      return this.getCurrentCellStyle(cell).overflow === "hidden";
    },
    isLabelMovable(cell) {
      return !this.isCellLocked(cell) && (cell.isEdge() && this.isEdgeLabelsMovable() || cell.isVertex() && this.isVertexLabelsMovable());
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/mixin/OrderMixin.js
  var OrderMixin = {
    orderCells(back = false, cells) {
      if (!cells)
        cells = this.getSelectionCells();
      if (!cells) {
        cells = sortCells(this.getSelectionCells(), true);
      }
      this.batchUpdate(() => {
        this.cellsOrdered(cells, back);
        const event = new EventObject_default(InternalEvent_default.ORDER_CELLS, "back", back, "cells", cells);
        this.fireEvent(event);
      });
      return cells;
    },
    cellsOrdered(cells, back = false) {
      this.batchUpdate(() => {
        for (let i = 0; i < cells.length; i += 1) {
          const parent = cells[i].getParent();
          if (back) {
            this.getDataModel().add(parent, cells[i], i);
          } else {
            this.getDataModel().add(parent, cells[i], parent ? parent.getChildCount() - 1 : 0);
          }
        }
        this.fireEvent(new EventObject_default(InternalEvent_default.CELLS_ORDERED, { back, cells }));
      });
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/cell/CellOverlay.js
  var CellOverlay = class extends EventSource_default {
    /**
     * Constructs a new overlay using the given image and tooltip.
     *
     * @param image {@link ImageBox} that represents the icon to be displayed.
     * @param tooltip Optional string that specifies the tooltip.
     * @param align Optional horizontal alignment for the overlay. Possible values are 'left', 'center' and 'right' (default).
     * @param verticalAlign Vertical alignment for the overlay. Possible values are 'top', 'middle' and 'bottom' (default).
     * @param offset Optional offset for positioning the overlay relative to its alignment. Scaled according to the current graph scale. Default is `new Point()`.
     * @param cursor Optional CSS cursor for the overlay. Default is `'help'`.
     */
    constructor(image, tooltip = null, align = "right", verticalAlign = "bottom", offset = new Point_default(), cursor = "help") {
      super();
      this.align = "right";
      this.verticalAlign = "bottom";
      this.offset = new Point_default();
      this.cursor = "help";
      this.defaultOverlap = 0.5;
      this.image = image;
      this.tooltip = tooltip;
      this.align = align;
      this.verticalAlign = verticalAlign;
      this.offset = offset;
      this.cursor = cursor;
    }
    /**
     * Returns the bounds of the overlay for the given {@link CellState} as an {@link Rectangle}.
     * This should be overridden when using multiple overlays per cell so that the overlays do not overlap.
     *
     * The following example will place the overlay along an edge (where x=[-1..1] from the start to the end of the edge
     * and y is the orthogonal offset in px).
     *
     * ```javascript
     * const overlayBounds = overlay.getBounds;
     * overlay.getBounds = function(state) {
     *   const bounds = overlayBounds.call(this, state);
     *
     *   if (state.view.graph.getDataModel().isEdge(state.cell)) {
     *     const pt = state.view.getPoint(state, {x: 0, y: 0, relative: true});
     *
     *     bounds.x = pt.x - bounds.width / 2;
     *     bounds.y = pt.y - bounds.height / 2;
     *   }
     *
     *   return bounds;
     * };
     * ```
     *
     * @param state {@link CellState} that represents the current state of the associated cell.
     */
    getBounds(state) {
      const isEdge = state.cell.isEdge();
      const s = state.view.scale;
      let pt = null;
      const image = this.image;
      const w = image.width;
      const h = image.height;
      if (isEdge) {
        const pts = state.absolutePoints;
        if (pts.length % 2 === 1) {
          pt = pts[Math.floor(pts.length / 2)];
        } else {
          const idx = pts.length / 2;
          const p0 = pts[idx - 1];
          const p1 = pts[idx];
          pt = new Point_default(p0.x + (p1.x - p0.x) / 2, p0.y + (p1.y - p0.y) / 2);
        }
      } else {
        pt = new Point_default();
        switch (this.align) {
          case "left": {
            pt.x = state.x;
            break;
          }
          case "center": {
            pt.x = state.x + state.width / 2;
            break;
          }
          case "right": {
            pt.x = state.x + state.width;
            break;
          }
          default: {
            throw new Error();
          }
        }
        switch (this.verticalAlign) {
          case "top": {
            pt.y = state.y;
            break;
          }
          case "middle": {
            pt.y = state.y + state.height / 2;
            break;
          }
          case "bottom": {
            pt.y = state.y + state.height;
            break;
          }
          default: {
            throw new Error();
          }
        }
      }
      return new Rectangle_default(Math.round(pt.x - (w * this.defaultOverlap - this.offset.x) * s), Math.round(pt.y - (h * this.defaultOverlap - this.offset.y) * s), w * s, h * s);
    }
    /**
     * Returns the textual representation of the overlay to be used as the tooltip.
     *
     * This implementation returns {@link tooltip}.
     */
    toString() {
      return this.tooltip;
    }
  };
  var CellOverlay_default = CellOverlay;

  // node_modules/@maxgraph/core/lib/esm/view/mixin/OverlaysMixin.js
  var OverlaysMixin = {
    addCellOverlay(cell, overlay) {
      cell.overlays.push(overlay);
      const state = this.getView().getState(cell);
      if (state) {
        this.getCellRenderer().redraw(state);
      }
      this.fireEvent(new EventObject_default(InternalEvent_default.ADD_OVERLAY, { cell, overlay }));
      return overlay;
    },
    getCellOverlays(cell) {
      return cell.overlays;
    },
    removeCellOverlay(cell, overlay = null) {
      if (!overlay) {
        this.removeCellOverlays(cell);
      } else {
        const index = cell.overlays.indexOf(overlay);
        if (index >= 0) {
          cell.overlays.splice(index, 1);
          const state = this.getView().getState(cell);
          if (state) {
            this.getCellRenderer().redraw(state);
          }
          this.fireEvent(new EventObject_default(InternalEvent_default.REMOVE_OVERLAY, { cell, overlay }));
        } else {
          overlay = null;
        }
      }
      return overlay;
    },
    removeCellOverlays(cell) {
      const { overlays } = cell;
      cell.overlays = [];
      const state = this.getView().getState(cell);
      if (state) {
        this.getCellRenderer().redraw(state);
      }
      for (let i = 0; i < overlays.length; i += 1) {
        this.fireEvent(new EventObject_default(InternalEvent_default.REMOVE_OVERLAY, "cell", cell, "overlay", overlays[i]));
      }
      return overlays;
    },
    clearCellOverlays(cell = null) {
      cell = cell != null ? cell : this.getDataModel().getRoot();
      if (!cell)
        return;
      this.removeCellOverlays(cell);
      const childCount = cell.getChildCount();
      for (let i = 0; i < childCount; i += 1) {
        const child = cell.getChildAt(i);
        this.clearCellOverlays(child);
      }
    },
    setCellWarning(cell, warning = null, img, isSelect = false) {
      img = img != null ? img : this.getWarningImage();
      if (warning && warning.length > 0) {
        const overlay = new CellOverlay_default(img, `<font color=red>${warning}</font>`);
        if (isSelect) {
          overlay.addListener(InternalEvent_default.CLICK, (sender, evt) => {
            if (this.isEnabled()) {
              this.setSelectionCell(cell);
            }
          });
        }
        return this.addCellOverlay(cell, overlay);
      }
      this.removeCellOverlays(cell);
      return null;
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/shape/edge/PolylineShape.js
  var PolylineShape = class extends Shape_default {
    /**
     * Constructs a new polyline shape.
     *
     * @param points Array of <{@link Point} that define the points. This is stored in {@link Shape.points}.
     * @param stroke String that defines the stroke color. Default is 'black'. This is stored in {@link Shape.stroke}.
     * @param strokeWidth Optional integer that defines the stroke width. Default is 1. This is stored in {@link Shape.strokeWidth}.
     */
    constructor(points, stroke, strokeWidth = 1) {
      super();
      this.points = points;
      this.stroke = stroke;
      this.strokeWidth = strokeWidth;
    }
    /**
     * Returns 0.
     */
    getRotation() {
      return 0;
    }
    /**
     * Returns 0.
     */
    getShapeRotation() {
      return 0;
    }
    /**
     * Returns false.
     */
    isPaintBoundsInverted() {
      return false;
    }
    /**
     * Paints the line shape.
     */
    paintEdgeShape(c, pts) {
      var _a2;
      const prev = c.pointerEventsValue;
      c.pointerEventsValue = "stroke";
      if (!((_a2 = this.style) == null ? void 0 : _a2.curved)) {
        this.paintLine(c, pts, this.isRounded);
      } else {
        this.paintCurvedLine(c, pts);
      }
      c.pointerEventsValue = prev;
    }
    /**
     * Paints the line shape.
     */
    paintLine(c, pts, rounded) {
      c.begin();
      this.addPoints(c, pts, rounded, this.getBaseArcSize(), false);
      c.stroke();
    }
    /**
     * Paints the line shape.
     */
    paintCurvedLine(c, pts) {
      c.begin();
      const pt = pts[0];
      const n = pts.length;
      c.moveTo(pt.x, pt.y);
      for (let i = 1; i < n - 2; i += 1) {
        const p02 = pts[i];
        const p12 = pts[i + 1];
        const ix = (p02.x + p12.x) / 2;
        const iy = (p02.y + p12.y) / 2;
        c.quadTo(p02.x, p02.y, ix, iy);
      }
      const p0 = pts[n - 2];
      const p1 = pts[n - 1];
      c.quadTo(p0.x, p0.y, p1.x, p1.y);
      c.stroke();
    }
  };
  var PolylineShape_default = PolylineShape;

  // node_modules/@maxgraph/core/lib/esm/view/mixin/PageBreaksMixin.js
  var PageBreaksMixin = {
    horizontalPageBreaks: null,
    verticalPageBreaks: null,
    updatePageBreaks(visible, _width, _height) {
      const { scale, translate: tr } = this.getView();
      const fmt = this.getPageFormat();
      const ps = scale * this.getPageScale();
      const bounds = new Rectangle_default(0, 0, fmt.width * ps, fmt.height * ps);
      const gb = Rectangle_default.fromRectangle(this.getGraphBounds());
      gb.width = Math.max(1, gb.width);
      gb.height = Math.max(1, gb.height);
      bounds.x = Math.floor((gb.x - tr.x * scale) / bounds.width) * bounds.width + tr.x * scale;
      bounds.y = Math.floor((gb.y - tr.y * scale) / bounds.height) * bounds.height + tr.y * scale;
      gb.width = Math.ceil((gb.width + (gb.x - bounds.x)) / bounds.width) * bounds.width;
      gb.height = Math.ceil((gb.height + (gb.y - bounds.y)) / bounds.height) * bounds.height;
      visible = visible && Math.min(bounds.width, bounds.height) > this.getMinPageBreakDist();
      const horizontalCount = visible ? Math.ceil(gb.height / bounds.height) + 1 : 0;
      const verticalCount = visible ? Math.ceil(gb.width / bounds.width) + 1 : 0;
      const right = (verticalCount - 1) * bounds.width;
      const bottom = (horizontalCount - 1) * bounds.height;
      if (this.horizontalPageBreaks == null && horizontalCount > 0) {
        this.horizontalPageBreaks = [];
      }
      if (this.verticalPageBreaks == null && verticalCount > 0) {
        this.verticalPageBreaks = [];
      }
      const drawPageBreaks = (breaks) => {
        if (!breaks) {
          return;
        }
        const count = breaks === this.horizontalPageBreaks ? horizontalCount : verticalCount;
        for (let i = 0; i <= count; i += 1) {
          const pts = breaks === this.horizontalPageBreaks ? [
            new Point_default(Math.round(bounds.x), Math.round(bounds.y + i * bounds.height)),
            new Point_default(Math.round(bounds.x + right), Math.round(bounds.y + i * bounds.height))
          ] : [
            new Point_default(Math.round(bounds.x + i * bounds.width), Math.round(bounds.y)),
            new Point_default(Math.round(bounds.x + i * bounds.width), Math.round(bounds.y + bottom))
          ];
          if (breaks[i] != null) {
            breaks[i].points = pts;
            breaks[i].redraw();
          } else {
            const pageBreak = new PolylineShape_default(pts, this.getPageBreakColor());
            pageBreak.dialect = this.getDialect();
            pageBreak.pointerEvents = false;
            pageBreak.isDashed = this.isPageBreakDashed();
            pageBreak.init(this.getView().backgroundPane);
            pageBreak.redraw();
            breaks[i] = pageBreak;
          }
        }
        for (let i = count; i < breaks.length; i += 1) {
          breaks[i].destroy();
        }
        breaks.splice(count, breaks.length - count);
      };
      drawPageBreaks(this.horizontalPageBreaks);
      drawPageBreaks(this.verticalPageBreaks);
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/mixin/PanningMixin.js
  var PanningMixin = {
    shiftPreview1: null,
    shiftPreview2: null,
    useScrollbarsForPanning: true,
    isUseScrollbarsForPanning() {
      return this.useScrollbarsForPanning;
    },
    timerAutoScroll: false,
    isTimerAutoScroll() {
      return this.timerAutoScroll;
    },
    allowAutoPanning: false,
    isAllowAutoPanning() {
      return this.allowAutoPanning;
    },
    panDx: 0,
    getPanDx() {
      return this.panDx;
    },
    setPanDx(dx) {
      this.panDx = dx;
    },
    panDy: 0,
    getPanDy() {
      return this.panDy;
    },
    setPanDy(dy) {
      this.panDy = dy;
    },
    panGraph(dx, dy) {
      const container = this.getContainer();
      if (this.useScrollbarsForPanning && hasScrollbars(container)) {
        container.scrollLeft = -dx;
        container.scrollTop = -dy;
      } else {
        const canvas = this.getView().getCanvas();
        if (dx === 0 && dy === 0) {
          canvas.removeAttribute("transform");
          if (this.shiftPreview1) {
            let child = this.shiftPreview1.firstChild;
            while (child) {
              const next = child.nextSibling;
              container.appendChild(child);
              child = next;
            }
            if (this.shiftPreview1.parentNode) {
              this.shiftPreview1.parentNode.removeChild(this.shiftPreview1);
            }
            this.shiftPreview1 = null;
            container.appendChild(canvas.parentNode);
            const shiftPreview2 = this.shiftPreview2;
            child = shiftPreview2.firstChild;
            while (child) {
              const next = child.nextSibling;
              container.appendChild(child);
              child = next;
            }
            if (shiftPreview2.parentNode) {
              shiftPreview2.parentNode.removeChild(shiftPreview2);
            }
            this.shiftPreview2 = null;
          }
        } else {
          canvas.setAttribute("transform", `translate(${dx},${dy})`);
          if (!this.shiftPreview1) {
            this.shiftPreview1 = document.createElement("div");
            this.shiftPreview1.style.position = "absolute";
            this.shiftPreview1.style.overflow = "visible";
            this.shiftPreview2 = document.createElement("div");
            this.shiftPreview2.style.position = "absolute";
            this.shiftPreview2.style.overflow = "visible";
            let current = this.shiftPreview1;
            let child = container.firstChild;
            while (child) {
              const next = child.nextSibling;
              if (child !== canvas.parentNode) {
                current.appendChild(child);
              } else {
                current = this.shiftPreview2;
              }
              child = next;
            }
            if (this.shiftPreview1.firstChild) {
              container.insertBefore(this.shiftPreview1, canvas.parentNode);
            }
            if (this.shiftPreview2.firstChild) {
              container.appendChild(this.shiftPreview2);
            }
          }
          this.shiftPreview1.style.left = `${dx}px`;
          this.shiftPreview1.style.top = `${dy}px`;
          if (this.shiftPreview2) {
            this.shiftPreview2.style.left = `${dx}px`;
            this.shiftPreview2.style.top = `${dy}px`;
          }
        }
        this.panDx = dx;
        this.panDy = dy;
        this.fireEvent(new EventObject_default(InternalEvent_default.PAN));
      }
    },
    scrollCellToVisible(cell, center = false) {
      const x = -this.getView().translate.x;
      const y = -this.getView().translate.y;
      const state = this.getView().getState(cell);
      if (state) {
        const bounds = new Rectangle_default(x + state.x, y + state.y, state.width, state.height);
        if (center && this.getContainer()) {
          const w = this.getContainer().clientWidth;
          const h = this.getContainer().clientHeight;
          bounds.x = bounds.getCenterX() - w / 2;
          bounds.width = w;
          bounds.y = bounds.getCenterY() - h / 2;
          bounds.height = h;
        }
        const tr = new Point_default(this.getView().translate.x, this.getView().translate.y);
        if (this.scrollRectToVisible(bounds)) {
          const tr2 = new Point_default(this.getView().translate.x, this.getView().translate.y);
          this.getView().translate.x = tr.x;
          this.getView().translate.y = tr.y;
          this.getView().setTranslate(tr2.x, tr2.y);
        }
      }
    },
    scrollRectToVisible(rect) {
      let isChanged = false;
      const container = this.getContainer();
      const w = container.offsetWidth;
      const h = container.offsetHeight;
      const widthLimit = Math.min(w, rect.width);
      const heightLimit = Math.min(h, rect.height);
      if (hasScrollbars(container)) {
        rect.x += this.getView().translate.x;
        rect.y += this.getView().translate.y;
        let dx = container.scrollLeft - rect.x;
        const ddx = Math.max(dx - container.scrollLeft, 0);
        if (dx > 0) {
          container.scrollLeft -= dx + 2;
        } else {
          dx = rect.x + widthLimit - container.scrollLeft - container.clientWidth;
          if (dx > 0) {
            container.scrollLeft += dx + 2;
          }
        }
        let dy = container.scrollTop - rect.y;
        const ddy = Math.max(0, dy - container.scrollTop);
        if (dy > 0) {
          container.scrollTop -= dy + 2;
        } else {
          dy = rect.y + heightLimit - container.scrollTop - container.clientHeight;
          if (dy > 0) {
            container.scrollTop += dy + 2;
          }
        }
        if (!this.useScrollbarsForPanning && (ddx != 0 || ddy != 0)) {
          this.getView().setTranslate(ddx, ddy);
        }
      } else {
        const x = -this.getView().translate.x;
        const y = -this.getView().translate.y;
        const s = this.getView().scale;
        if (rect.x + widthLimit > x + w) {
          this.getView().translate.x -= (rect.x + widthLimit - w - x) / s;
          isChanged = true;
        }
        if (rect.y + heightLimit > y + h) {
          this.getView().translate.y -= (rect.y + heightLimit - h - y) / s;
          isChanged = true;
        }
        if (rect.x < x) {
          this.getView().translate.x += (x - rect.x) / s;
          isChanged = true;
        }
        if (rect.y < y) {
          this.getView().translate.y += (y - rect.y) / s;
          isChanged = true;
        }
        if (isChanged) {
          this.getView().refresh();
          const selectionCellsHandler = this.getPlugin("SelectionCellsHandler");
          if (selectionCellsHandler) {
            selectionCellsHandler.refresh();
          }
        }
      }
      return isChanged;
    },
    setPanning(enabled) {
      const panningHandler = this.getPlugin("PanningHandler");
      panningHandler && (panningHandler.panningEnabled = enabled);
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/mixin/PortsMixin.js
  var PortsMixin = {
    portsEnabled: true,
    isPort(cell) {
      return false;
    },
    getTerminalForPort(cell, _source = false) {
      return cell.getParent();
    },
    isPortsEnabled() {
      return this.portsEnabled;
    },
    setPortsEnabled(value) {
      this.portsEnabled = value;
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/mixin/SelectionMixin.js
  var SelectionMixin = {
    // Always non-null at runtime: initialized in {@link AbstractGraph.initializeCollaborators}
    // via {@link setSelectionModel}, which shadows the `null` set on the prototype by the mixin.
    selectionModel: null,
    getSelectionModel() {
      return this.selectionModel;
    },
    setSelectionModel(selectionModel) {
      this.selectionModel = selectionModel;
    },
    /*****************************************************************************
     * Selection
     *****************************************************************************/
    isCellSelected(cell) {
      return this.selectionModel.isSelected(cell);
    },
    isSelectionEmpty() {
      return this.selectionModel.isEmpty();
    },
    clearSelection() {
      this.selectionModel.clear();
    },
    getSelectionCount() {
      return this.selectionModel.cells.length;
    },
    getSelectionCell() {
      return this.selectionModel.cells[0];
    },
    getSelectionCells() {
      return this.selectionModel.cells.slice();
    },
    setSelectionCell(cell) {
      this.selectionModel.setCell(cell);
    },
    setSelectionCells(cells) {
      this.selectionModel.setCells(cells);
    },
    addSelectionCell(cell) {
      this.selectionModel.addCell(cell);
    },
    addSelectionCells(cells) {
      this.selectionModel.addCells(cells);
    },
    removeSelectionCell(cell) {
      this.selectionModel.removeCell(cell);
    },
    removeSelectionCells(cells) {
      this.selectionModel.removeCells(cells);
    },
    selectRegion(rect, evt) {
      const cells = this.getCells(rect.x, rect.y, rect.width, rect.height);
      this.selectCellsForEvent(cells, evt);
      return cells;
    },
    selectNextCell() {
      this.selectCell(true);
    },
    selectPreviousCell() {
      this.selectCell();
    },
    selectParentCell() {
      this.selectCell(false, true);
    },
    selectChildCell() {
      this.selectCell(false, false, true);
    },
    selectCell(isNext = false, isParent = false, isChild = false) {
      const cell = this.selectionModel.cells.length > 0 ? this.selectionModel.cells[0] : null;
      if (this.selectionModel.cells.length > 1) {
        this.selectionModel.clear();
      }
      const parent = cell ? cell.getParent() : this.getDefaultParent();
      const childCount = parent.getChildCount();
      if (!cell && childCount > 0) {
        const child = parent.getChildAt(0);
        this.setSelectionCell(child);
      } else if (parent && (!cell || isParent) && this.getView().getState(parent) && parent.getGeometry()) {
        if (this.getCurrentRoot() !== parent) {
          this.setSelectionCell(parent);
        }
      } else if (cell && isChild) {
        const tmp = cell.getChildCount();
        if (tmp > 0) {
          const child = cell.getChildAt(0);
          this.setSelectionCell(child);
        }
      } else if (childCount > 0) {
        let i = parent.getIndex(cell);
        if (isNext) {
          i++;
          const child = parent.getChildAt(i % childCount);
          this.setSelectionCell(child);
        } else {
          i--;
          const index = i < 0 ? childCount - 1 : i;
          const child = parent.getChildAt(index);
          this.setSelectionCell(child);
        }
      }
    },
    selectAll(parent, descendants = false) {
      parent = parent != null ? parent : this.getDefaultParent();
      const cells = descendants ? parent.filterDescendants((cell) => {
        return cell !== parent && !!this.getView().getState(cell);
      }) : parent.getChildren();
      this.setSelectionCells(cells);
    },
    selectVertices(parent, selectGroups = false) {
      this.selectCells(true, false, parent, selectGroups);
    },
    selectEdges(parent) {
      this.selectCells(false, true, parent);
    },
    selectCells(vertices = false, edges = false, parent, selectGroups = false) {
      parent = parent != null ? parent : this.getDefaultParent();
      const filter = (cell) => {
        const p = cell.getParent();
        return !!this.getView().getState(cell) && ((selectGroups || cell.getChildCount() === 0) && cell.isVertex() && vertices && p && !p.isEdge() || cell.isEdge() && edges);
      };
      const cells = parent.filterDescendants(filter);
      this.setSelectionCells(cells);
    },
    selectCellForEvent(cell, evt) {
      const isSelected = this.isCellSelected(cell);
      if (this.isToggleEvent(evt)) {
        if (isSelected) {
          this.removeSelectionCell(cell);
        } else {
          this.addSelectionCell(cell);
        }
      } else if (!isSelected || this.getSelectionCount() !== 1) {
        this.setSelectionCell(cell);
      }
    },
    selectCellsForEvent(cells, evt) {
      if (this.isToggleEvent(evt)) {
        this.addSelectionCells(cells);
      } else {
        this.setSelectionCells(cells);
      }
    },
    isSiblingSelected(cell) {
      const parent = cell.getParent();
      const childCount = parent.getChildCount();
      for (let i = 0; i < childCount; i += 1) {
        const child = parent.getChildAt(i);
        if (cell !== child && this.isCellSelected(child)) {
          return true;
        }
      }
      return false;
    },
    /*****************************************************************************
     * Selection state
     *****************************************************************************/
    getSelectionCellsForChanges(changes, ignoreFn = null) {
      const coveredElements = /* @__PURE__ */ new Map();
      const cells = [];
      const addCell = (cell) => {
        if (!coveredElements.has(cell) && this.getDataModel().contains(cell)) {
          if (cell.isEdge() || cell.isVertex()) {
            coveredElements.set(cell, true);
            cells.push(cell);
          } else {
            const childCount = cell.getChildCount();
            for (let i = 0; i < childCount; i += 1) {
              addCell(cell.getChildAt(i));
            }
          }
        }
      };
      for (let i = 0; i < changes.length; i += 1) {
        const change = changes[i];
        if (change.constructor !== RootChange_default && (!ignoreFn || !ignoreFn(change))) {
          let cell = null;
          if (change instanceof ChildChange_default) {
            cell = change.child;
          } else if (change.cell && change.cell instanceof Cell_default) {
            cell = change.cell;
          }
          if (cell) {
            addCell(cell);
          }
        }
      }
      return cells;
    },
    updateSelection() {
      const cells = this.getSelectionCells();
      const removed = [];
      for (const cell of cells) {
        if (!this.getDataModel().contains(cell) || !cell.isVisible()) {
          removed.push(cell);
        } else {
          let par = cell.getParent();
          while (par && par !== this.getView().currentRoot) {
            if (par.isCollapsed() || !par.isVisible()) {
              removed.push(cell);
              break;
            }
            par = par.getParent();
          }
        }
      }
      this.removeSelectionCells(removed);
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/mixin/SnapMixin.js
  var SnapMixin = {
    snapTolerance: 0,
    getSnapTolerance() {
      return this.snapTolerance;
    },
    gridSize: 10,
    gridEnabled: true,
    snap(value) {
      if (this.gridEnabled) {
        value = Math.round(value / this.gridSize) * this.gridSize;
      }
      return value;
    },
    snapDelta(delta, bounds, ignoreGrid = false, ignoreHorizontal = false, ignoreVertical = false) {
      const t = this.getView().translate;
      const s = this.getView().scale;
      if (!ignoreGrid && this.gridEnabled) {
        const tol = this.gridSize * s * 0.5;
        if (!ignoreHorizontal) {
          const tx = bounds.x - (this.snap(bounds.x / s - t.x) + t.x) * s;
          if (Math.abs(delta.x - tx) < tol) {
            delta.x = 0;
          } else {
            delta.x = this.snap(delta.x / s) * s - tx;
          }
        }
        if (!ignoreVertical) {
          const ty = bounds.y - (this.snap(bounds.y / s - t.y) + t.y) * s;
          if (Math.abs(delta.y - ty) < tol) {
            delta.y = 0;
          } else {
            delta.y = this.snap(delta.y / s) * s - ty;
          }
        }
      } else {
        const tol = 0.5 * s;
        if (!ignoreHorizontal) {
          const tx = bounds.x - (Math.round(bounds.x / s - t.x) + t.x) * s;
          if (Math.abs(delta.x - tx) < tol) {
            delta.x = 0;
          } else {
            delta.x = Math.round(delta.x / s) * s - tx;
          }
        }
        if (!ignoreVertical) {
          const ty = bounds.y - (Math.round(bounds.y / s - t.y) + t.y) * s;
          if (Math.abs(delta.y - ty) < tol) {
            delta.y = 0;
          } else {
            delta.y = Math.round(delta.y / s) * s - ty;
          }
        }
      }
      return delta;
    },
    isGridEnabled() {
      return this.gridEnabled;
    },
    setGridEnabled(value) {
      this.gridEnabled = value;
    },
    getGridSize() {
      return this.gridSize;
    },
    setGridSize(value) {
      this.gridSize = value;
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/mixin/SwimlaneMixin.js
  var SwimlaneMixin = {
    swimlaneSelectionEnabled: true,
    swimlaneNesting: true,
    swimlaneIndicatorColorAttribute: "fillColor",
    getSwimlane(cell = null) {
      while (cell && !this.isSwimlane(cell)) {
        cell = cell.getParent();
      }
      return cell;
    },
    getSwimlaneAt(x, y, parent) {
      if (!parent) {
        parent = this.getCurrentRoot();
        if (!parent) {
          parent = this.getDataModel().getRoot();
        }
      }
      if (parent) {
        const childCount = parent.getChildCount();
        for (let i = 0; i < childCount; i += 1) {
          const child = parent.getChildAt(i);
          if (child) {
            const result = this.getSwimlaneAt(x, y, child);
            if (result != null) {
              return result;
            }
            if (child.isVisible() && this.isSwimlane(child)) {
              const state = this.getView().getState(child);
              if (state && this.intersects(state, x, y)) {
                return child;
              }
            }
          }
        }
      }
      return null;
    },
    hitsSwimlaneContent(swimlane, x, y) {
      const state = this.getView().getState(swimlane);
      const size = this.getStartSize(swimlane);
      if (state) {
        const scale = this.getView().getScale();
        x -= state.x;
        y -= state.y;
        if (size.width > 0 && x > 0 && x > size.width * scale) {
          return true;
        }
        if (size.height > 0 && y > 0 && y > size.height * scale) {
          return true;
        }
      }
      return false;
    },
    getStartSize(swimlane, ignoreState = false) {
      var _a2, _b;
      const result = new Rectangle_default();
      const style = this.getCurrentCellStyle(swimlane, ignoreState);
      const size = (_a2 = style.startSize) != null ? _a2 : StyleDefaultsConfig.startSize;
      if ((_b = style.horizontal) != null ? _b : true) {
        result.height = size;
      } else {
        result.width = size;
      }
      return result;
    },
    getSwimlaneDirection(style) {
      var _a2, _b;
      const dir = (_a2 = style.direction) != null ? _a2 : "east";
      const flipH = style.flipH;
      const flipV = style.flipV;
      const h = (_b = style.horizontal) != null ? _b : true;
      let n = h ? 0 : 3;
      switch (dir) {
        case "north": {
          n--;
          break;
        }
        case "west": {
          n += 2;
          break;
        }
        case "south": {
          n += 1;
          break;
        }
      }
      const _mod = mod(n, 2);
      if (flipH && _mod === 1) {
        n += 2;
      }
      if (flipV && _mod === 0) {
        n += 2;
      }
      return ["north", "east", "south", "west"][mod(n, 4)];
    },
    getActualStartSize(swimlane, ignoreState = false) {
      var _a2;
      const result = new Rectangle_default();
      if (this.isSwimlane(swimlane, ignoreState)) {
        const style = this.getCurrentCellStyle(swimlane, ignoreState);
        const size = (_a2 = style.startSize) != null ? _a2 : StyleDefaultsConfig.startSize;
        const dir = this.getSwimlaneDirection(style);
        switch (dir) {
          case "north": {
            result.y = size;
            break;
          }
          case "west": {
            result.x = size;
            break;
          }
          case "south": {
            result.height = size;
            break;
          }
          default: {
            result.width = size;
          }
        }
      }
      return result;
    },
    isSwimlane(cell, ignoreState = false) {
      if (cell && cell.getParent() !== this.getDataModel().getRoot() && !cell.isEdge()) {
        return this.getCurrentCellStyle(cell, ignoreState).shape === "swimlane";
      }
      return false;
    },
    isValidDropTarget(cell, cells, evt) {
      return cell && (this.isSplitEnabled() && this.isSplitTarget(cell, cells, evt) || !cell.isEdge() && (this.isSwimlane(cell) || cell.getChildCount() > 0 && !cell.isCollapsed()));
    },
    getDropTarget(cells, evt, cell = null, clone2 = false) {
      if (!this.isSwimlaneNesting()) {
        for (let i = 0; i < cells.length; i += 1) {
          if (this.isSwimlane(cells[i])) {
            return null;
          }
        }
      }
      const pt = convertPoint(this.getContainer(), getClientX(evt), getClientY(evt));
      pt.x -= this.getPanDx();
      pt.y -= this.getPanDy();
      const swimlane = this.getSwimlaneAt(pt.x, pt.y);
      if (!cell) {
        cell = swimlane;
      } else if (swimlane) {
        let tmp = swimlane.getParent();
        while (tmp && this.isSwimlane(tmp) && tmp !== cell) {
          tmp = tmp.getParent();
        }
        if (tmp === cell) {
          cell = swimlane;
        }
      }
      while (cell && !this.isValidDropTarget(cell, cells, evt) && !this.getDataModel().isLayer(cell)) {
        cell = cell.getParent();
      }
      let parentCell = cell;
      if (!clone2) {
        while (parentCell && !cells.includes(parentCell)) {
          parentCell = parentCell.getParent();
        }
      }
      return !this.getDataModel().isLayer(cell) && !parentCell ? cell : null;
    },
    isSwimlaneNesting() {
      return this.swimlaneNesting;
    },
    setSwimlaneNesting(value) {
      this.swimlaneNesting = value;
    },
    isSwimlaneSelectionEnabled() {
      return this.swimlaneSelectionEnabled;
    },
    setSwimlaneSelectionEnabled(value) {
      this.swimlaneSelectionEnabled = value;
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/mixin/TerminalMixin.js
  var TerminalMixin = {
    isTerminalPointMovable(cell, source) {
      return true;
    },
    getOpposites(edges, terminal = null, includeSources = true, includeTargets = true) {
      const terminals = [];
      const coveredEntries = /* @__PURE__ */ new Map();
      for (let i = 0; i < edges.length; i += 1) {
        const state = this.getView().getState(edges[i]);
        const source = state ? state.getVisibleTerminal(true) : this.getView().getVisibleTerminal(edges[i], true);
        const target = state ? state.getVisibleTerminal(false) : this.getView().getVisibleTerminal(edges[i], false);
        if (source === terminal && target && target !== terminal && includeTargets) {
          if (!coveredEntries.has(target)) {
            coveredEntries.set(target, true);
            terminals.push(target);
          }
        } else if (target === terminal && source && source !== terminal && includeSources) {
          if (!coveredEntries.has(source)) {
            coveredEntries.set(source, true);
            terminals.push(source);
          }
        }
      }
      return terminals;
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/mixin/ValidationMixin.js
  var ValidationMixin = {
    validationAlert(message) {
      alert(message);
    },
    isEdgeValid(edge, source, target) {
      return !this.getEdgeValidationError(edge, source, target);
    },
    getEdgeValidationError(edge = null, source = null, target = null) {
      if (edge && !this.isAllowDanglingEdges() && (!source || !target)) {
        return "";
      }
      if (edge && !edge.getTerminal(true) && !edge.getTerminal(false)) {
        return null;
      }
      if (!this.isAllowLoops() && source === target && source) {
        return "";
      }
      if (!this.isValidConnection(source, target)) {
        return "";
      }
      if (source && target) {
        let error = "";
        if (!this.isMultigraph()) {
          const tmp = this.getDataModel().getEdgesBetween(source, target, true);
          if (tmp.length > 1 || tmp.length === 1 && tmp[0] !== edge) {
            error += `${translate(this.getAlreadyConnectedResource()) || this.getAlreadyConnectedResource()}
`;
          }
        }
        const sourceOut = source.getDirectedEdgeCount(true, edge);
        const targetIn = target.getDirectedEdgeCount(false, edge);
        for (const multiplicity of this.multiplicities) {
          const err2 = multiplicity.check(
            this,
            // needs to cast to Graph
            edge,
            source,
            target,
            sourceOut,
            targetIn
          );
          if (!isNullish(err2)) {
            error += err2;
          }
        }
        const err = this.validateEdge(edge, source, target);
        if (!isNullish(err)) {
          error += err;
        }
        return error.length > 0 ? error : null;
      }
      return this.isAllowDanglingEdges() ? null : "";
    },
    validateEdge(edge = null, source = null, target = null) {
      return null;
    },
    validateGraph(cell = null, context) {
      cell = cell != null ? cell : this.getDataModel().getRoot();
      if (!cell) {
        return "The root does not exist!";
      }
      context = context != null ? context : {};
      let isValid = true;
      const childCount = cell.getChildCount();
      for (let i = 0; i < childCount; i += 1) {
        const tmp = cell.getChildAt(i);
        let ctx = context;
        if (this.isValidRoot(tmp)) {
          ctx = {};
        }
        const warn = this.validateGraph(tmp, ctx);
        if (warn) {
          this.setCellWarning(tmp, warn.replace(/\n/g, "<br>"));
        } else {
          this.setCellWarning(tmp, null);
        }
        isValid = isValid && warn == null;
      }
      let warning = "";
      if (cell && cell.isCollapsed() && !isValid) {
        warning += `${translate(this.getContainsValidationErrorsResource()) || this.getContainsValidationErrorsResource()}
`;
      }
      if (cell && cell.isEdge()) {
        warning += this.getEdgeValidationError(cell, cell.getTerminal(true), cell.getTerminal(false)) || "";
      } else {
        warning += this.getCellValidationError(cell) || "";
      }
      const err = this.validateCell(cell, context);
      if (!isNullish(err)) {
        warning += err;
      }
      if (cell.getParent() == null) {
        this.getView().validate();
      }
      return warning.length > 0 || !isValid ? warning : null;
    },
    getCellValidationError(cell) {
      const outCount = cell.getDirectedEdgeCount(true);
      const inCount = cell.getDirectedEdgeCount(false);
      const value = cell.getValue();
      let error = "";
      for (let i = 0; i < this.multiplicities.length; i += 1) {
        const rule = this.multiplicities[i];
        if (rule.source && isNode(value, rule.type, rule.attr, rule.value) && (outCount > rule.max || outCount < rule.min)) {
          error += `${rule.countError}
`;
        } else if (!rule.source && isNode(value, rule.type, rule.attr, rule.value) && (inCount > rule.max || inCount < rule.min)) {
          error += `${rule.countError}
`;
        }
      }
      return error.length > 0 ? error : null;
    },
    validateCell(cell, context) {
      return null;
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/mixin/VertexMixin.js
  var VertexMixin = {
    vertexLabelsMovable: false,
    allowNegativeCoordinates: true,
    isAllowNegativeCoordinates() {
      return this.allowNegativeCoordinates;
    },
    setAllowNegativeCoordinates(value) {
      this.allowNegativeCoordinates = value;
    },
    insertVertex(...args) {
      var _a2, _b, _c, _d;
      let parent;
      let id;
      let value;
      let x;
      let y;
      let width;
      let height;
      let style;
      let relative;
      let geometryClass;
      if (args.length === 1 && typeof args[0] === "object") {
        const params = args[0];
        parent = params.parent;
        id = params.id;
        value = params.value;
        x = "x" in params ? params.x : (_a2 = params.position) == null ? void 0 : _a2[0];
        y = "y" in params ? params.y : (_b = params.position) == null ? void 0 : _b[1];
        width = "width" in params ? params.width : (_c = params.size) == null ? void 0 : _c[0];
        height = "height" in params ? params.height : (_d = params.size) == null ? void 0 : _d[1];
        style = params.style;
        relative = params.relative;
        geometryClass = params.geometryClass;
      } else {
        [parent, id, value, x, y, width, height, style, relative, geometryClass] = args;
      }
      const vertex = this.createVertex(parent, id, value, x, y, width, height, style, relative, geometryClass);
      return this.addCell(vertex, parent);
    },
    createVertex(_parent, id, value, x, y, width, height, style, relative = false, geometryClass = Geometry_default) {
      const geometry = new geometryClass(x, y, width, height);
      geometry.relative = relative;
      const vertex = new Cell_default(value, geometry, style);
      vertex.setId(id);
      vertex.setVertex(true);
      vertex.setConnectable(true);
      return vertex;
    },
    getChildVertices(parent) {
      return this.getChildCells(parent, true, false);
    },
    isVertexLabelsMovable() {
      return this.vertexLabelsMovable;
    },
    setVertexLabelsMovable(value) {
      this.vertexLabelsMovable = value;
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/mixin/ZoomMixin.js
  var ZoomMixin = {
    zoomFactor: 1.2,
    keepSelectionVisibleOnZoom: false,
    centerZoom: true,
    zoomIn() {
      this.zoom(this.zoomFactor);
    },
    zoomOut() {
      this.zoom(1 / this.zoomFactor);
    },
    zoomActual() {
      if (this.getView().scale === 1) {
        this.getView().setTranslate(0, 0);
      } else {
        this.getView().translate.x = 0;
        this.getView().translate.y = 0;
        this.getView().setScale(1);
      }
    },
    zoomTo(scale, center = false) {
      this.zoom(scale / this.getView().scale, center);
    },
    zoom(factor, center) {
      center = center != null ? center : this.centerZoom;
      const scale = Math.round(this.getView().scale * factor * 100) / 100;
      const state = this.getView().getState(this.getSelectionCell());
      const container = this.getContainer();
      factor = scale / this.getView().scale;
      if (this.keepSelectionVisibleOnZoom && state != null) {
        const rect = new Rectangle_default(state.x * factor, state.y * factor, state.width * factor, state.height * factor);
        this.getView().scale = scale;
        if (!this.scrollRectToVisible(rect)) {
          this.getView().revalidate();
          this.getView().setScale(scale);
        }
      } else {
        const _hasScrollbars = hasScrollbars(this.getContainer());
        if (center && !_hasScrollbars) {
          let dx = container.offsetWidth;
          let dy = container.offsetHeight;
          if (factor > 1) {
            const f = (factor - 1) / (scale * 2);
            dx *= -f;
            dy *= -f;
          } else {
            const f = (1 / factor - 1) / (this.getView().scale * 2);
            dx *= f;
            dy *= f;
          }
          this.getView().scaleAndTranslate(scale, this.getView().translate.x + dx, this.getView().translate.y + dy);
        } else {
          const tx = this.getView().translate.x;
          const ty = this.getView().translate.y;
          const sl = container.scrollLeft;
          const st = container.scrollTop;
          this.getView().setScale(scale);
          if (_hasScrollbars) {
            let dx = 0;
            let dy = 0;
            if (center) {
              dx = container.offsetWidth * (factor - 1) / 2;
              dy = container.offsetHeight * (factor - 1) / 2;
            }
            container.scrollLeft = (this.getView().translate.x - tx) * this.getView().scale + Math.round(sl * factor + dx);
            container.scrollTop = (this.getView().translate.y - ty) * this.getView().scale + Math.round(st * factor + dy);
          }
        }
      }
    },
    zoomToRect(rect) {
      const container = this.getContainer();
      const scaleX = container.clientWidth / rect.width;
      const scaleY = container.clientHeight / rect.height;
      const aspectFactor = scaleX / scaleY;
      rect.x = Math.max(0, rect.x);
      rect.y = Math.max(0, rect.y);
      let rectRight = Math.min(container.scrollWidth, rect.x + rect.width);
      let rectBottom = Math.min(container.scrollHeight, rect.y + rect.height);
      rect.width = rectRight - rect.x;
      rect.height = rectBottom - rect.y;
      if (aspectFactor < 1) {
        const newHeight = rect.height / aspectFactor;
        const deltaHeightBuffer = (newHeight - rect.height) / 2;
        rect.height = newHeight;
        const upperBuffer = Math.min(rect.y, deltaHeightBuffer);
        rect.y -= upperBuffer;
        rectBottom = Math.min(container.scrollHeight, rect.y + rect.height);
        rect.height = rectBottom - rect.y;
      } else {
        const newWidth = rect.width * aspectFactor;
        const deltaWidthBuffer = (newWidth - rect.width) / 2;
        rect.width = newWidth;
        const leftBuffer = Math.min(rect.x, deltaWidthBuffer);
        rect.x -= leftBuffer;
        rectRight = Math.min(container.scrollWidth, rect.x + rect.width);
        rect.width = rectRight - rect.x;
      }
      const scale = container.clientWidth / rect.width;
      const newScale = this.getView().scale * scale;
      if (!hasScrollbars(this.getContainer())) {
        this.getView().scaleAndTranslate(newScale, this.getView().translate.x - rect.x / this.getView().scale, this.getView().translate.y - rect.y / this.getView().scale);
      } else {
        this.getView().setScale(newScale);
        container.scrollLeft = Math.round(rect.x * scale);
        container.scrollTop = Math.round(rect.y * scale);
      }
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/mixin/_graph-mixins-apply.js
  var applyGraphMixins = (target) => {
    const mixIntoGraph = mixInto(target);
    for (const mixin of [
      CellsMixin,
      ConnectionsMixin,
      DragDropMixin,
      EdgeMixin,
      EditingMixin,
      EventsMixin,
      FoldingMixin,
      GroupingMixin,
      LabelMixin,
      OrderMixin,
      PageBreaksMixin,
      OverlaysMixin,
      PanningMixin,
      PortsMixin,
      SelectionMixin,
      SnapMixin,
      SwimlaneMixin,
      TerminalMixin,
      ValidationMixin,
      VertexMixin,
      ZoomMixin
    ]) {
      mixIntoGraph(mixin);
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/AbstractGraph.js
  var AbstractGraph = class extends EventSource_default {
    // ===================================================================================================================
    // Group: "Create Class Instance" factory functions.
    // These can be overridden in subclasses to allow the Graph to instantiate user-defined implementations with custom behavior.
    // Notice that the methods will be moved as part of https://github.com/maxGraph/maxGraph/issues/762
    // ===================================================================================================================
    /**
     * Hooks to create a new {@link EdgeHandler} for the given {@link CellState}.
     *
     * @param state {@link CellState} to create the handler for.
     */
    createEdgeHandlerInstance(state) {
      return new EdgeHandler_default(state);
    }
    /**
     * Hooks to create a new {@link EdgeSegmentHandler} for the given {@link CellState}.
     *
     * @param state {@link CellState} to create the handler for.
     */
    createEdgeSegmentHandler(state) {
      return new EdgeSegmentHandler_default(state);
    }
    /**
     * Hooks to create a new {@link ElbowEdgeHandler} for the given {@link CellState}.
     *
     * @param state {@link CellState} to create the handler for.
     */
    createElbowEdgeHandler(state) {
      return new ElbowEdgeHandler_default(state);
    }
    /**
     * Hooks to create a new {@link VertexHandler} for the given {@link CellState}.
     *
     * @param state {@link CellState} to create the handler for.
     */
    createVertexHandler(state) {
      return new VertexHandler_default(state);
    }
    // ===================================================================================================================
    // Group: Main graph constructor and functions
    // ===================================================================================================================
    /**
     * Convenient hook method that can be used to register global styles and shapes using the related global registries.
     *
     * While registration can also be done outside of this class (as it applies globally),
     * implementing it here makes the registration process transparent to the caller of this class.
     *
     * Subclasses can override this method to register custom defaults.
     */
    registerDefaults() {
    }
    constructor(options) {
      var _a2, _b;
      super();
      this.destroyed = false;
      this.graphModelChangeListener = null;
      this.paintBackground = null;
      this.isConstrainedMoving = false;
      this.alternateEdgeStyle = {};
      this.cells = [];
      this.mouseListeners = [];
      this.multiplicities = [];
      this.options = {
        foldingEnabled: true,
        collapsedImage: new ImageBox_default(`${Client_default.imageBasePath}/collapsed.gif`, 9, 9),
        expandedImage: new ImageBox_default(`${Client_default.imageBasePath}/expanded.gif`, 9, 9),
        collapseToPreferredSize: true
      };
      this.plugins = /* @__PURE__ */ new Map();
      this.renderHint = null;
      this.dialect = "svg";
      this.defaultOverlap = 0.5;
      this.defaultParent = null;
      this.backgroundImage = null;
      this.pageVisible = false;
      this.pageBreaksVisible = false;
      this.pageBreakColor = "gray";
      this.pageBreakDashed = true;
      this.minPageBreakDist = 20;
      this.preferPageSize = false;
      this.pageFormat = new Rectangle_default(...PAGE_FORMAT_A4_PORTRAIT);
      this.pageScale = 1.5;
      this.enabled = true;
      this.exportEnabled = true;
      this.importEnabled = true;
      this.ignoreScrollbars = false;
      this.translateToScrollPosition = false;
      this.maximumGraphBounds = null;
      this.minimumGraphSize = null;
      this.minimumContainerSize = null;
      this.maximumContainerSize = null;
      this.resizeContainer = false;
      this.border = 0;
      this.keepEdgesInForeground = false;
      this.keepEdgesInBackground = false;
      this.recursiveResize = false;
      this.resetViewOnRootChange = true;
      this.allowLoops = false;
      this.defaultLoopStyle = edge_exports.Loop;
      this.multigraph = true;
      this.warningImage = new ImageBox_default(`${Client_default.imageBasePath}/warning${Client_default.IS_MAC ? ".png" : ".gif"}`, 16, 16);
      this.alreadyConnectedResource = isI18nEnabled() ? "alreadyConnected" : "";
      this.containsValidationErrorsResource = isI18nEnabled() ? "containsValidationErrors" : "";
      this.getContainer = () => this.container;
      this.getPlugin = (id) => this.plugins.get(id);
      this.getCellRenderer = () => this.cellRenderer;
      this.getDialect = () => this.dialect;
      this.isPageVisible = () => this.pageVisible;
      this.isPageBreaksVisible = () => this.pageBreaksVisible;
      this.getPageBreakColor = () => this.pageBreakColor;
      this.isPageBreakDashed = () => this.pageBreakDashed;
      this.getMinPageBreakDist = () => this.minPageBreakDist;
      this.isPreferPageSize = () => this.preferPageSize;
      this.getPageFormat = () => this.pageFormat;
      this.getPageScale = () => this.pageScale;
      this.isExportEnabled = () => this.exportEnabled;
      this.isImportEnabled = () => this.importEnabled;
      this.isIgnoreScrollbars = () => this.ignoreScrollbars;
      this.isTranslateToScrollPosition = () => this.translateToScrollPosition;
      this.getMinimumGraphSize = () => this.minimumGraphSize;
      this.setMinimumGraphSize = (size) => this.minimumGraphSize = size;
      this.getMinimumContainerSize = () => this.minimumContainerSize;
      this.setMinimumContainerSize = (size) => this.minimumContainerSize = size;
      this.getAlreadyConnectedResource = () => this.alreadyConnectedResource;
      this.getContainsValidationErrorsResource = () => this.containsValidationErrorsResource;
      this.registerDefaults();
      this.container = (_a2 = options == null ? void 0 : options.container) != null ? _a2 : document.createElement("div");
      this.initializeCollaborators(options);
      this.graphModelChangeListener = (_sender, evt) => {
        this.graphModelChanged(evt.getProperty("edit").changes);
      };
      this.getDataModel().addListener(InternalEvent_default.CHANGE, this.graphModelChangeListener);
      this.view.init();
      this.sizeDidChange();
      (_b = options == null ? void 0 : options.plugins) == null ? void 0 : _b.forEach((p) => this.plugins.set(p.pluginId, new p(this)));
      this.view.revalidate();
    }
    getWarningImage() {
      return this.warningImage;
    }
    setTooltips(enabled) {
      const tooltipHandler = this.getPlugin("TooltipHandler");
      tooltipHandler == null ? void 0 : tooltipHandler.setEnabled(enabled);
    }
    /**
     * Updates the model in a transaction.
     *
     * @param fn the update to be performed in the transaction.
     *
     * @see {@link GraphDataModel.batchUpdate}
     */
    batchUpdate(fn) {
      this.getDataModel().batchUpdate(fn);
    }
    /**
     * Returns the {@link GraphDataModel} that contains the cells.
     */
    getDataModel() {
      return this.model;
    }
    /**
     * Returns the {@link GraphView} that contains the {@link CellState}s.
     */
    getView() {
      return this.view;
    }
    /**
     * Returns the {@link Stylesheet} that defines the style.
     */
    getStylesheet() {
      return this.stylesheet;
    }
    /**
     * Sets the {@link Stylesheet} that defines the style.
     */
    setStylesheet(stylesheet) {
      this.stylesheet = stylesheet;
    }
    /**
     * Called when the graph model changes. Invokes {@link processChange} on each
     * item of the given array to update the view accordingly.
     *
     * @param changes Array that contains the individual changes.
     */
    graphModelChanged(changes) {
      for (const change of changes) {
        this.processChange(change);
      }
      this.updateSelection();
      this.view.validate();
      this.sizeDidChange();
    }
    /**
     * Processes the given change and invalidates the respective cached data
     * in {@link GraphView}. This fires a {@link root} event if the root has changed in the
     * model.
     *
     * @param {(RootChange|ChildChange|TerminalChange|GeometryChange|ValueChange|StyleChange)} change - Object that represents the change on the model.
     */
    processChange(change) {
      if (change instanceof RootChange_default) {
        this.clearSelection();
        this.setDefaultParent(null);
        if (change.previous)
          this.removeStateForCell(change.previous);
        if (this.resetViewOnRootChange) {
          this.view.scale = 1;
          this.view.translate.x = 0;
          this.view.translate.y = 0;
        }
        this.fireEvent(new EventObject_default(InternalEvent_default.ROOT));
      } else if (change instanceof ChildChange_default) {
        const newParent = change.child.getParent();
        this.view.invalidate(change.child, true, true);
        if (!newParent || !this.getDataModel().contains(newParent) || newParent.isCollapsed()) {
          this.view.invalidate(change.child, true, true);
          this.removeStateForCell(change.child);
          if (this.view.currentRoot == change.child) {
            this.home();
          }
        }
        if (newParent != change.previous) {
          if (newParent != null) {
            this.view.invalidate(newParent, false, false);
          }
          if (change.previous != null) {
            this.view.invalidate(change.previous, false, false);
          }
        }
      } else if (change instanceof TerminalChange_default || change instanceof GeometryChange_default) {
        if (change instanceof TerminalChange_default || change.previous == null && change.geometry != null || change.previous != null && !change.previous.equals(change.geometry)) {
          this.view.invalidate(change.cell);
        }
      } else if (change instanceof ValueChange_default) {
        this.view.invalidate(change.cell, false, false);
      } else if (change instanceof StyleChange_default) {
        this.view.invalidate(change.cell, true, true);
        const state = this.view.getState(change.cell);
        if (state != null) {
          state.invalidStyle = true;
        }
      } else if (change.cell != null && change.cell instanceof Cell_default) {
        this.removeStateForCell(change.cell);
      }
    }
    /**
     * Scrolls the graph to the given point, extending the graph container if
     * specified.
     */
    scrollPointToVisible(x, y, extend = false, border = 20) {
      const panningHandler = this.getPlugin("PanningHandler");
      if (!this.isTimerAutoScroll() && (this.ignoreScrollbars || hasScrollbars(this.container))) {
        const c = this.container;
        if (x >= c.scrollLeft && y >= c.scrollTop && x <= c.scrollLeft + c.clientWidth && y <= c.scrollTop + c.clientHeight) {
          let dx = c.scrollLeft + c.clientWidth - x;
          if (dx < border) {
            const old = c.scrollLeft;
            c.scrollLeft += border - dx;
            if (extend && old === c.scrollLeft) {
              const root = this.view.getDrawPane().ownerSVGElement;
              const width = c.scrollWidth + border - dx;
              root.style.width = `${width}px`;
              c.scrollLeft += border - dx;
            }
          } else {
            dx = x - c.scrollLeft;
            if (dx < border) {
              c.scrollLeft -= border - dx;
            }
          }
          let dy = c.scrollTop + c.clientHeight - y;
          if (dy < border) {
            const old = c.scrollTop;
            c.scrollTop += border - dy;
            if (old == c.scrollTop && extend) {
              const root = this.view.getDrawPane().ownerSVGElement;
              const height = c.scrollHeight + border - dy;
              root.style.height = `${height}px`;
              c.scrollTop += border - dy;
            }
          } else {
            dy = y - c.scrollTop;
            if (dy < border) {
              c.scrollTop -= border - dy;
            }
          }
        }
      } else if (this.isAllowAutoPanning() && panningHandler && !panningHandler.isActive()) {
        panningHandler.getPanningManager().panTo(x + this.getPanDx(), y + this.getPanDy());
      }
    }
    /**
     * Returns the size of the border and padding on all four sides of the
     * container. The left, top, right and bottom borders are stored in the x, y,
     * width and height of the returned {@link Rectangle}, respectively.
     */
    getBorderSizes() {
      const css = getCurrentStyle(this.container);
      return new Rectangle_default(parseCssNumber(css.paddingLeft) + (css.borderLeftStyle != "none" ? parseCssNumber(css.borderLeftWidth) : 0), parseCssNumber(css.paddingTop) + (css.borderTopStyle != "none" ? parseCssNumber(css.borderTopWidth) : 0), parseCssNumber(css.paddingRight) + (css.borderRightStyle != "none" ? parseCssNumber(css.borderRightWidth) : 0), parseCssNumber(css.paddingBottom) + (css.borderBottomStyle != "none" ? parseCssNumber(css.borderBottomWidth) : 0));
    }
    /**
     * Returns the preferred size of the background page if {@link preferPageSize} is true.
     */
    getPreferredPageSize(bounds, width, height) {
      const tr = this.view.translate;
      const fmt = this.pageFormat;
      const ps = this.pageScale;
      const page = new Rectangle_default(0, 0, Math.ceil(fmt.width * ps), Math.ceil(fmt.height * ps));
      const hCount = this.pageBreaksVisible ? Math.ceil(width / page.width) : 1;
      const vCount = this.pageBreaksVisible ? Math.ceil(height / page.height) : 1;
      return new Rectangle_default(0, 0, hCount * page.width + 2 + tr.x, vCount * page.height + 2 + tr.y);
    }
    /**
     * Resizes the container for the given graph width and height.
     */
    doResizeContainer(width, height) {
      if (this.maximumContainerSize != null) {
        width = Math.min(this.maximumContainerSize.width, width);
        height = Math.min(this.maximumContainerSize.height, height);
      }
      const container = this.container;
      container.style.width = `${Math.ceil(width)}px`;
      container.style.height = `${Math.ceil(height)}px`;
    }
    /*****************************************************************************
     * Group: UNCLASSIFIED
     *****************************************************************************/
    /**
     * Creates a new handler for the given cell state. This implementation
     * returns a new {@link EdgeHandler} of the corresponding cell is an edge,
     * otherwise it returns an {@link VertexHandler}.
     *
     * @param state {@link CellState} whose handler should be created.
     */
    createHandler(state) {
      let result = null;
      if (state.cell.isEdge()) {
        const source = state.getVisibleTerminalState(true);
        const target = state.getVisibleTerminalState(false);
        const geo = state.cell.getGeometry();
        const edgeStyle = this.getView().getEdgeStyle(state, geo ? geo.points || void 0 : void 0, source, target);
        result = this.createEdgeHandler(state, edgeStyle);
      } else {
        result = this.createVertexHandler(state);
      }
      return result;
    }
    /**
     * Hooks to create a new {@link EdgeHandler} for the given {@link CellState}.
     *
     * This method relies on the registered elements in {@link EdgeStyleRegistry} to know which {@link EdgeHandler} to create.
     * If the {@link EdgeStyle} is not registered, it will return a default {@link EdgeHandler}.
     *
     * @param state {@link CellState} to create the handler for.
     * @param edgeStyle the {@link EdgeStyleFunction} that let choose the actual edge handler.
     */
    createEdgeHandler(state, edgeStyle) {
      const handlerKind = EdgeStyleRegistry.getHandlerKind(edgeStyle);
      switch (handlerKind) {
        case "elbow":
          return this.createElbowEdgeHandler(state);
        case "segment":
          return this.createEdgeSegmentHandler(state);
      }
      return this.createEdgeHandlerInstance(state);
    }
    /*****************************************************************************
     * Group: Drill down
     *****************************************************************************/
    /**
     * Returns the current root of the displayed cell hierarchy. This is a
     * shortcut to {@link GraphView.currentRoot} in {@link GraphView}.
     */
    getCurrentRoot() {
      return this.view.currentRoot;
    }
    /**
     * Returns the translation to be used if the given cell is the root cell as
     * an {@link Point}. This implementation returns null.
     *
     * To keep the children at their absolute position while stepping into groups,
     * this function can be overridden as follows.
     *
     * @example
     * ```javascript
     * var offset = new mxPoint(0, 0);
     *
     * while (cell != null)
     * {
     *   var geo = this.model.getGeometry(cell);
     *
     *   if (geo != null)
     *   {
     *     offset.x -= geo.x;
     *     offset.y -= geo.y;
     *   }
     *
     *   cell = this.model.getParent(cell);
     * }
     *
     * return offset;
     * ```
     *
     * @param cell {@link Cell} that represents the root.
     */
    getTranslateForRoot(cell) {
      return null;
    }
    /**
     * Returns the offset to be used for the cells inside the given cell. The
     * root and layer cells may be identified using {@link GraphDataModel.isRoot} and
     * {@link GraphDataModel.isLayer}. For all other current roots, the
     * {@link GraphView.currentRoot} field points to the respective cell, so that
     * the following holds: cell == this.view.currentRoot. This implementation
     * returns null.
     *
     * @param cell {@link Cell} whose offset should be returned.
     */
    getChildOffsetForCell(cell) {
      return null;
    }
    /**
     * Uses the root of the model as the root of the displayed cell hierarchy
     * and selects the previous root.
     */
    home() {
      const current = this.getCurrentRoot();
      if (current != null) {
        this.view.setCurrentRoot(null);
        const state = this.view.getState(current);
        if (state != null) {
          this.setSelectionCell(current);
        }
      }
    }
    /**
     * Returns true if the given cell is a valid root for the cell display
     * hierarchy. This implementation returns true for all non-null values.
     *
     * @param cell {@link Cell} which should be checked as a possible root.
     */
    isValidRoot(cell) {
      return !!cell;
    }
    /*****************************************************************************
     * Group: Graph display
     *****************************************************************************/
    /**
     * Returns the bounds of the visible graph. Shortcut to
     * {@link GraphView.getGraphBounds}. See also: {@link getBoundingBoxFromGeometry}.
     */
    getGraphBounds() {
      return this.view.getGraphBounds();
    }
    /**
     * Returns the bounds inside which the diagram should be kept as an
     * {@link Rectangle}.
     */
    getMaximumGraphBounds() {
      return this.maximumGraphBounds;
    }
    /**
     * Clears all cell states or the states for the hierarchy starting at the
     * given cell and validates the graph. This fires a refresh event as the
     * last step.
     *
     * @param cell Optional {@link Cell} for which the cell states should be cleared.
     */
    refresh(cell = null) {
      if (cell) {
        this.view.clear(cell, false);
      } else {
        this.view.clear(void 0, true);
      }
      this.view.validate();
      this.sizeDidChange();
      this.fireEvent(new EventObject_default(InternalEvent_default.REFRESH));
    }
    /**
     * Centers the graph in the container.
     *
     * @param horizontal Optional boolean that specifies if the graph should be centered
     * horizontally. Default is `true`.
     * @param vertical Optional boolean that specifies if the graph should be centered
     * vertically. Default is `true`.
     * @param cx Optional float that specifies the horizontal center. Default is `0.5`.
     * @param cy Optional float that specifies the vertical center. Default is `0.5`.
     */
    center(horizontal = true, vertical = true, cx = 0.5, cy = 0.5) {
      const container = this.container;
      const _hasScrollbars = hasScrollbars(this.container);
      const padding = 2 * this.getBorder();
      const cw = container.clientWidth - padding;
      const ch = container.clientHeight - padding;
      const bounds = this.getGraphBounds();
      const t = this.view.translate;
      const s = this.view.scale;
      let dx = horizontal ? cw - bounds.width : 0;
      let dy = vertical ? ch - bounds.height : 0;
      if (!_hasScrollbars) {
        this.view.setTranslate(horizontal ? Math.floor(t.x - bounds.x / s + dx * cx / s) : t.x, vertical ? Math.floor(t.y - bounds.y / s + dy * cy / s) : t.y);
      } else {
        bounds.x -= t.x;
        bounds.y -= t.y;
        const sw = container.scrollWidth;
        const sh = container.scrollHeight;
        if (sw > cw) {
          dx = 0;
        }
        if (sh > ch) {
          dy = 0;
        }
        this.view.setTranslate(Math.floor(dx / 2 - bounds.x), Math.floor(dy / 2 - bounds.y));
        container.scrollLeft = (sw - cw) / 2;
        container.scrollTop = (sh - ch) / 2;
      }
    }
    /**
     * Returns `true` if perimeter points should be computed such that the resulting edge has only horizontal or vertical segments.
     *
     * This method relies on the registered elements in {@link EdgeStyleRegistry} to know if the {@link CellStateStyle.edgeStyle} of the {@link CellState} is orthogonal.
     * If the {@link EdgeStyle} is not registered, it is considered as NOT orthogonal.
     *
     * @param edge {@link CellState} that represents the edge.
     */
    isOrthogonal(edge) {
      const orthogonal = edge.style.orthogonal;
      if (!isNullish(orthogonal)) {
        return orthogonal;
      }
      const edgeStyle = this.view.getEdgeStyle(edge);
      return EdgeStyleRegistry.isOrthogonal(edgeStyle);
    }
    /*****************************************************************************
     * Group: Graph appearance
     *****************************************************************************/
    /**
     * Returns the {@link backgroundImage} as an {@link Image}.
     */
    getBackgroundImage() {
      return this.backgroundImage;
    }
    /**
     * Sets the new {@link backgroundImage}.
     *
     * @param image New {@link Image} to be used for the background.
     */
    setBackgroundImage(image) {
      this.backgroundImage = image;
    }
    /**
     * Returns the textual representation for the given cell.
     *
     * This implementation returns the node name or string-representation of the user object.
     *
     *
     * The following returns the label attribute from the cells user object if it is an XML node.
     *
     * @example
     * ```javascript
     * graph.convertValueToString = function(cell)
     * {
     * 	return cell.getAttribute('label');
     * }
     * ```
     *
     * See also: {@link cellLabelChanged}.
     *
     * @param cell {@link Cell} whose textual representation should be returned.
     */
    convertValueToString(cell) {
      const value = cell.getValue();
      if (value != null) {
        if (isNode(value)) {
          return value.nodeName;
        }
        if (typeof value.toString === "function") {
          return value.toString();
        }
      }
      return "";
    }
    /**
     * Returns the string to be used as the link for the given cell.
     *
     * This implementation returns null.
     *
     * @param cell {@link Cell} whose link should be returned.
     */
    getLinkForCell(cell) {
      return null;
    }
    /**
     * Returns the value of {@link border}.
     */
    getBorder() {
      return this.border;
    }
    /**
     * Sets the value of {@link border}.
     *
     * @param value Positive integer that represents the border to be used.
     */
    setBorder(value) {
      this.border = value;
    }
    /*****************************************************************************
     * Group: Graph behaviour
     *****************************************************************************/
    /**
     * Returns {@link resizeContainer}.
     */
    isResizeContainer() {
      return this.resizeContainer;
    }
    /**
     * Sets {@link resizeContainer}.
     *
     * @param value Boolean indicating if the container should be resized.
     */
    setResizeContainer(value) {
      this.resizeContainer = value;
    }
    /**
     * Returns true if the graph is {@link enabled}.
     */
    isEnabled() {
      return this.enabled;
    }
    /**
     * Specifies if the graph should allow any interactions. This
     * implementation updates {@link enabled}.
     *
     * @param value Boolean indicating if the graph should be enabled.
     */
    setEnabled(value) {
      this.enabled = value;
    }
    /**
     * Returns {@link multigraph} as a boolean.
     */
    isMultigraph() {
      return this.multigraph;
    }
    /**
     * Specifies if the graph should allow multiple connections between the
     * same pair of vertices.
     *
     * @param value Boolean indicating if the graph allows multiple connections
     * between the same pair of vertices.
     */
    setMultigraph(value) {
      this.multigraph = value;
    }
    /**
     * Returns {@link allowLoops} as a boolean.
     */
    isAllowLoops() {
      return this.allowLoops;
    }
    /**
     * Specifies if loops are allowed.
     *
     * @param value Boolean indicating if loops are allowed.
     */
    setAllowLoops(value) {
      this.allowLoops = value;
    }
    /**
     * Returns {@link recursiveResize}.
     *
     * @param state {@link CellState} that is being resized.
     */
    isRecursiveResize(state = null) {
      return this.recursiveResize;
    }
    /**
     * Sets {@link recursiveResize}.
     *
     * @param value New boolean value for {@link recursiveResize}.
     */
    setRecursiveResize(value) {
      this.recursiveResize = value;
    }
    /**
     * Returns a decimal number representing the amount of the width and height
     * of the given cell that is allowed to overlap its parent. A value of 0
     * means all children must stay inside the parent, 1 means the child is
     * allowed to be placed outside of the parent such that it touches one of
     * the parents sides. If {@link isAllowOverlapParent} returns false for the given
     * cell, then this method returns 0.
     *
     * @param cell {@link Cell} for which the overlap ratio should be returned.
     */
    getOverlap(cell) {
      return this.isAllowOverlapParent(cell) ? this.defaultOverlap : 0;
    }
    /**
     * Returns true if the given cell is allowed to be placed outside the
     * parents area.
     *
     * @param cell {@link Cell} that represents the child to be checked.
     */
    isAllowOverlapParent(cell) {
      return false;
    }
    /*****************************************************************************
     * Group: Cell retrieval
     *****************************************************************************/
    /**
     * Returns {@link defaultParent} or {@link GraphView.currentRoot} or the first child
     * of {@link GraphDataModel.root} if both are null. The value returned by
     * this function should be used as the parent for new cells (aka default
     * layer).
     */
    getDefaultParent() {
      let parent = this.getCurrentRoot();
      if (!parent) {
        parent = this.defaultParent;
        if (!parent) {
          const root = this.getDataModel().getRoot();
          parent = root.getChildAt(0);
        }
      }
      return parent;
    }
    /**
     * Sets the {@link defaultParent} to the given cell. Set this to null to return
     * the first child of the root in getDefaultParent.
     */
    setDefaultParent(cell) {
      this.defaultParent = cell;
    }
    /**
     * Destroys the graph and all its resources.
     * After calling this method, the Graph should not be used anymore.
     *
     * For example, call this method when unmounting/disposing a component using the Graph to avoid memory leaks.
     */
    destroy() {
      if (!this.destroyed) {
        this.destroyed = true;
        this.plugins.forEach((p) => p.onDestroy());
        this.view.destroy();
        if (this.model && this.graphModelChangeListener) {
          this.getDataModel().removeListener(this.graphModelChangeListener);
          this.graphModelChangeListener = null;
        }
        this.container = null;
        super.destroy();
      }
    }
  };
  applyGraphMixins(AbstractGraph);

  // node_modules/@maxgraph/core/lib/esm/view/undoable-change/UndoableEdit.js
  var UndoableEdit = class {
    constructor(source, significant = true) {
      this.changes = [];
      this.significant = true;
      this.undone = false;
      this.redone = false;
      this.source = source;
      this.changes = [];
      this.significant = significant;
    }
    /**
     * Returns true if the this edit contains no changes.
     */
    isEmpty() {
      return this.changes.length === 0;
    }
    /**
     * Returns <significant>.
     */
    isSignificant() {
      return this.significant;
    }
    /**
     * Adds the specified change to this edit. The change is an object that is
     * expected to either have an undo and redo, or an execute function.
     */
    add(change) {
      this.changes.push(change);
    }
    /**
     * Hook to notify any listeners of the changes after an <undo> or <redo>
     * has been carried out. This implementation is empty.
     */
    notify() {
      return;
    }
    /**
     * Hook to free resources after the edit has been removed from the command
     * history. This implementation is empty.
     */
    die() {
      return;
    }
    /**
     * Undoes all changes in this edit.
     */
    undo() {
      if (!this.undone) {
        this.source.fireEvent(new EventObject_default(InternalEvent_default.START_EDIT));
        const count = this.changes.length;
        for (let i = count - 1; i >= 0; i--) {
          const change = this.changes[i];
          if (change.execute) {
            change.execute();
          } else if (change.undo) {
            change.undo();
          }
          this.source.fireEvent(new EventObject_default(InternalEvent_default.EXECUTED, { change }));
        }
        this.undone = true;
        this.redone = false;
        this.source.fireEvent(new EventObject_default(InternalEvent_default.END_EDIT));
      }
      this.notify();
    }
    /**
     * Redoes all changes in this edit.
     */
    redo() {
      if (!this.redone) {
        this.source.fireEvent(new EventObject_default(InternalEvent_default.START_EDIT));
        const count = this.changes.length;
        for (let i = 0; i < count; i += 1) {
          const change = this.changes[i];
          if (change.execute != null) {
            change.execute();
          } else if (change.redo != null) {
            change.redo();
          }
          this.source.fireEvent(new EventObject_default(InternalEvent_default.EXECUTED, { change }));
        }
        this.undone = false;
        this.redone = true;
        this.source.fireEvent(new EventObject_default(InternalEvent_default.END_EDIT));
      }
      this.notify();
    }
  };
  var UndoableEdit_default = UndoableEdit;

  // node_modules/@maxgraph/core/lib/esm/view/undoable-change/CollapseChange.js
  var CollapseChange = class {
    constructor(model, cell, collapsed) {
      this.model = model;
      this.cell = cell;
      this.collapsed = collapsed;
      this.previous = collapsed;
    }
    /**
     * Changes the collapsed state of {@link cell} to {@link previous} using {@link GraphDataModel.collapsedStateForCellChanged}.
     */
    execute() {
      this.collapsed = this.previous;
      this.previous = this.model.collapsedStateForCellChanged(this.cell, this.previous);
    }
  };
  var CollapseChange_default = CollapseChange;

  // node_modules/@maxgraph/core/lib/esm/view/undoable-change/VisibleChange.js
  var VisibleChange = class {
    constructor(model, cell, visible) {
      this.model = model;
      this.cell = cell;
      this.visible = visible;
      this.previous = visible;
    }
    /**
     * Changes the visible state of {@link cell} to {@link previous} using {@link GraphDataModel.visibleStateForCellChanged}.
     */
    execute() {
      this.visible = this.previous;
      this.previous = this.model.visibleStateForCellChanged(this.cell, this.previous);
    }
  };
  var VisibleChange_default = VisibleChange;

  // node_modules/@maxgraph/core/lib/esm/view/GraphDataModel.js
  var GraphDataModel = class extends EventSource_default {
    constructor(root = null) {
      super();
      this.root = null;
      this.cells = {};
      this.maintainEdgeParent = true;
      this.ignoreRelativeEdgeParent = true;
      this.createIds = true;
      this.prefix = "";
      this.postfix = "";
      this.nextId = 0;
      this.currentEdit = null;
      this.updateLevel = 0;
      this.endingUpdate = false;
      this.currentEdit = this.createUndoableEdit();
      if (root != null) {
        this.setRoot(root);
      } else {
        this.clear();
      }
    }
    /**
     * Sets a new root using {@link createRoot}.
     */
    clear() {
      this.setRoot(this.createRoot());
    }
    /**
     * Returns {@link createIds}.
     */
    isCreateIds() {
      return this.createIds;
    }
    /**
     * Sets {@link createIds}.
     */
    setCreateIds(value) {
      this.createIds = value;
    }
    /**
     * Creates a new root cell with a default layer (child 0).
     */
    createRoot() {
      const cell = new Cell_default();
      cell.insert(new Cell_default());
      return cell;
    }
    /**
     * Returns the {@link Cell} for the specified Id or null if no cell can be
     * found for the given Id.
     *
     * @param {string} id  A string representing the Id of the cell.
     */
    getCell(id) {
      return this.cells ? this.cells[id] : null;
    }
    filterCells(cells, filter) {
      return cells.filter(filter);
    }
    getRoot(cell = null) {
      return cell ? cell.getRoot() : this.root;
    }
    /**
     * Sets the {@link root} of the model using {@link RootChange} and adds the change to
     * the current transaction. This resets all datastructures in the model and
     * is the preferred way of clearing an existing model. Returns the new
     * root.
     *
     * Example:
     *
     * ```javascript
     * const root = new Cell();
     * root.insert(new Cell());
     * model.setRoot(root);
     * ```
     *
     * @param {Cell} root  that specifies the new root.
     */
    setRoot(root) {
      this.execute(new RootChange_default(this, root));
      return root;
    }
    /**
     * Inner callback to change the root of the model and update the internal
     * datastructures, such as {@link cells} and {@link nextId}. Returns the previous root.
     *
     * @param {Cell} root  that specifies the new root.
     */
    rootChanged(root) {
      const oldRoot = this.root;
      this.root = root;
      this.nextId = 0;
      this.cells = null;
      this.cellAdded(root);
      return oldRoot;
    }
    /**
     * Returns true if the given cell is the root of the model and a non-null
     * value.
     *
     * @param {Cell} cell  that represents the possible root.
     */
    isRoot(cell = null) {
      return cell != null && this.root === cell;
    }
    /**
     * Returns true if {@link isRoot} returns true for the parent of the given cell.
     *
     * @param cell  that represents the possible layer.
     */
    isLayer(cell) {
      return cell ? this.isRoot(cell.getParent()) : false;
    }
    /**
     * Returns true if the model contains the given {@link Cell}.
     *
     * @param {Cell} cell  that specifies the cell.
     */
    contains(cell) {
      return this.root.isAncestor(cell);
    }
    /**
     * Adds the specified child to the parent at the given index using
     * {@link ChildChange} and adds the change to the current transaction. If no
     * index is specified then the child is appended to the parent's array of
     * children. Returns the inserted child.
     *
     * @param {Cell} parent  that specifies the parent to contain the child.
     * @param {Cell} child  that specifies the child to be inserted.
     * @param index  Optional integer that specifies the index of the child.
     */
    add(parent, child, index = null) {
      if (child !== parent && parent != null && child != null) {
        if (index == null) {
          index = parent.getChildCount();
        }
        const parentChanged = parent !== child.getParent();
        this.execute(new ChildChange_default(this, parent, child, index));
        if (this.maintainEdgeParent && parentChanged) {
          this.updateEdgeParents(child);
        }
      }
      return child;
    }
    /**
     * Inner callback to update {@link cells} when a cell has been added. This
     * implementation resolves collisions by creating new Ids. To change the
     * ID of a cell after it was inserted into the model, use the following
     * code:
     *
     * (code
     * delete model.cells[cell.getId()];
     * cell.setId(newId);
     * model.cells[cell.getId()] = cell;
     * ```
     *
     * If the change of the ID should be part of the command history, then the
     * cell should be removed from the model and a clone with the new ID should
     * be reinserted into the model instead.
     *
     * @param {Cell} cell  that specifies the cell that has been added.
     */
    cellAdded(cell) {
      if (cell != null) {
        if (cell.getId() == null && this.createIds) {
          cell.setId(this.createId(cell));
        }
        if (cell.getId() != null) {
          let collision = this.getCell(cell.getId());
          if (collision !== cell) {
            while (collision != null) {
              cell.setId(this.createId(cell));
              collision = this.getCell(cell.getId());
            }
            if (this.cells == null) {
              this.cells = {};
            }
            this.cells[cell.getId()] = cell;
          }
        }
        if (isNumeric(String(cell.getId()))) {
          this.nextId = Math.max(this.nextId, Number.parseInt(cell.getId()));
        }
        for (const child of cell.getChildren()) {
          this.cellAdded(child);
        }
      }
    }
    /**
     * Hook method to create an Id for the specified cell. This implementation
     * concatenates {@link prefix}, id and {@link postfix} to create the Id and increments
     * {@link nextId}. The cell is ignored by this implementation, but can be used in
     * overridden methods to prefix the Ids with eg. the cell type.
     *
     * @param {Cell} cell  to create the Id for.
     */
    createId(cell) {
      const id = this.nextId;
      this.nextId++;
      return this.prefix + id + this.postfix;
    }
    /**
     * Updates the parent for all edges that are connected to cell or one of
     * its descendants using {@link updateEdgeParent}.
     */
    updateEdgeParents(cell, root = this.getRoot(cell)) {
      const childCount = cell.getChildCount();
      for (let i = 0; i < childCount; i += 1) {
        const child = cell.getChildAt(i);
        this.updateEdgeParents(child, root);
      }
      const edgeCount = cell.getEdgeCount();
      const edges = [];
      for (let i = 0; i < edgeCount; i += 1) {
        edges.push(cell.getEdgeAt(i));
      }
      for (let i = 0; i < edges.length; i += 1) {
        const edge = edges[i];
        if (root.isAncestor(edge)) {
          this.updateEdgeParent(edge, root);
        }
      }
    }
    /**
     * Inner callback to update the parent of the specified {@link Cell} to the
     * nearest-common-ancestor of its two terminals.
     *
     * @param {Cell} edge  that specifies the edge.
     * @param {Cell} root  that represents the current root of the model.
     */
    updateEdgeParent(edge, root) {
      let source = edge.getTerminal(true);
      let target = edge.getTerminal(false);
      let cell = null;
      while (source != null && !source.isEdge() && source.geometry != null && source.geometry.relative) {
        source = source.getParent();
      }
      while (target != null && this.ignoreRelativeEdgeParent && !target.isEdge() && target.geometry != null && target.geometry.relative) {
        target = target.getParent();
      }
      if (root.isAncestor(source) && root.isAncestor(target)) {
        if (source === target) {
          cell = source ? source.getParent() : null;
        } else if (source) {
          cell = source.getNearestCommonAncestor(target);
        }
        if (cell != null && (cell.getParent() !== this.root || cell.isAncestor(edge)) && edge && edge.getParent() !== cell) {
          let geo = edge.getGeometry();
          if (geo != null) {
            const origin1 = edge.getParent().getOrigin();
            const origin2 = cell.getOrigin();
            const dx = origin2.x - origin1.x;
            const dy = origin2.y - origin1.y;
            geo = geo.clone();
            geo.translate(-dx, -dy);
            this.setGeometry(edge, geo);
          }
          this.add(cell, edge, cell.getChildCount());
        }
      }
    }
    /**
     * Removes the specified cell from the model using {@link ChildChange} and adds
     * the change to the current transaction. This operation will remove the
     * cell and all of its children from the model. Returns the removed cell.
     *
     * @param {Cell} cell  that should be removed.
     */
    remove(cell) {
      if (cell === this.root) {
        this.setRoot(null);
      } else if (cell.getParent() != null) {
        this.execute(new ChildChange_default(this, null, cell));
      }
      return cell;
    }
    /**
     * Inner callback to update {@link cells} when a cell has been removed.
     *
     * @param {Cell} cell  that specifies the cell that has been removed.
     */
    cellRemoved(cell) {
      if (cell != null && this.cells != null) {
        const childCount = cell.getChildCount();
        for (let i = childCount - 1; i >= 0; i--) {
          this.cellRemoved(cell.getChildAt(i));
        }
        if (this.cells != null && cell.getId() != null) {
          delete this.cells[cell.getId()];
        }
      }
    }
    /**
     * Inner callback to update the parent of a cell using {@link Cell#insert}
     * on the parent and return the previous parent.
     *
     * @param {Cell} cell  to update the parent for.
     * @param {Cell} parent  that specifies the new parent of the cell.
     * @param index  Optional integer that defines the index of the child
     * in the parent's child array.
     */
    parentForCellChanged(cell, parent, index) {
      const previous = cell.getParent();
      if (parent != null) {
        if (parent !== previous || previous.getIndex(cell) !== index) {
          parent.insert(cell, index);
        }
      } else if (previous != null) {
        const oldIndex = previous.getIndex(cell);
        previous.remove(oldIndex);
      }
      const par = parent ? this.contains(parent) : null;
      const pre = this.contains(previous);
      if (par && !pre) {
        this.cellAdded(cell);
      } else if (pre && !par) {
        this.cellRemoved(cell);
      }
      return previous;
    }
    /**
     * Sets the source or target terminal of the given {@link Cell} using
     * {@link TerminalChange} and adds the change to the current transaction.
     * This implementation updates the parent of the edge using {@link updateEdgeParent}
     * if required.
     *
     * @param {Cell} edge  that specifies the edge.
     * @param {Cell} terminal  that specifies the new terminal.
     * @param isSource  Boolean indicating if the terminal is the new source or
     * target terminal of the edge.
     */
    setTerminal(edge, terminal, isSource) {
      const terminalChanged = terminal !== edge.getTerminal(isSource);
      this.execute(new TerminalChange_default(this, edge, terminal, isSource));
      if (this.maintainEdgeParent && terminalChanged) {
        this.updateEdgeParent(edge, this.getRoot());
      }
      return terminal;
    }
    /**
     * Sets the source and target {@link Cell} of the given {@link Cell} in a single
     * transaction using {@link setTerminal} for each end of the edge.
     *
     * @param {Cell} edge  that specifies the edge.
     * @param {Cell} source  that specifies the new source terminal.
     * @param {Cell} target  that specifies the new target terminal.
     */
    setTerminals(edge, source, target) {
      this.beginUpdate();
      try {
        this.setTerminal(edge, source, true);
        this.setTerminal(edge, target, false);
      } finally {
        this.endUpdate();
      }
    }
    /**
     * Inner helper function to update the terminal of the edge using
     * {@link Cell#insertEdge} and return the previous terminal.
     *
     * @param {Cell} edge  that specifies the edge to be updated.
     * @param {Cell} terminal  that specifies the new terminal.
     * @param isSource  Boolean indicating if the terminal is the new source or
     * target terminal of the edge.
     */
    terminalForCellChanged(edge, terminal, isSource = false) {
      const previous = edge.getTerminal(isSource);
      if (terminal != null) {
        terminal.insertEdge(edge, isSource);
      } else if (previous != null) {
        previous.removeEdge(edge, isSource);
      }
      return previous;
    }
    /**
     * Returns all edges between the given source and target pair. If directed
     * is true, then only edges from the source to the target are returned,
     * otherwise, all edges between the two cells are returned.
     *
     * @param {Cell} source  that defines the source terminal of the edge to be
     * returned.
     * @param {Cell} target  that defines the target terminal of the edge to be
     * returned.
     * @param directed  Optional boolean that specifies if the direction of the
     * edge should be taken into account. Default is false.
     */
    getEdgesBetween(source, target, directed = false) {
      const tmp1 = source.getEdgeCount();
      const tmp2 = target.getEdgeCount();
      let terminal = source;
      let edgeCount = tmp1;
      if (tmp2 < tmp1) {
        edgeCount = tmp2;
        terminal = target;
      }
      const result = [];
      for (let i = 0; i < edgeCount; i += 1) {
        const edge = terminal.getEdgeAt(i);
        const src = edge.getTerminal(true);
        const trg = edge.getTerminal(false);
        const directedMatch = src === source && trg === target;
        const oppositeMatch = trg === source && src === target;
        if (directedMatch || !directed && oppositeMatch) {
          result.push(edge);
        }
      }
      return result;
    }
    /**
     * Sets the user object of then given {@link Cell} using {@link ValueChange}
     * and adds the change to the current transaction.
     *
     * @param {Cell} cell  whose user object should be changed.
     * @param value  Object that defines the new user object.
     */
    setValue(cell, value) {
      this.execute(new ValueChange_default(this, cell, value));
      return value;
    }
    /**
     * Inner callback to update the user object of the given {@link Cell}
     * using {@link Cell#valueChanged} and return the previous value,
     * that is, the return value of {@link Cell#valueChanged}.
     *
     * To change a specific attribute in an XML node, the following code can be
     * used.
     *
     * ```javascript
     * graph.getDataModel().valueForCellChanged(cell, value)
     * {
     *   var previous = cell.value.getAttribute('label');
     *   cell.value.setAttribute('label', value);
     *
     *   return previous;
     * };
     * ```
     */
    valueForCellChanged(cell, value) {
      return cell.valueChanged(value);
    }
    /**
     * Sets the {@link Geometry} of the given {@link Cell}. The actual update
     * of the cell is carried out in {@link geometryForCellChanged}. The
     * {@link GeometryChange} action is used to encapsulate the change.
     *
     * @param {Cell} cell  whose geometry should be changed.
     * @param {Geometry} geometry  that defines the new geometry.
     */
    setGeometry(cell, geometry) {
      if (geometry !== cell.getGeometry()) {
        this.execute(new GeometryChange_default(this, cell, geometry));
      }
      return geometry;
    }
    /**
     * Inner callback to update the {@link Geometry} of the given {@link Cell} using
     * {@link Cell#setGeometry} and return the previous {@link Geometry}.
     */
    geometryForCellChanged(cell, geometry) {
      const previous = cell.getGeometry();
      cell.setGeometry(geometry);
      return previous;
    }
    /**
     * Sets the style of the given {@link Cell} using {@link StyleChange} and adds the change to the current transaction.
     *
     * **IMPORTANT**: Do not pass {@link Cell.getStyle} as value of the `style` parameter. Otherwise, no style change is performed, so the view won't be updated.
     * Always get a clone of the style of the cell with {@link Cell.getClonedStyle}, then update it and pass the updated style to this method.
     *
     * @param cell  whose style should be changed.
     * @param style the new cell style to set.
     */
    setStyle(cell, style) {
      if (style !== cell.getStyle()) {
        this.execute(new StyleChange_default(this, cell, style));
      }
    }
    /**
     * Inner callback to update the style of the given {@link Cell}  using {@link Cell#setStyle} and return the previous style.
     *
     * **IMPORTANT**: to fully work, this method should not receive `cell.getStyle` as value of the `style` parameter. See {@link setStyle} for more information.
     *
     * @param cell  whose style should be changed.
     * @param style the new cell style to set.
     */
    styleForCellChanged(cell, style) {
      const previous = cell.getStyle();
      cell.setStyle(style);
      return previous;
    }
    /**
     * Sets the collapsed state of the given {@link Cell} using {@link CollapseChange}
     * and adds the change to the current transaction.
     *
     * @param {Cell} cell  whose collapsed state should be changed.
     * @param collapsed  Boolean that specifies the new collpased state.
     */
    setCollapsed(cell, collapsed) {
      if (collapsed !== cell.isCollapsed()) {
        this.execute(new CollapseChange_default(this, cell, collapsed));
      }
      return collapsed;
    }
    /**
     * Inner callback to update the collapsed state of the
     * given {@link Cell} using {@link Cell#setCollapsed} and return
     * the previous collapsed state.
     *
     * @param {Cell} cell  that specifies the cell to be updated.
     * @param collapsed  Boolean that specifies the new collapsed state.
     */
    collapsedStateForCellChanged(cell, collapsed) {
      const previous = cell.isCollapsed();
      cell.setCollapsed(collapsed);
      return previous;
    }
    /**
     * Sets the visible state of the given {@link Cell} using {@link VisibleChange} and
     * adds the change to the current transaction.
     *
     * @param {Cell} cell  whose visible state should be changed.
     * @param visible  Boolean that specifies the new visible state.
     */
    setVisible(cell, visible) {
      if (visible !== cell.isVisible()) {
        this.execute(new VisibleChange_default(this, cell, visible));
      }
      return visible;
    }
    /**
     * Inner callback to update the visible state of the
     * given {@link Cell} using {@link Cell#setCollapsed} and return
     * the previous visible state.
     *
     * @param {Cell} cell  that specifies the cell to be updated.
     * @param visible  Boolean that specifies the new visible state.
     */
    visibleStateForCellChanged(cell, visible) {
      const previous = cell.isVisible();
      cell.setVisible(visible);
      return previous;
    }
    /**
     * Executes the given edit and fires events if required. The edit object
     * requires an execute function which is invoked. The edit is added to the
     * {@link currentEdit} between {@link beginUpdate} and {@link endUpdate} calls, so that
     * events will be fired if this execute is an individual transaction, that
     * is, if no previous {@link beginUpdate} calls have been made without calling
     * {@link endUpdate}. This implementation fires an {@link execute} event before
     * executing the given change.
     *
     * @param change  Object that described the change.
     */
    execute(change) {
      change.execute();
      this.beginUpdate();
      this.currentEdit.add(change);
      this.fireEvent(new EventObject_default(InternalEvent_default.EXECUTE, { change }));
      this.fireEvent(new EventObject_default(InternalEvent_default.EXECUTED, { change }));
      this.endUpdate();
    }
    /**
     * Updates the model in a transaction.
     * This is a shortcut to the usage of {@link beginUpdate} and the {@link endUpdate} methods.
     *
     * ```javascript
     * const model = graph.getDataModel();
     * const parent = graph.getDefaultParent();
     * const index = model.getChildCount(parent);
     * model.batchUpdate(() => {
     *   model.add(parent, v1, index);
     *   model.add(parent, v2, index+1);
     * });
     * ```
     *
     * @param fn the update to be performed in the transaction.
     */
    batchUpdate(fn) {
      this.beginUpdate();
      try {
        fn();
      } finally {
        this.endUpdate();
      }
    }
    /**
     * Increments the {@link updateLevel} by one. The event notification
     * is queued until {@link updateLevel} reaches 0 by use of
     * {@link endUpdate}.
     *
     * All changes on {@link GraphDataModel} are transactional,
     * that is, they are executed in a single undoable change
     * on the model (without transaction isolation).
     * Therefore, if you want to combine any
     * number of changes into a single undoable change,
     * you should group any two or more API calls that
     * modify the graph model between {@link beginUpdate}
     * and {@link endUpdate} calls as shown here:
     *
     * ```javascript
     * const model = graph.getDataModel();
     * const parent = graph.getDefaultParent();
     * const index = model.getChildCount(parent);
     * model.beginUpdate();
     * try
     * {
     *   model.add(parent, v1, index);
     *   model.add(parent, v2, index+1);
     * }
     * finally
     * {
     *   model.endUpdate();
     * }
     * ```
     *
     * Of course there is a shortcut for appending a
     * sequence of cells into the default parent:
     *
     * ```javascript
     * graph.addCells([v1, v2]).
     * ```
     */
    beginUpdate() {
      this.updateLevel += 1;
      this.fireEvent(new EventObject_default(InternalEvent_default.BEGIN_UPDATE));
      if (this.updateLevel === 1) {
        this.fireEvent(new EventObject_default(InternalEvent_default.START_EDIT));
      }
    }
    /**
     * Decrements the {@link updateLevel} by one and fires an {@link undo}
     * event if the {@link updateLevel} reaches 0. This function
     * indirectly fires a {@link change} event by invoking the notify
     * function on the {@link currentEdit} und then creates a new
     * {@link currentEdit} using {@link createUndoableEdit}.
     *
     * The {@link undo} event is fired only once per edit, whereas
     * the {@link change} event is fired whenever the notify
     * function is invoked, that is, on undo and redo of
     * the edit.
     */
    endUpdate() {
      this.updateLevel -= 1;
      if (this.updateLevel === 0) {
        this.fireEvent(new EventObject_default(InternalEvent_default.END_EDIT));
      }
      if (!this.endingUpdate) {
        this.endingUpdate = this.updateLevel === 0;
        this.fireEvent(new EventObject_default(InternalEvent_default.END_UPDATE, { edit: this.currentEdit }));
        try {
          if (this.endingUpdate && !this.currentEdit.isEmpty()) {
            this.fireEvent(new EventObject_default(InternalEvent_default.BEFORE_UNDO, { edit: this.currentEdit }));
            const tmp = this.currentEdit;
            this.currentEdit = this.createUndoableEdit();
            tmp.notify();
            this.fireEvent(new EventObject_default(InternalEvent_default.UNDO, { edit: tmp }));
          }
        } finally {
          this.endingUpdate = false;
        }
      }
    }
    /**
     * Creates a new {@link UndoableEdit} that implements the
     * notify function to fire a {@link change} and {@link notify} event
     * through the {@link UndoableEdit}'s source.
     *
     * @param significant  Optional boolean that specifies if the edit to be created is
     * significant. Default is true.
     */
    createUndoableEdit(significant = true) {
      const edit = new UndoableEdit_default(this, significant);
      edit.notify = () => {
        edit.source.fireEvent(new EventObject_default(InternalEvent_default.CHANGE, { edit, changes: edit.changes }));
        edit.source.fireEvent(new EventObject_default(InternalEvent_default.NOTIFY, { edit, changes: edit.changes }));
      };
      return edit;
    }
    /**
     * Merges the children of the given cell into the given target cell inside
     * this model. All cells are cloned unless there is a corresponding cell in
     * the model with the same id, in which case the source cell is ignored and
     * all edges are connected to the corresponding cell in this model. Edges
     * are considered to have no identity and are always cloned unless the
     * cloneAllEdges flag is set to false, in which case edges with the same
     * id in the target model are reconnected to reflect the terminals of the
     * source edges.
     */
    mergeChildren(from, to, cloneAllEdges = true) {
      this.beginUpdate();
      try {
        const mapping = {};
        this.mergeChildrenImpl(from, to, cloneAllEdges, mapping);
        for (const key in mapping) {
          const cell = mapping[key];
          let terminal = cell.getTerminal(true);
          if (terminal != null) {
            terminal = mapping[CellPath_default.create(terminal)];
            this.setTerminal(cell, terminal, true);
          }
          terminal = cell.getTerminal(false);
          if (terminal != null) {
            terminal = mapping[CellPath_default.create(terminal)];
            this.setTerminal(cell, terminal, false);
          }
        }
      } finally {
        this.endUpdate();
      }
    }
    /**
     * Clones the children of the source cell into the given target cell in
     * this model and adds an entry to the mapping that maps from the source
     * cell to the target cell with the same id or the clone of the source cell
     * that was inserted into this model.
     */
    mergeChildrenImpl(from, to, cloneAllEdges, mapping = {}) {
      this.beginUpdate();
      try {
        const childCount = from.getChildCount();
        for (let i = 0; i < childCount; i += 1) {
          const cell = from.getChildAt(i);
          if (typeof cell.getId === "function") {
            const id = cell.getId();
            let target = id != null && (!cell.isEdge() || !cloneAllEdges) ? this.getCell(id) : null;
            if (target == null) {
              const clone2 = cell.clone();
              clone2.setId(id);
              clone2.setTerminal(cell.getTerminal(true), true);
              clone2.setTerminal(cell.getTerminal(false), false);
              target = to.insert(clone2);
              this.cellAdded(target);
            }
            mapping[CellPath_default.create(cell)] = target;
            this.mergeChildrenImpl(cell, target, cloneAllEdges, mapping);
          }
        }
      } finally {
        this.endUpdate();
      }
    }
  };
  var GraphDataModel_default = GraphDataModel;

  // node_modules/@maxgraph/core/lib/esm/view/style/marker/EdgeMarkerRegistry.js
  var EdgeMarkerRegistryImpl = class extends BaseRegistry {
    createMarker(canvas, shape, type, pe, unitX, unitY, size, source, sw, filled) {
      const markerFunction = this.get(type);
      return markerFunction ? markerFunction(canvas, shape, type, pe, unitX, unitY, size, source, sw, filled) : null;
    }
  };
  var EdgeMarkerRegistry = new EdgeMarkerRegistryImpl();

  // node_modules/@maxgraph/core/lib/esm/view/shape/edge/ConnectorShape.js
  var ConnectorShape = class extends PolylineShape_default {
    constructor(points, stroke, strokewidth) {
      super(points, stroke, strokewidth);
    }
    /**
     * Updates the {@link boundingBox} for this shape using {@link createBoundingBox}
     * and {@link augmentBoundingBox} and stores the result in {@link boundingBox}.
     */
    updateBoundingBox() {
      var _a2, _b;
      this.useSvgBoundingBox = (_b = (_a2 = this.style) == null ? void 0 : _a2.curved) != null ? _b : false;
      super.updateBoundingBox();
    }
    /**
     * Paints the line shape.
     */
    paintEdgeShape(c, pts) {
      var _a2, _b, _c, _d, _e, _f, _g, _h;
      const sourceMarker = this.createMarker(c, pts, true);
      const targetMarker = this.createMarker(c, pts, false);
      super.paintEdgeShape(c, pts);
      c.setShadow(false);
      c.setDashed(false);
      if (sourceMarker) {
        const strokeColor = (_b = (_a2 = this.style) == null ? void 0 : _a2.startStrokeColor) != null ? _b : this.stroke;
        c.setStrokeColor(strokeColor);
        c.setFillColor((_d = (_c = this.style) == null ? void 0 : _c.startFillColor) != null ? _d : strokeColor);
        sourceMarker();
      }
      if (targetMarker) {
        const strokeColor = (_f = (_e = this.style) == null ? void 0 : _e.endStrokeColor) != null ? _f : this.stroke;
        c.setStrokeColor(strokeColor);
        c.setFillColor((_h = (_g = this.style) == null ? void 0 : _g.endFillColor) != null ? _h : strokeColor);
        targetMarker();
      }
    }
    /**
     * Prepares the marker by adding offsets in pts and returning a function to paint the marker.
     */
    createMarker(c, pts, source) {
      var _a2, _b;
      if (!this.style)
        return null;
      let result = null;
      const n = pts.length;
      const type = (source ? this.style.startArrow : this.style.endArrow) || NONE;
      let p0 = source ? pts[1] : pts[n - 2];
      const pe = source ? pts[0] : pts[n - 1];
      if (type !== NONE && p0 !== null && pe !== null) {
        let count = 1;
        while (count < n - 1 && Math.round(p0.x - pe.x) === 0 && Math.round(p0.y - pe.y) === 0) {
          p0 = source ? pts[1 + count] : pts[n - 2 - count];
          count++;
        }
        const dx = pe.x - p0.x;
        const dy = pe.y - p0.y;
        const dist = Math.max(1, Math.sqrt(dx * dx + dy * dy));
        const unitX = dx / dist;
        const unitY = dy / dist;
        const size = (_a2 = source ? this.style.startSize : this.style.endSize) != null ? _a2 : StyleDefaultsConfig.markerSize;
        const filled = (_b = source ? this.style.startFill : this.style.endFill) != null ? _b : true;
        result = EdgeMarkerRegistry.createMarker(c, this, type, pe, unitX, unitY, size, source, this.strokeWidth, filled);
      }
      return result;
    }
    /**
     * Augments the bounding box with the strokewidth and shadow offsets.
     */
    augmentBoundingBox(bbox) {
      var _a2, _b, _c, _d;
      super.augmentBoundingBox(bbox);
      if (!this.style)
        return;
      let size = 0;
      if (((_a2 = this.style.startArrow) != null ? _a2 : NONE) !== NONE) {
        size = ((_b = this.style.startSize) != null ? _b : StyleDefaultsConfig.markerSize) + 1;
      }
      if (((_c = this.style.endArrow) != null ? _c : NONE) !== NONE) {
        size = Math.max(size, (_d = this.style.endSize) != null ? _d : StyleDefaultsConfig.markerSize) + 1;
      }
      bbox.grow(size * this.scale);
    }
  };
  var ConnectorShape_default = ConnectorShape;

  // node_modules/@maxgraph/core/lib/esm/view/shape/node/TextShape.js
  var TextShape = class extends Shape_default {
    constructor(value, bounds, align = "center", valign = "middle", color = "black", family = StyleDefaultsConfig.fontFamily, size = StyleDefaultsConfig.fontSize, fontStyle = DEFAULT_FONTSTYLE, spacing = 2, spacingTop = 0, spacingRight = 0, spacingBottom = 0, spacingLeft = 0, horizontal = true, background = NONE, border = NONE, wrap = false, clipped = false, overflow = "visible", labelPadding = 0, textDirection = DEFAULT_TEXT_DIRECTION) {
      super();
      this.margin = null;
      this.unrotatedBoundingBox = null;
      this.flipH = false;
      this.flipV = false;
      this.baseSpacingTop = 0;
      this.baseSpacingBottom = 0;
      this.baseSpacingLeft = 0;
      this.baseSpacingRight = 0;
      this.replaceLinefeeds = true;
      this.verticalTextRotation = -90;
      this.ignoreClippedStringSize = true;
      this.ignoreStringSize = false;
      this.lastValue = null;
      this.cacheEnabled = true;
      this.value = value;
      this.bounds = bounds;
      this.color = color != null ? color : "black";
      this.align = align != null ? align : "center";
      this.valign = valign != null ? valign : "middle";
      this.family = family != null ? family : StyleDefaultsConfig.fontFamily;
      this.size = size != null ? size : StyleDefaultsConfig.fontSize;
      this.fontStyle = fontStyle != null ? fontStyle : DEFAULT_FONTSTYLE;
      this.spacing = spacing != null ? spacing : 2;
      this.spacingTop = this.spacing + (spacingTop != null ? spacingTop : 0);
      this.spacingRight = this.spacing + (spacingRight != null ? spacingRight : 0);
      this.spacingBottom = this.spacing + (spacingBottom != null ? spacingBottom : 0);
      this.spacingLeft = this.spacing + (spacingLeft != null ? spacingLeft : 0);
      this.horizontal = horizontal != null ? horizontal : true;
      this.background = background;
      this.border = border;
      this.wrap = wrap != null ? wrap : false;
      this.clipped = clipped != null ? clipped : false;
      this.overflow = overflow != null ? overflow : "visible";
      this.labelPadding = labelPadding != null ? labelPadding : 0;
      this.textDirection = textDirection;
      this.rotation = 0;
      this.updateMargin();
    }
    /**
     * Disables offset in IE9 for crisper image output.
     */
    getSvgScreenOffset() {
      return 0;
    }
    /**
     * Returns true if the bounds are not null and all of its variables are numeric.
     */
    checkBounds() {
      return !Number.isNaN(this.scale) && Number.isFinite(this.scale) && this.scale > 0 && this.bounds && !Number.isNaN(this.bounds.x) && !Number.isNaN(this.bounds.y) && !Number.isNaN(this.bounds.width) && !Number.isNaN(this.bounds.height);
    }
    /**
     * Generic rendering code.
     */
    paint(c, update = false) {
      const s = this.scale;
      const x = this.bounds.x / s;
      const y = this.bounds.y / s;
      const w = this.bounds.width / s;
      const h = this.bounds.height / s;
      this.updateTransform(c, x, y, w, h);
      this.configureCanvas(c, x, y, w, h);
      if (update) {
        c.updateText(x, y, w, h, this.align, this.valign, this.wrap, this.overflow, this.clipped, this.getTextRotation(), this.node);
      } else {
        const realHtml = isNode(this.value) || this.dialect === "strictHtml";
        const fmt = realHtml ? "html" : "";
        let val = this.value;
        if (!realHtml && fmt === "html") {
          val = htmlEntities(val, false);
        }
        if (fmt === "html" && !isNode(this.value)) {
          val = replaceTrailingNewlines(val, "<div><br></div>");
        }
        val = !isNode(this.value) && this.replaceLinefeeds && fmt === "html" ? val.replace(/\n/g, "<br/>") : val;
        let dir = this.textDirection;
        if (dir === "auto" && !realHtml) {
          dir = this.getAutoDirection();
        }
        if (dir !== "ltr" && dir !== "rtl") {
          dir = "";
        }
        c.text(x, y, w, h, val, this.align, this.valign, this.wrap, fmt, this.overflow, this.clipped, this.getTextRotation(), dir);
      }
    }
    /**
     * Renders the text using the given DOM nodes.
     */
    redraw() {
      if (this.visible && this.checkBounds() && this.cacheEnabled && this.lastValue === this.value && (isNode(this.value) || this.dialect === "strictHtml")) {
        if (this.node.nodeName === "DIV") {
          this.redrawHtmlShape();
          this.updateBoundingBox();
        } else {
          const canvas = this.createCanvas();
          if (canvas) {
            canvas.pointerEvents = this.pointerEvents;
            this.paint(canvas, true);
            this.destroyCanvas(canvas);
            this.updateBoundingBox();
          }
        }
      } else {
        super.redraw();
        if (isNode(this.value) || this.dialect === "strictHtml") {
          this.lastValue = this.value;
        } else {
          this.lastValue = null;
        }
      }
    }
    /**
     * Resets all styles.
     */
    resetStyles() {
      super.resetStyles();
      this.color = "black";
      this.align = "center";
      this.valign = "middle";
      this.family = StyleDefaultsConfig.fontFamily;
      this.size = StyleDefaultsConfig.fontSize;
      this.fontStyle = DEFAULT_FONTSTYLE;
      this.spacing = 2;
      this.spacingTop = 2;
      this.spacingRight = 2;
      this.spacingBottom = 2;
      this.spacingLeft = 2;
      this.horizontal = true;
      this.background = NONE;
      this.border = NONE;
      this.textDirection = DEFAULT_TEXT_DIRECTION;
      this.margin = null;
    }
    /**
     * Extends mxShape to update the text styles.
     *
     * @param state <CellState> of the corresponding cell.
     */
    apply(state) {
      var _a2, _b, _c, _d, _e, _f, _g, _h, _i, _j, _k, _l, _m, _n, _o, _p;
      const old = this.spacing;
      super.apply(state);
      if (this.style) {
        this.fontStyle = (_a2 = this.style.fontStyle) != null ? _a2 : this.fontStyle;
        this.family = (_b = this.style.fontFamily) != null ? _b : this.family;
        this.size = (_c = this.style.fontSize) != null ? _c : this.size;
        this.color = (_d = this.style.fontColor) != null ? _d : this.color;
        this.align = (_e = this.style.align) != null ? _e : this.align;
        this.valign = (_f = this.style.verticalAlign) != null ? _f : this.valign;
        this.spacing = (_g = this.style.spacing) != null ? _g : this.spacing;
        this.spacingTop = ((_h = this.style.spacingTop) != null ? _h : this.spacingTop - old) + this.spacing;
        this.spacingRight = ((_i = this.style.spacingRight) != null ? _i : this.spacingRight - old) + this.spacing;
        this.spacingBottom = ((_j = this.style.spacingBottom) != null ? _j : this.spacingBottom - old) + this.spacing;
        this.spacingLeft = ((_k = this.style.spacingLeft) != null ? _k : this.spacingLeft - old) + this.spacing;
        this.horizontal = (_l = this.style.horizontal) != null ? _l : this.horizontal;
        this.background = (_m = this.style.labelBackgroundColor) != null ? _m : this.background;
        this.border = (_n = this.style.labelBorderColor) != null ? _n : this.border;
        this.textDirection = (_o = this.style.textDirection) != null ? _o : DEFAULT_TEXT_DIRECTION;
        this.opacity = (_p = this.style.textOpacity) != null ? _p : 100;
        this.updateMargin();
      }
      this.flipV = false;
      this.flipH = false;
    }
    /**
     * Used to determine the automatic text direction.
     *
     * Returns 'ltr' or 'rtl' depending on the contents of {@link value}.
     *
     * This is not invoked for HTML, wrapped content or if {@link value} is a DOM node.
     */
    getAutoDirection() {
      const tmp = /[A-Za-z\u05d0-\u065f\u066a-\u06ef\u06fa-\u07ff\ufb1d-\ufdff\ufe70-\ufefc]/.exec(String(this.value));
      return tmp && tmp.length > 0 && tmp[0] > "z" ? "rtl" : "ltr";
    }
    /**
     * Returns the node that contains the rendered input.
     */
    getContentNode() {
      let result = this.node;
      if (result) {
        if (!result.ownerSVGElement) {
          result = this.node.firstChild.firstChild;
        } else {
          result = result.firstChild.firstChild.firstChild.firstChild.firstChild;
        }
      }
      return result;
    }
    /**
     * Updates the <boundingBox> for this shape using the given node and position.
     */
    updateBoundingBox() {
      var _a2, _b, _c, _d, _e;
      let { node } = this;
      this.boundingBox = this.bounds.clone();
      const rot = this.getTextRotation();
      const h = (_b = (_a2 = this.style) == null ? void 0 : _a2.labelPosition) != null ? _b : "center";
      const v = (_d = (_c = this.style) == null ? void 0 : _c.verticalLabelPosition) != null ? _d : "middle";
      if (!this.ignoreStringSize && node && this.overflow !== "fill" && (!this.clipped || !this.ignoreClippedStringSize || h !== "center" || v !== "middle")) {
        let ow = null;
        let oh = null;
        if (node.firstChild && node.firstChild.firstChild && node.firstChild.firstChild.nodeName === "foreignObject") {
          node = node.firstChild.firstChild.firstChild.firstChild;
          oh = node.offsetHeight * this.scale;
          if (this.overflow === "width") {
            ow = this.boundingBox.width;
          } else {
            ow = node.offsetWidth * this.scale;
          }
        } else {
          try {
            const b = node.getBBox();
            if (typeof this.value === "string" && ((_e = trim(this.value)) == null ? void 0 : _e.length) === 0) {
              this.boundingBox = null;
            } else if (b.width === 0 && b.height === 0) {
              this.boundingBox = null;
            } else {
              this.boundingBox = new Rectangle_default(b.x, b.y, b.width, b.height);
            }
            return;
          } catch (e) {
          }
        }
        if (ow && oh) {
          this.boundingBox = new Rectangle_default(this.bounds.x, this.bounds.y, ow, oh);
        }
      }
      if (this.boundingBox) {
        const margin = this.margin;
        if (rot !== 0) {
          const bbox = getBoundingBox(new Rectangle_default(margin.x * this.boundingBox.width, margin.y * this.boundingBox.height, this.boundingBox.width, this.boundingBox.height), rot, new Point_default(0, 0));
          this.unrotatedBoundingBox = Rectangle_default.fromRectangle(this.boundingBox);
          this.unrotatedBoundingBox.x += margin.x * this.unrotatedBoundingBox.width;
          this.unrotatedBoundingBox.y += margin.y * this.unrotatedBoundingBox.height;
          this.boundingBox.x += bbox.x;
          this.boundingBox.y += bbox.y;
          this.boundingBox.width = bbox.width;
          this.boundingBox.height = bbox.height;
        } else {
          this.boundingBox.x += margin.x * this.boundingBox.width;
          this.boundingBox.y += margin.y * this.boundingBox.height;
          this.unrotatedBoundingBox = null;
        }
      }
    }
    /**
     * Returns 0 to avoid using rotation in the canvas via updateTransform.
     */
    getShapeRotation() {
      return 0;
    }
    /**
     * Returns the rotation for the text label of the corresponding shape.
     */
    getTextRotation() {
      return this.state && this.state.shape ? this.state.shape.getTextRotation() : 0;
    }
    /**
     * Inverts the bounds if {@link Shape#isBoundsInverted} returns true or if the
     * horizontal style is false.
     */
    isPaintBoundsInverted() {
      return !this.horizontal && !!this.state && this.state.cell.isVertex();
    }
    /**
     * Sets the state of the canvas for drawing the shape.
     */
    configureCanvas(c, x, y, w, h) {
      super.configureCanvas(c, x, y, w, h);
      c.setFontColor(this.color);
      c.setFontBackgroundColor(this.background);
      c.setFontBorderColor(this.border);
      c.setFontFamily(this.family);
      c.setFontSize(this.size);
      c.setFontStyle(this.fontStyle);
    }
    /**
     * Private helper function to create SVG elements
     */
    getHtmlValue() {
      let val = this.value;
      if (this.dialect !== "strictHtml") {
        val = htmlEntities(val, false);
      }
      val = replaceTrailingNewlines(val, "<div><br></div>");
      val = this.replaceLinefeeds ? val.replace(/\n/g, "<br/>") : val;
      return val;
    }
    /**
     * Private helper function to create SVG elements
     */
    getTextCss() {
      const lh = ABSOLUTE_LINE_HEIGHT ? `${this.size * LINE_HEIGHT}px` : LINE_HEIGHT;
      let css = `display: inline-block; font-size: ${this.size}px; font-family: ${this.family}; color: ${this.color}; line-height: ${lh}; pointer-events: ${this.pointerEvents ? "all" : "none"}; `;
      matchBinaryMask(this.fontStyle, FONT_STYLE_MASK.BOLD) && (css += "font-weight: bold; ");
      matchBinaryMask(this.fontStyle, FONT_STYLE_MASK.ITALIC) && (css += "font-style: italic; ");
      const txtDecor = [];
      matchBinaryMask(this.fontStyle, FONT_STYLE_MASK.UNDERLINE) && txtDecor.push("underline");
      matchBinaryMask(this.fontStyle, FONT_STYLE_MASK.STRIKETHROUGH) && txtDecor.push("line-through");
      txtDecor.length > 0 && (css += `text-decoration: ${txtDecor.join(" ")}; `);
      return css;
    }
    /**
     * Updates the HTML node(s) to reflect the latest bounds and scale.
     */
    redrawHtmlShape() {
      const w = Math.max(0, Math.round(this.bounds.width / this.scale));
      const h = Math.max(0, Math.round(this.bounds.height / this.scale));
      const flex = `position: absolute; left: ${Math.round(this.bounds.x)}px; top: ${Math.round(this.bounds.y)}px; pointer-events: none; `;
      const block = this.getTextCss();
      const margin = this.margin;
      const node = this.node;
      SvgCanvas2D_default.createCss(w + 2, h, this.align, this.valign, this.wrap, this.overflow, this.clipped, this.background !== NONE ? htmlEntities(this.background, true) : null, this.border !== NONE ? htmlEntities(this.border, true) : null, flex, block, this.scale, (dx, dy, flex2, item, block2, ofl) => {
        const r = this.getTextRotation();
        let tr = (this.scale !== 1 ? `scale(${this.scale}) ` : "") + (r !== 0 ? `rotate(${r}deg) ` : "") + (margin.x !== 0 || margin.y !== 0 ? `translate(${margin.x * 100}%,${margin.y * 100}%)` : "");
        if (tr !== "") {
          tr = `transform-origin: 0 0; transform: ${tr}; `;
        }
        if (ofl === "") {
          flex2 += item;
          item = `display:inline-block; min-width: 100%; ${tr}`;
        } else {
          item += tr;
          if (Client_default.IS_SF) {
            item += "-webkit-clip-path: content-box;";
          }
        }
        if (this.opacity < 100) {
          block2 += `opacity: ${this.opacity / 100}; `;
        }
        node.setAttribute("style", flex2);
        const html = isNode(this.value) ? this.value.outerHTML : this.getHtmlValue();
        if (!node.firstChild) {
          node.innerHTML = `<div><div>${html}</div></div>`;
        }
        node.firstChild.firstChild.setAttribute("style", block2);
        node.firstChild.setAttribute("style", item);
      });
    }
    /**
     * Sets the inner HTML of the given element to the <value>.
     */
    updateInnerHtml(elt) {
      if (isNode(this.value)) {
        elt.innerHTML = this.value.outerHTML;
      } else {
        let val = this.value;
        if (this.dialect !== "strictHtml") {
          val = htmlEntities(val, false);
        }
        val = replaceTrailingNewlines(val, "<div>&nbsp;</div>");
        val = this.replaceLinefeeds ? val.replace(/\n/g, "<br/>") : val;
        val = `<div style="display:inline-block;_display:inline;">${val}</div>`;
        elt.innerHTML = val;
      }
    }
    /**
     * Updates the HTML node(s) to reflect the latest bounds and scale.
     */
    updateValue() {
      const node = this.node;
      if (isNode(this.value)) {
        node.innerHTML = "";
        node.appendChild(this.value);
      } else {
        let val = this.value;
        if (this.dialect !== "strictHtml") {
          val = htmlEntities(val, false);
        }
        val = replaceTrailingNewlines(val, "<div><br></div>");
        val = this.replaceLinefeeds ? val.replace(/\n/g, "<br/>") : val;
        const bg = this.background !== NONE ? this.background : null;
        const bd = this.border !== NONE ? this.border : null;
        if (this.overflow === "fill" || this.overflow === "width") {
          if (bg) {
            node.style.backgroundColor = bg;
          }
          if (bd) {
            node.style.border = `1px solid ${bd}`;
          }
        } else {
          let css = "";
          if (bg) {
            css += `background-color:${htmlEntities(bg, true)};`;
          }
          if (bd) {
            css += `border:1px solid ${htmlEntities(bd, true)};`;
          }
          const lh = ABSOLUTE_LINE_HEIGHT ? `${this.size * LINE_HEIGHT}px` : LINE_HEIGHT;
          val = `<div style="zoom:1;${css}display:inline-block;_display:inline;text-decoration:inherit;padding-bottom:1px;padding-right:1px;line-height:${lh}">${val}</div>`;
        }
        node.innerHTML = val;
        const divs = node.getElementsByTagName("div");
        if (divs.length > 0) {
          let dir = this.textDirection;
          if (dir === "auto" && this.dialect !== "strictHtml") {
            dir = this.getAutoDirection();
          }
          if (dir === "ltr" || dir === "rtl") {
            divs[divs.length - 1].setAttribute("dir", dir);
          } else {
            divs[divs.length - 1].removeAttribute("dir");
          }
        }
      }
    }
    /**
     * Updates the HTML node(s) to reflect the latest bounds and scale.
     */
    updateFont(node) {
      const { style } = node;
      style.lineHeight = ABSOLUTE_LINE_HEIGHT ? `${this.size * LINE_HEIGHT}px` : LINE_HEIGHT;
      style.fontSize = `${this.size}px`;
      style.fontFamily = this.family;
      style.verticalAlign = "top";
      style.color = this.color;
      matchBinaryMask(this.fontStyle, FONT_STYLE_MASK.BOLD) ? style.fontWeight = "bold" : style.fontWeight = "";
      matchBinaryMask(this.fontStyle, FONT_STYLE_MASK.ITALIC) ? style.fontStyle = "italic" : style.fontStyle = "";
      const txtDecor = [];
      matchBinaryMask(this.fontStyle, FONT_STYLE_MASK.UNDERLINE) && txtDecor.push("underline");
      matchBinaryMask(this.fontStyle, FONT_STYLE_MASK.STRIKETHROUGH) && txtDecor.push("line-through");
      txtDecor.length > 0 && (style.textDecoration = txtDecor.join(" "));
      if (this.align === "center") {
        style.textAlign = "center";
      } else if (this.align === "right") {
        style.textAlign = "right";
      } else {
        style.textAlign = "left";
      }
    }
    /**
     * Updates the HTML node(s) to reflect the latest bounds and scale.
     */
    updateSize(node, enableWrap = false) {
      const w = Math.max(0, Math.round(this.bounds.width / this.scale));
      const h = Math.max(0, Math.round(this.bounds.height / this.scale));
      const { style } = node;
      if (this.clipped) {
        style.overflow = "hidden";
        style.maxHeight = `${h}px`;
        style.maxWidth = `${w}px`;
      } else if (this.overflow === "fill") {
        style.width = `${w + 1}px`;
        style.height = `${h + 1}px`;
        style.overflow = "hidden";
      } else if (this.overflow === "width") {
        style.width = `${w + 1}px`;
        style.maxHeight = `${h + 1}px`;
        style.overflow = "hidden";
      }
      if (this.wrap && w > 0) {
        style.wordWrap = WORD_WRAP;
        style.whiteSpace = "normal";
        style.width = `${w}px`;
        if (enableWrap && this.overflow !== "fill" && this.overflow !== "width") {
          let sizeDiv = node;
          if (sizeDiv.firstChild != null && sizeDiv.firstChild.nodeName === "DIV") {
            sizeDiv = sizeDiv.firstChild;
            if (node.style.wordWrap === "break-word") {
              sizeDiv.style.width = "100%";
            }
          }
          let tmp = sizeDiv.offsetWidth;
          if (tmp === 0) {
            const prev = node.parentNode;
            node.style.visibility = "hidden";
            document.body.appendChild(node);
            tmp = sizeDiv.offsetWidth;
            node.style.visibility = "";
            prev.appendChild(node);
          }
          tmp += 3;
          if (this.clipped) {
            tmp = Math.min(tmp, w);
          }
          style.width = `${tmp}px`;
        }
      } else {
        style.whiteSpace = "nowrap";
      }
    }
    /**
     * Returns the spacing as an {@link Point}.
     */
    updateMargin() {
      this.margin = getAlignmentAsPoint(this.align, this.valign);
    }
    /**
     * Returns the spacing as an {@link Point}.
     */
    getSpacing() {
      let dx = 0;
      let dy = 0;
      if (this.align === "center") {
        dx = (this.spacingLeft - this.spacingRight) / 2;
      } else if (this.align === "right") {
        dx = -this.spacingRight - this.baseSpacingRight;
      } else {
        dx = this.spacingLeft + this.baseSpacingLeft;
      }
      if (this.valign === "middle") {
        dy = (this.spacingTop - this.spacingBottom) / 2;
      } else if (this.valign === "bottom") {
        dy = -this.spacingBottom - this.baseSpacingBottom;
      } else {
        dy = this.spacingTop + this.baseSpacingTop;
      }
      return new Point_default(dx, dy);
    }
  };
  var TextShape_default = TextShape;

  // node_modules/@maxgraph/core/lib/esm/view/shape/ShapeRegistry.js
  var ShapeRegistry = new BaseRegistry();

  // node_modules/@maxgraph/core/lib/esm/view/shape/stencil/StencilShapeRegistry.js
  var StencilShapeRegistry = new BaseRegistry();

  // node_modules/@maxgraph/core/lib/esm/view/cell/CellRenderer.js
  var placeholderStyleValues = ["inherit", "swimlane", "indicated"];
  var placeholderStyleProperties = [
    "fillColor",
    "strokeColor",
    "gradientColor",
    "fontColor"
  ];
  var CellRenderer = class {
    constructor() {
      this.defaultEdgeShape = ConnectorShape_default;
      this.defaultVertexShape = RectangleShape_default;
      this.defaultTextShape = TextShape_default;
      this.legacyControlPosition = true;
      this.legacySpacing = true;
      this.antiAlias = true;
      this.minSvgStrokeWidth = 1;
      this.forceControlClickHandler = false;
    }
    /**
     * Initializes the shape in the given state by calling its init method with
     * the correct container after configuring it using {@link configureShape}.
     *
     * @param state {@link CellState} for which the shape should be initialized.
     */
    initializeShape(state) {
      if (state.shape) {
        state.shape.dialect = state.view.graph.dialect;
        this.configureShape(state);
        state.shape.init(state.view.getDrawPane());
      }
    }
    /**
     * Creates and returns the shape for the given cell state.
     *
     * @param state {@link CellState} for which the shape should be created.
     */
    createShape(state) {
      const stencil = StencilShapeRegistry.get(state.style.shape);
      if (stencil) {
        return new Shape_default(stencil);
      }
      const shapeConstructor = this.getShapeConstructor(state);
      return new shapeConstructor();
    }
    /**
     * Creates the indicator shape for the given cell state.
     *
     * @param state {@link CellState} for which the indicator shape should be created.
     */
    createIndicatorShape(state) {
      if (state.shape) {
        state.shape.indicatorShape = this.getShape(state.getIndicatorShape());
      }
    }
    /**
     * Returns the shape for the given name from {@link ShapeRegistry}.
     */
    getShape(name) {
      return ShapeRegistry.get(name);
    }
    /**
     * Returns the constructor to be used for creating the shape.
     */
    getShapeConstructor(state) {
      let ctor = this.getShape(state.style.shape);
      if (!ctor) {
        ctor = state.cell.isEdge() ? this.defaultEdgeShape : this.defaultVertexShape;
      }
      return ctor;
    }
    /**
     * Configures the shape for the given cell state.
     *
     * @param state {@link CellState} for which the shape should be configured.
     */
    configureShape(state) {
      const shape = state.shape;
      if (shape) {
        shape.apply(state);
        shape.imageSrc = state.getImageSrc() || null;
        shape.indicatorColor = state.getIndicatorColor() || NONE;
        shape.indicatorStrokeColor = state.style.indicatorStrokeColor || NONE;
        shape.indicatorGradientColor = state.getIndicatorGradientColor() || NONE;
        if (state.style.indicatorDirection) {
          shape.indicatorDirection = state.style.indicatorDirection;
        }
        shape.indicatorImageSrc = state.getIndicatorImageSrc() || null;
        this.postConfigureShape(state);
      }
    }
    /**
     * Replaces any reserved words used for attributes, eg. inherit,
     * indicated or swimlane for colors in the shape for the given state.
     * This implementation resolves these keywords on the fill, stroke
     * and gradient color keys.
     */
    postConfigureShape(state) {
      if (state.shape) {
        this.resolveColor(state, "indicatorGradientColor", "gradientColor");
        this.resolveColor(state, "indicatorColor", "fillColor");
        this.resolveColor(state, "gradient", "gradientColor");
        this.resolveColor(state, "stroke", "strokeColor");
        this.resolveColor(state, "fill", "fillColor");
      }
    }
    /**
     * Check if style properties supporting placeholders requires resolution.
     */
    checkPlaceholderStyles(state) {
      for (const property of placeholderStyleProperties) {
        if (placeholderStyleValues.includes(state.style[property])) {
          return true;
        }
      }
      return false;
    }
    /**
     * Resolves special keywords 'inherit', 'indicated' and 'swimlane' and sets
     * the respective color on the shape.
     */
    resolveColor(state, field, key) {
      const shape = key === "fontColor" ? state.text : state.shape;
      if (shape) {
        const graph = state.view.graph;
        const value = shape[field];
        let referenced = null;
        if (value === "inherit") {
          referenced = state.cell.getParent();
        } else if (value === "swimlane") {
          shape[field] = key === "strokeColor" || key === "fontColor" ? "#000000" : "#ffffff";
          if (state.cell.getTerminal(false)) {
            referenced = state.cell.getTerminal(false);
          } else {
            referenced = state.cell;
          }
          referenced = graph.getSwimlane(referenced);
          key = graph.swimlaneIndicatorColorAttribute;
        } else if (value === "indicated" && state.shape) {
          shape[field] = state.shape.indicatorColor;
        } else if (key !== "fillColor" && value === "fillColor" && state.shape) {
          shape[field] = state.style.fillColor;
        } else if (key !== "strokeColor" && value === "strokeColor" && state.shape) {
          shape[field] = state.style.strokeColor;
        }
        if (referenced) {
          const rstate = graph.getView().getState(referenced);
          shape[field] = null;
          if (rstate) {
            const rshape = key === "fontColor" ? rstate.text : rstate.shape;
            if (rshape && field !== "indicatorColor") {
              shape[field] = rshape[field];
            } else {
              shape[field] = rstate.style[key];
            }
          }
        }
      }
    }
    /**
     * Returns the value to be used for the label.
     *
     * @param state {@link CellState} for which the label should be created.
     */
    getLabelValue(state) {
      const graph = state.view.graph;
      return graph.getLabel(state.cell);
    }
    /**
     * Creates the label for the given cell state.
     *
     * @param state {@link CellState} for which the label should be created.
     * @param value the label value.
     */
    createLabel(state, value) {
      var _a2, _b, _c;
      const graph = state.view.graph;
      if ((state.style.fontSize || 0) > 0 || state.style.fontSize == null) {
        const isForceHtml = graph.isHtmlLabel(state.cell) || isNode(value);
        state.text = new this.defaultTextShape(value, new Rectangle_default(), (_a2 = state.style.align) != null ? _a2 : "center", state.getVerticalAlign(), state.style.fontColor, state.style.fontFamily, state.style.fontSize, state.style.fontStyle, state.style.spacing, state.style.spacingTop, state.style.spacingRight, state.style.spacingBottom, state.style.spacingLeft, state.style.horizontal, state.style.labelBackgroundColor, state.style.labelBorderColor, graph.isWrapping(state.cell) && graph.isHtmlLabel(state.cell), graph.isLabelClipped(state.cell), state.style.overflow, state.style.labelPadding, (_b = state.style.textDirection) != null ? _b : DEFAULT_TEXT_DIRECTION);
        state.text.opacity = (_c = state.style.textOpacity) != null ? _c : 100;
        state.text.dialect = isForceHtml ? "strictHtml" : graph.dialect;
        state.text.style = state.style;
        state.text.state = state;
        this.initializeLabel(state, state.text);
        let forceGetCell = false;
        const getState = (evt) => {
          let result = state;
          if (Client_default.IS_TOUCH || forceGetCell) {
            const x = getClientX(evt);
            const y = getClientY(evt);
            const pt = convertPoint(graph.container, x, y);
            result = graph.view.getState(graph.getCellAt(pt.x, pt.y));
          }
          return result;
        };
        InternalEvent_default.addGestureListeners(state.text.node, (evt) => {
          if (this.isLabelEvent(state, evt)) {
            graph.fireMouseEvent(InternalEvent_default.MOUSE_DOWN, new InternalMouseEvent_default(evt, state));
            const source = getSource(evt);
            forceGetCell = // @ts-ignore nodeName should exist.
            graph.dialect !== "svg" && source.nodeName === "IMG";
          }
        }, (evt) => {
          if (this.isLabelEvent(state, evt)) {
            graph.fireMouseEvent(InternalEvent_default.MOUSE_MOVE, new InternalMouseEvent_default(evt, getState(evt)));
          }
        }, (evt) => {
          if (this.isLabelEvent(state, evt)) {
            graph.fireMouseEvent(InternalEvent_default.MOUSE_UP, new InternalMouseEvent_default(evt, getState(evt)));
            forceGetCell = false;
          }
        });
        if (graph.isNativeDblClickEnabled()) {
          InternalEvent_default.addListener(state.text.node, "dblclick", (evt) => {
            if (this.isLabelEvent(state, evt)) {
              graph.dblClick(evt, state.cell);
              InternalEvent_default.consume(evt);
            }
          });
        }
      }
    }
    /**
     * Initializes the label with a suitable container.
     *
     * @param state {@link CellState} whose label should be initialized.
     * @param shape {@link Shape} that represents the label.
     */
    initializeLabel(state, shape) {
      if (Client_default.IS_SVG && Client_default.NO_FO && shape.dialect !== "svg") {
        const graph = state.view.graph;
        shape.init(graph.container);
      } else {
        shape.init(state.view.getDrawPane());
      }
    }
    /**
     * Creates the actual shape for showing the overlay for the given cell state.
     *
     * @param state {@link CellState} for which the overlay should be created.
     */
    createCellOverlays(state) {
      const graph = state.view.graph;
      const cellOverlays = graph.getCellOverlays(state.cell);
      const createdOverlays = /* @__PURE__ */ new Map();
      for (const cellOverlay of cellOverlays) {
        const shape = state.overlays.get(cellOverlay);
        state.overlays.delete(cellOverlay);
        if (shape) {
          createdOverlays.set(cellOverlay, shape);
          continue;
        }
        const overlayShape = this.createOverlayShape(state, cellOverlay);
        overlayShape.dialect = graph.dialect;
        overlayShape.overlay = cellOverlay;
        this.initializeOverlay(state, overlayShape);
        this.installCellOverlayListeners(state, cellOverlay, overlayShape);
        this.configureOverlayShape(state, cellOverlay, overlayShape);
        createdOverlays.set(cellOverlay, overlayShape);
      }
      state.overlays.forEach((shape) => {
        shape.destroy();
      });
      state.overlays = createdOverlays;
    }
    /**
     * Create the Shape of the overlay.
     *
     * @param _state {@link CellState} for which the overlay shape should be created.
     * @param cellOverlay {@link CellOverlay} used to create the Shape of the overlay.
     * @since 0.16.0
     */
    createOverlayShape(_state, cellOverlay) {
      const overlayShape = new ImageShape_default(new Rectangle_default(), cellOverlay.image.src);
      overlayShape.preserveImageAspect = false;
      return overlayShape;
    }
    /**
     * Initializes the given overlay.
     *
     * @param state {@link CellState}  for which the overlay should be created.
     * @param overlay {@link Shape} that represents the overlay.
     */
    initializeOverlay(state, overlay) {
      overlay.init(state.view.getOverlayPane());
    }
    /**
     * Installs the listeners for the given {@link CellState} , {@link CellOverlay} and {@link Shape} that represents the overlay.
     */
    installCellOverlayListeners(state, overlay, shape) {
      const graph = state.view.graph;
      InternalEvent_default.addListener(shape.node, "click", (evt) => {
        if (graph.isEditing()) {
          graph.stopEditing(!graph.isInvokesStopCellEditing());
        }
        overlay.fireEvent(new EventObject_default(InternalEvent_default.CLICK, { event: evt, cell: state.cell }));
      });
      InternalEvent_default.addGestureListeners(shape.node, (evt) => {
        InternalEvent_default.consume(evt);
      }, (evt) => {
        graph.fireMouseEvent(InternalEvent_default.MOUSE_MOVE, new InternalMouseEvent_default(evt, state));
      });
      if (Client_default.IS_TOUCH) {
        InternalEvent_default.addListener(shape.node, "touchend", (evt) => {
          overlay.fireEvent(new EventObject_default(InternalEvent_default.CLICK, { event: evt, cell: state.cell }));
        });
      }
    }
    /**
     * Configure the Shape of the overlay. Generally, it is used to configure the DOM node of the Shape
     *
     * The default implementation set the cursor in the DOM node of the Shape based on the {@link CellOverlay.cursor}.
     *
     * @param _state {@link CellState} for which the overlay shape should be created.
     * @param cellOverlay {@link CellOverlay} used to create the Shape of the overlay.
     * @param overlayShape the {@link Shape} of the overlay.
     * @since 0.16.0
     */
    configureOverlayShape(_state, cellOverlay, overlayShape) {
      if (cellOverlay.cursor) {
        overlayShape.node.style.cursor = cellOverlay.cursor;
      }
    }
    /**
     * Creates the control for the given cell state.
     *
     * @param state {@link CellState}  for which the control should be created.
     */
    createControl(state) {
      const graph = state.view.graph;
      const image = graph.getFoldingImage(state);
      if (graph.isFoldingEnabled() && image) {
        if (!state.control) {
          const b = new Rectangle_default(0, 0, image.width, image.height);
          state.control = new ImageShape_default(b, image.src);
          state.control.preserveImageAspect = false;
          state.control.dialect = graph.dialect;
          this.initControl(state, state.control, true, this.createControlClickHandler(state));
        }
      } else if (state.control) {
        state.control.destroy();
        state.control = null;
      }
    }
    /**
     * Hook for creating the click handler for the folding icon.
     *
     * @param state {@link CellState}  whose control click handler should be returned.
     */
    createControlClickHandler(state) {
      const graph = state.view.graph;
      return (evt) => {
        if (this.forceControlClickHandler || graph.isEnabled()) {
          const collapse = !state.cell.isCollapsed();
          graph.foldCells(collapse, false, [state.cell], false, evt);
          InternalEvent_default.consume(evt);
        }
      };
    }
    /**
     * Initializes the given control and returns the corresponding DOM node.
     *
     * @param state {@link CellState} for which the control should be initialized.
     * @param control {@link Shape} to be initialized.
     * @param handleEvents Boolean indicating if mousedown and mousemove should fire events via the graph.
     * @param clickHandler Optional function to implement clicks on the control.
     */
    initControl(state, control, handleEvents, clickHandler) {
      const graph = state.view.graph;
      const isForceHtml = graph.isHtmlLabel(state.cell) && Client_default.NO_FO && graph.dialect === "svg";
      if (isForceHtml) {
        control.dialect = "preferHtml";
        control.init(graph.container);
        control.node.style.zIndex = String(1);
      } else {
        control.init(state.view.getOverlayPane());
      }
      const node = control.node;
      if (clickHandler && !Client_default.IS_IOS) {
        if (graph.isEnabled()) {
          node.style.cursor = "pointer";
        }
        InternalEvent_default.addListener(node, "click", clickHandler);
      }
      if (handleEvents) {
        let first = null;
        InternalEvent_default.addGestureListeners(node, (evt) => {
          first = new Point_default(getClientX(evt), getClientY(evt));
          graph.fireMouseEvent(InternalEvent_default.MOUSE_DOWN, new InternalMouseEvent_default(evt, state));
          InternalEvent_default.consume(evt);
        }, (evt) => {
          graph.fireMouseEvent(InternalEvent_default.MOUSE_MOVE, new InternalMouseEvent_default(evt, state));
        }, (evt) => {
          graph.fireMouseEvent(InternalEvent_default.MOUSE_UP, new InternalMouseEvent_default(evt, state));
          InternalEvent_default.consume(evt);
        });
        if (clickHandler && Client_default.IS_IOS) {
          node.addEventListener("touchend", (evt) => {
            if (first) {
              const tol = graph.getEventTolerance();
              if (Math.abs(first.x - getClientX(evt)) < tol && Math.abs(first.y - getClientY(evt)) < tol) {
                clickHandler.call(clickHandler, evt);
                InternalEvent_default.consume(evt);
              }
            }
          }, true);
        }
      }
      return node;
    }
    /**
     * Returns `true` if the event is for the shape of the given state.
     *
     * This implementation always returns `true`.
     *
     * @param state {@link CellState}  whose shape fired the event.
     * @param evt Mouse event which was fired.
     */
    isShapeEvent(state, evt) {
      return true;
    }
    /**
     * Returns `true` if the event is for the label of the given state.
     *
     * This implementation always returns `true`.
     *
     * @param state {@link CellState}  whose label fired the event.
     * @param evt Mouse event which was fired.
     */
    isLabelEvent(state, evt) {
      return true;
    }
    /**
     * Installs the event listeners for the given cell state.
     *
     * @param state {@link CellState}  for which the event listeners should be isntalled.
     */
    installListeners(state) {
      const graph = state.view.graph;
      const getState = (evt) => {
        let result = state;
        const source = getSource(evt);
        if (source && graph.dialect !== "svg" && // @ts-ignore nodeName should exist
        source.nodeName === "IMG" || Client_default.IS_TOUCH) {
          const x = getClientX(evt);
          const y = getClientY(evt);
          const pt = convertPoint(graph.container, x, y);
          const cell = graph.getCellAt(pt.x, pt.y);
          result = cell ? graph.view.getState(cell) : null;
        }
        return result;
      };
      if (state.shape) {
        InternalEvent_default.addGestureListeners(state.shape.node, (evt) => {
          if (this.isShapeEvent(state, evt)) {
            graph.fireMouseEvent(InternalEvent_default.MOUSE_DOWN, new InternalMouseEvent_default(evt, state));
          }
        }, (evt) => {
          if (this.isShapeEvent(state, evt)) {
            graph.fireMouseEvent(InternalEvent_default.MOUSE_MOVE, new InternalMouseEvent_default(evt, getState(evt)));
          }
        }, (evt) => {
          if (this.isShapeEvent(state, evt)) {
            graph.fireMouseEvent(InternalEvent_default.MOUSE_UP, new InternalMouseEvent_default(evt, getState(evt)));
          }
        });
        if (graph.isNativeDblClickEnabled()) {
          InternalEvent_default.addListener(state.shape.node, "dblclick", (evt) => {
            if (this.isShapeEvent(state, evt)) {
              graph.dblClick(evt, state.cell);
              InternalEvent_default.consume(evt);
            }
          });
        }
      }
    }
    /**
     * Redraws the label for the given cell state.
     *
     * @param state {@link CellState}  whose label should be redrawn.
     */
    redrawLabel(state, forced) {
      var _a2;
      const graph = state.view.graph;
      const value = this.getLabelValue(state);
      const wrapping = graph.isWrapping(state.cell);
      const clipping = graph.isLabelClipped(state.cell);
      const isForceHtml = graph.isHtmlLabel(state.cell) || value && isNode(value);
      const dialect = isForceHtml ? "strictHtml" : graph.dialect;
      const overflow = (_a2 = state.style.overflow) != null ? _a2 : "visible";
      if (state.text && (state.text.wrap !== wrapping || state.text.clipped !== clipping || state.text.overflow !== overflow || state.text.dialect !== dialect)) {
        state.text.destroy();
        state.text = null;
      }
      if (state.text == null && value != null && (isNode(value) || value.length > 0)) {
        this.createLabel(state, value);
      } else if (state.text != null && (value == null || value.length == 0)) {
        state.text.destroy();
        state.text = null;
      }
      if (state.text != null) {
        if (forced) {
          if (state.text.lastValue != null && this.isTextShapeInvalid(state, state.text)) {
            state.text.lastValue = null;
          }
          state.text.resetStyles();
          state.text.apply(state);
          state.text.valign = state.getVerticalAlign();
        }
        const bounds = this.getLabelBounds(state);
        const nextScale = this.getTextScale(state);
        this.resolveColor(state, "color", "fontColor");
        if (forced || state.text.value !== value || state.text.wrap !== wrapping || state.text.overflow !== overflow || state.text.clipped !== clipping || state.text.scale !== nextScale || state.text.dialect !== dialect || state.text.bounds == null || !state.text.bounds.equals(bounds)) {
          state.text.dialect = dialect;
          state.text.value = value;
          state.text.bounds = bounds;
          state.text.scale = nextScale;
          state.text.wrap = wrapping;
          state.text.clipped = clipping;
          state.text.overflow = overflow;
          const vis = state.text.node.style.visibility;
          this.redrawLabelShape(state.text);
          state.text.node.style.visibility = vis;
        }
      }
    }
    /**
     * Returns true if the style for the text shape has changed.
     *
     * @param state {@link CellState}  whose label should be checked.
     * @param shape {@link Text} shape to be checked.
     */
    isTextShapeInvalid(state, shape) {
      function check(property, styleName, defaultValue) {
        let result = false;
        if (styleName === "spacingTop" || styleName === "spacingRight" || styleName === "spacingBottom" || styleName === "spacingLeft") {
          result = // @ts-ignore
          Number.parseFloat(String(shape[property])) - Number.parseFloat(String(shape.spacing)) !== (state.style[styleName] || defaultValue);
        } else {
          result = shape[property] !== (state.style[styleName] || defaultValue);
        }
        return result;
      }
      return check("fontStyle", "fontStyle", DEFAULT_FONTSTYLE) || check("family", "fontFamily", StyleDefaultsConfig.fontFamily) || check("size", "fontSize", StyleDefaultsConfig.fontSize) || check("color", "fontColor", "black") || check("align", "align", "") || check("valign", "verticalAlign", "") || check("spacing", "spacing", 2) || check("spacingTop", "spacingTop", 0) || check("spacingRight", "spacingRight", 0) || check("spacingBottom", "spacingBottom", 0) || check("spacingLeft", "spacingLeft", 0) || check("horizontal", "horizontal", true) || check("background", "labelBackgroundColor", null) || check("border", "labelBorderColor", null) || check("opacity", "textOpacity", 100) || check("textDirection", "textDirection", DEFAULT_TEXT_DIRECTION);
    }
    /**
     * Called to invoked redraw on the given text shape.
     *
     * @param shape {@link Text} shape to be redrawn.
     */
    redrawLabelShape(shape) {
      shape.redraw();
    }
    /**
     * Returns the scaling used for the label of the given state
     *
     * @param state {@link CellState}  whose label scale should be returned.
     */
    getTextScale(state) {
      return state.view.scale;
    }
    /**
     * Returns the bounds to be used to draw the label of the given state.
     *
     * @param state {@link CellState}  whose label bounds should be returned.
     */
    getLabelBounds(state) {
      var _a2, _b, _c;
      const { scale } = state.view;
      const isEdge = state.cell.isEdge();
      let bounds = new Rectangle_default(state.absoluteOffset.x, state.absoluteOffset.y);
      if (isEdge) {
        const spacing = state.text.getSpacing();
        bounds.x += spacing.x * scale;
        bounds.y += spacing.y * scale;
        const geo = state.cell.getGeometry();
        if (geo != null) {
          bounds.width = Math.max(0, geo.width * scale);
          bounds.height = Math.max(0, geo.height * scale);
        }
      } else {
        if (state.text.isPaintBoundsInverted()) {
          const tmp = bounds.x;
          bounds.x = bounds.y;
          bounds.y = tmp;
        }
        bounds.x += state.x;
        bounds.y += state.y;
        bounds.width = Math.max(1, state.width);
        bounds.height = Math.max(1, state.height);
      }
      if (state.text.isPaintBoundsInverted()) {
        const t = (state.width - state.height) / 2;
        bounds.x += t;
        bounds.y -= t;
        const tmp = bounds.width;
        bounds.width = bounds.height;
        bounds.height = tmp;
      }
      if (state.shape != null) {
        const hpos = (_a2 = state.style.labelPosition) != null ? _a2 : "center";
        const vpos = (_b = state.style.verticalLabelPosition) != null ? _b : "middle";
        if (hpos === "center" && vpos === "middle") {
          bounds = state.shape.getLabelBounds(bounds);
        }
      }
      const lw = (_c = state.style.labelWidth) != null ? _c : null;
      if (lw != null) {
        bounds.width = lw * scale;
      }
      if (!isEdge) {
        this.rotateLabelBounds(state, bounds);
      }
      return bounds;
    }
    /**
     * Adds the shape rotation to the given label bounds and
     * applies the alignment and offsets.
     *
     * @param state {@link CellState}  whose label bounds should be rotated.
     * @param bounds {@link Rectangle} the rectangle to be rotated.
     */
    rotateLabelBounds(state, bounds) {
      var _a2, _b, _c;
      const textShape = state.text;
      bounds.y -= textShape.margin.y * bounds.height;
      bounds.x -= textShape.margin.x * bounds.width;
      if (!this.legacySpacing || state.style.overflow !== "fill" && state.style.overflow !== "width") {
        const s = state.view.scale;
        const spacing = textShape.getSpacing();
        bounds.x += spacing.x * s;
        bounds.y += spacing.y * s;
        const hpos = (_a2 = state.style.labelPosition) != null ? _a2 : "center";
        const vpos = (_b = state.style.verticalLabelPosition) != null ? _b : "middle";
        const lw = (_c = state.style.labelWidth) != null ? _c : null;
        bounds.width = Math.max(0, bounds.width - (hpos === "center" && lw == null ? textShape.spacingLeft * s + textShape.spacingRight * s : 0));
        bounds.height = Math.max(0, bounds.height - (vpos === "middle" ? textShape.spacingTop * s + textShape.spacingBottom * s : 0));
      }
      const theta = textShape.getTextRotation();
      if (theta !== 0 && state != null && // @ts-ignore
      state.cell.isVertex()) {
        const cx = state.getCenterX();
        const cy = state.getCenterY();
        if (bounds.x !== cx || bounds.y !== cy) {
          const rad = theta * (Math.PI / 180);
          const pt = getRotatedPoint(new Point_default(bounds.x, bounds.y), Math.cos(rad), Math.sin(rad), new Point_default(cx, cy));
          bounds.x = pt.x;
          bounds.y = pt.y;
        }
      }
    }
    /**
     * Redraws the overlays for the given cell state.
     *
     * @param state {@link CellState}  whose overlays should be redrawn.
     */
    redrawCellOverlays(state, forced = false) {
      var _a2;
      this.createCellOverlays(state);
      if (state.overlays) {
        const rot = mod((_a2 = state.style.rotation) != null ? _a2 : 0, 90);
        const rad = toRadians(rot);
        const cos = Math.cos(rad);
        const sin = Math.sin(rad);
        state.overlays.forEach((shape) => {
          var _a3, _b;
          const bounds = (_b = (_a3 = shape.overlay) == null ? void 0 : _a3.getBounds(state)) != null ? _b : null;
          if (bounds && !state.cell.isEdge() && state.shape && rot !== 0) {
            let cx = bounds.getCenterX();
            let cy = bounds.getCenterY();
            const point = getRotatedPoint(new Point_default(cx, cy), cos, sin, new Point_default(state.getCenterX(), state.getCenterY()));
            cx = point.x;
            cy = point.y;
            bounds.x = Math.round(cx - bounds.width / 2);
            bounds.y = Math.round(cy - bounds.height / 2);
          }
          if (forced || shape.bounds == null || shape.scale !== state.view.scale || !shape.bounds.equals(bounds)) {
            shape.bounds = bounds;
            shape.scale = state.view.scale;
            shape.redraw();
          }
        });
      }
    }
    /**
     * Redraws the control for the given cell state.
     *
     * @param state {@link CellState}  whose control should be redrawn.
     */
    redrawControl(state, forced = false) {
      var _a2;
      const image = state.view.graph.getFoldingImage(state);
      if (state.control != null && image != null) {
        const bounds = this.getControlBounds(state, image.width, image.height);
        const r = this.legacyControlPosition ? (_a2 = state.style.rotation) != null ? _a2 : 0 : state.shape.getTextRotation();
        const s = state.view.scale;
        if (forced || state.control.scale !== s || !state.control.bounds.equals(bounds) || state.control.rotation !== r) {
          state.control.rotation = r;
          state.control.bounds = bounds;
          state.control.scale = s;
          state.control.redraw();
        }
      }
    }
    /**
     * Returns the bounds to be used to draw the control (folding icon) of the given state.
     */
    getControlBounds(state, w, h) {
      var _a2;
      if (state.control != null) {
        const s = state.view.scale;
        let cx = state.getCenterX();
        let cy = state.getCenterY();
        if (!state.cell.isEdge()) {
          cx = state.x + w * s;
          cy = state.y + h * s;
          if (state.shape != null) {
            let rot = state.shape.getShapeRotation();
            if (this.legacyControlPosition) {
              rot = (_a2 = state.style.rotation) != null ? _a2 : 0;
            } else if (state.shape.isPaintBoundsInverted()) {
              const t = (state.width - state.height) / 2;
              cx += t;
              cy -= t;
            }
            if (rot !== 0) {
              const rad = toRadians(rot);
              const cos = Math.cos(rad);
              const sin = Math.sin(rad);
              const point = getRotatedPoint(new Point_default(cx, cy), cos, sin, new Point_default(state.getCenterX(), state.getCenterY()));
              cx = point.x;
              cy = point.y;
            }
          }
        }
        return state.cell.isEdge() ? new Rectangle_default(Math.round(cx - w / 2 * s), Math.round(cy - h / 2 * s), Math.round(w * s), Math.round(h * s)) : new Rectangle_default(Math.round(cx - w / 2 * s), Math.round(cy - h / 2 * s), Math.round(w * s), Math.round(h * s));
      }
      return null;
    }
    /**
     * Inserts the given {@link CellState} after the given nodes in the DOM.
     *
     * @param state {@link CellState} to be inserted.
     * @param node Node in {@link GraphView.drawPane} after which the shapes should be inserted.
     * @param htmlNode Node in the graph container after which the shapes should be inserted that
     * will not go into the {@link GraphView.drawPane} (e.g. HTML labels without foreignObjects).
     */
    insertStateAfter(state, node, htmlNode) {
      const graph = state.view.graph;
      const shapes = this.getShapesForState(state);
      for (let i = 0; i < shapes.length; i += 1) {
        if (shapes[i] != null && shapes[i].node != null) {
          const html = (
            // @ts-ignore
            shapes[i].node.parentNode !== state.view.getDrawPane() && // @ts-ignore
            shapes[i].node.parentNode !== state.view.getOverlayPane()
          );
          const temp = html ? htmlNode : node;
          if (temp != null && temp.nextSibling !== shapes[i].node) {
            if (temp.nextSibling == null) {
              temp.parentNode.appendChild(shapes[i].node);
            } else {
              temp.parentNode.insertBefore(shapes[i].node, temp.nextSibling);
            }
          } else if (temp == null) {
            const shapeNode = shapes[i].node;
            if (shapeNode.parentNode === graph.container) {
              let { canvas } = state.view;
              while (canvas != null && canvas.parentNode !== graph.container) {
                canvas = canvas.parentNode;
              }
              if (canvas != null && canvas.nextSibling != null) {
                if (canvas.nextSibling !== shapeNode) {
                  shapeNode.parentNode.insertBefore(shapeNode, canvas.nextSibling);
                }
              } else {
                shapeNode.parentNode.appendChild(shapeNode);
              }
            } else if (shapeNode.parentNode != null && shapeNode.parentNode.firstChild != null && shapeNode.parentNode.firstChild != shapeNode) {
              shapeNode.parentNode.insertBefore(shapeNode, shapeNode.parentNode.firstChild);
            }
          }
          if (html) {
            htmlNode = shapes[i].node;
          } else {
            node = shapes[i].node;
          }
        }
      }
      return [node, htmlNode];
    }
    /**
     * Returns the {@link Shape}s for the given cell state in the order in which they should appear in the DOM.
     *
     * @param state {@link CellState}  whose shapes should be returned.
     */
    getShapesForState(state) {
      return [state.shape, state.text, state.control];
    }
    /**
     * Updates the bounds or points and scale of the shapes for the given cell
     * state. This is called in mxGraphView.validatePoints as the last step of
     * updating all cells.
     *
     * @param state {@link CellState}  for which the shapes should be updated.
     * @param force Optional boolean that specifies if the cell should be reconfiured
     * and redrawn without any additional checks.
     * @param rendering Optional boolean that specifies if the cell should actually
     * be drawn into the DOM. If this is false then redraw and/or reconfigure
     * will not be called on the shape.
     */
    redraw(state, force = false, rendering = true) {
      const shapeChanged = this.redrawShape(state, force, rendering);
      if (state.shape != null && rendering) {
        this.redrawLabel(state, shapeChanged);
        this.redrawCellOverlays(state, shapeChanged);
        this.redrawControl(state, shapeChanged);
      }
    }
    /**
     * Redraws the shape for the given cell state.
     *
     * @param state {@link CellState}  whose label should be redrawn.
     */
    redrawShape(state, force = false, rendering = true) {
      let shapeChanged = false;
      const graph = state.view.graph;
      if (state.shape != null && state.shape.style != null && state.style != null && state.shape.style.shape !== state.style.shape) {
        state.shape.destroy();
        state.shape = null;
      }
      const selectionCellsHandler = graph.getPlugin("SelectionCellsHandler");
      if (state.shape == null && graph.container != null && state.cell !== state.view.currentRoot && (state.cell.isVertex() || state.cell.isEdge())) {
        state.shape = this.createShape(state);
        if (state.shape != null) {
          state.shape.minSvgStrokeWidth = this.minSvgStrokeWidth;
          state.shape.antiAlias = this.antiAlias;
          this.createIndicatorShape(state);
          this.initializeShape(state);
          this.createCellOverlays(state);
          this.installListeners(state);
          selectionCellsHandler == null ? void 0 : selectionCellsHandler.updateHandler(state);
        }
      } else if (!force && state.shape != null && (!equalEntries(state.shape.style, state.style) || this.checkPlaceholderStyles(state))) {
        state.shape.resetStyles();
        this.configureShape(state);
        selectionCellsHandler == null ? void 0 : selectionCellsHandler.updateHandler(state);
        force = true;
      }
      if (state.shape != null && state.shape.indicatorShape != this.getShape(state.getIndicatorShape())) {
        if (state.shape.indicator != null) {
          state.shape.indicator.destroy();
          state.shape.indicator = null;
        }
        this.createIndicatorShape(state);
        if (state.shape.indicatorShape != null) {
          state.shape.indicator = new state.shape.indicatorShape();
          state.shape.indicator.dialect = state.shape.dialect;
          state.shape.indicator.init(state.node);
          force = true;
        }
      }
      if (state.shape) {
        this.createControl(state);
        if (force || this.isShapeInvalid(state, state.shape)) {
          if (state.absolutePoints.length > 0) {
            state.shape.points = state.absolutePoints.slice();
            state.shape.bounds = null;
          } else {
            state.shape.points = [];
            state.shape.bounds = new Rectangle_default(state.x, state.y, state.width, state.height);
          }
          state.shape.scale = state.view.scale;
          if (rendering == null || rendering) {
            this.doRedrawShape(state);
          } else {
            state.shape.updateBoundingBox();
          }
          shapeChanged = true;
        }
      }
      return shapeChanged;
    }
    /**
     * Invokes redraw on the shape of the given state.
     */
    doRedrawShape(state) {
      var _a2;
      (_a2 = state.shape) == null ? void 0 : _a2.redraw();
    }
    /**
     * Returns true if the given shape must be repainted.
     */
    isShapeInvalid(state, shape) {
      return shape.bounds == null || shape.scale !== state.view.scale || state.absolutePoints.length === 0 && !shape.bounds.equals(state) || state.absolutePoints.length > 0 && !equalPoints(shape.points, state.absolutePoints);
    }
    /**
     * Destroys the shapes associated with the given cell state.
     *
     * @param state {@link CellState}  for which the shapes should be destroyed.
     */
    destroy(state) {
      if (state.shape) {
        if (state.text) {
          state.text.destroy();
          state.text = null;
        }
        state.overlays.forEach((shape) => {
          shape.destroy();
        });
        state.overlays = /* @__PURE__ */ new Map();
        if (state.control) {
          state.control.destroy();
          state.control = null;
        }
        state.shape.destroy();
        state.shape = null;
      }
    }
  };
  var CellRenderer_default = CellRenderer;

  // node_modules/@maxgraph/core/lib/esm/view/style/Stylesheet.js
  var Stylesheet = class {
    constructor() {
      this.styles = /* @__PURE__ */ new Map();
      this.putDefaultVertexStyle(this.createDefaultVertexStyle());
      this.putDefaultEdgeStyle(this.createDefaultEdgeStyle());
    }
    /**
     * Creates and returns the default vertex style.
     */
    createDefaultVertexStyle() {
      return {
        shape: "rectangle",
        perimeter: "rectanglePerimeter",
        verticalAlign: "middle",
        align: "center",
        fillColor: "#C3D9FF",
        strokeColor: "#6482B9",
        fontColor: "#774400"
      };
    }
    /**
     * Creates and returns the default edge style.
     */
    createDefaultEdgeStyle() {
      return {
        shape: "connector",
        endArrow: "classic",
        verticalAlign: "middle",
        align: "center",
        strokeColor: "#6482B9",
        fontColor: "#446299"
      };
    }
    /**
     * Sets the default style for vertices using `defaultVertex` as the style name.
     * @param style The style to be stored.
     */
    putDefaultVertexStyle(style) {
      this.putCellStyle("defaultVertex", style);
    }
    /**
     * Sets the default style for edges using `defaultEdge` as the style name.
     * @param style The style to be stored.
     */
    putDefaultEdgeStyle(style) {
      this.putCellStyle("defaultEdge", style);
    }
    /**
     * Returns the default style for vertices.
     */
    getDefaultVertexStyle() {
      return this.styles.get("defaultVertex");
    }
    /**
     * Returns the default style for edges.
     */
    getDefaultEdgeStyle() {
      return this.styles.get("defaultEdge");
    }
    /**
     * Stores the given {@link CellStateStyle} under the given name in {@link styles}.
     *
     * ### Example
     *
     * The following example adds a new style called `rounded` into an existing stylesheet:
     *
     * ```javascript
     * const style = {} as CellStateStyle;
     * style.shape = 'rectangle';
     * style.perimeter = 'rectanglePerimeter';
     * style.rounded = true;
     * graph.getStylesheet().putCellStyle('rounded', style);
     * ```
     *
     * ### Description
     *
     * Note that not all properties will be interpreted by all shapes. For example, the 'line' shape ignores the fill color.
     * The final call to this method associates the style with a name in the stylesheet.
     *
     * The style is used in a cell with the following code:
     * ```javascript
     * // model is an instance of GraphDataModel
     * // style is an instance of CellStyle
     * model.setStyle(cell, { baseStyleNames: ['rounded'] });
     * ```
     *
     * @param name Name for the style to be stored.
     * @param style The instance of the style to be stored.
     */
    putCellStyle(name, style) {
      this.styles.set(name, style);
    }
    /**
     * Returns a {@link CellStateStyle} computed by merging the default style, styles referenced in the specified `baseStyleNames`
     * and the properties of the `cellStyle` parameter.
     *
     * The properties are merged by taking the properties from various styles in the following order:
     *   - default style (if {@link CellStyle.ignoreDefaultStyle} is not set to `true`, otherwise it is ignored)
     *   - registered styles referenced in `baseStyleNames`, in the order of the array
     *   - `cellStyle` parameter
     *
     * To fully unset a style property i.e. the property is not set even if a value is set in the default style or in the referenced styles,
     * set the `cellStyle` property to `none`. For example. `cellStyle.fillColor = 'none'`
     *
     * @param cellStyle An object that represents the style.
     * @param defaultStyle Default style used as reference to compute the returned style.
     */
    getCellStyle(cellStyle, defaultStyle) {
      let style = cellStyle.ignoreDefaultStyle ? {} : __spreadValues({}, defaultStyle);
      if (cellStyle.baseStyleNames) {
        style = cellStyle.baseStyleNames.reduce((acc, styleName) => {
          return __spreadValues(__spreadValues({}, acc), this.styles.get(styleName));
        }, style);
      }
      for (const key of Object.keys(cellStyle)) {
        if (cellStyle[key] !== void 0) {
          cellStyle[key] == NONE ? delete style[key] : style[key] = cellStyle[key];
        }
      }
      "baseStyleNames" in style && delete style.baseStyleNames;
      "ignoreDefaultStyle" in style && delete style.ignoreDefaultStyle;
      return style;
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/undoable-change/SelectionChange.js
  var SelectionChange = class {
    constructor(graph, added = [], removed = []) {
      this.graph = graph;
      this.added = added.slice();
      this.removed = removed.slice();
    }
    /**
     * Applies the change to the selection model: calls {@link GraphSelectionModel.cellRemoved} for each cell
     * in {@link removed}, then {@link GraphSelectionModel.cellAdded} for each cell in {@link added}. Swaps
     * {@link added} and {@link removed} so a subsequent call undoes the change, then fires
     * {@link InternalEvent.CHANGE} on the selection model.
     *
     * **WARN**: because of the swap, the `added` and `removed` properties of the fired event refer to the
     * post-swap arrays — the event's `added` contains the cells just removed from the selection, and
     * vice-versa. This naming is preserved for historical reasons.
     */
    execute() {
      const selectionModel = this.graph.getSelectionModel();
      for (const removed of this.removed) {
        selectionModel.cellRemoved(removed);
      }
      for (const added of this.added) {
        selectionModel.cellAdded(added);
      }
      [this.added, this.removed] = [this.removed, this.added];
      selectionModel.fireEvent(new EventObject_default(InternalEvent_default.CHANGE, { added: this.added, removed: this.removed }));
    }
  };
  var SelectionChange_default = SelectionChange;

  // node_modules/@maxgraph/core/lib/esm/view/GraphSelectionModel.js
  var GraphSelectionModel = class extends EventSource_default {
    /**
     * Constructs a new graph selection model for the given {@link AbstractGraph}.
     * @param graph Reference to the enclosing {@link AbstractGraph}.
     */
    constructor(graph) {
      super();
      this.doneResource = isI18nEnabled() ? "done" : "";
      this.updatingSelectionResource = isI18nEnabled() ? "updatingSelection" : "";
      this.singleSelection = false;
      this.graph = graph;
      this.cells = [];
    }
    /**
     * Returns {@link singleSelection} as a boolean.
     */
    isSingleSelection() {
      return this.singleSelection;
    }
    /**
     * Sets the {@link singleSelection} flag.
     *
     * @param singleSelection the new value for {@link singleSelection}.
     */
    setSingleSelection(singleSelection) {
      this.singleSelection = singleSelection;
    }
    /**
     * Returns true if the given {@link Cell} is selected.
     */
    isSelected(cell) {
      return this.cells.includes(cell);
    }
    /**
     * Returns true if no cells are currently selected.
     */
    isEmpty() {
      return this.cells.length === 0;
    }
    /**
     * Clears the selection and fires a {@link InternalEvent.CHANGE} event if the selection was not empty.
     */
    clear() {
      this.changeSelection(null, this.cells);
    }
    /**
     * Selects the specified {@link Cell} using {@link setCells}.
     *
     * @param cell {@link Cell} to be selected.
     */
    setCell(cell) {
      this.setCells(cell ? [cell] : []);
    }
    /**
     * Selects the given array of {@link Cell} and fires a {@link InternalEvent.CHANGE} event.
     *
     * @param cells Array of {@link Cell} to be selected.
     */
    setCells(cells) {
      if (this.singleSelection) {
        const firstSelectable = this.getFirstSelectableCell(cells);
        this.changeSelection(firstSelectable ? [firstSelectable] : [], this.cells);
        return;
      }
      const selectable = cells.filter((cell) => this.graph.isCellSelectable(cell));
      this.changeSelection(selectable, this.cells);
    }
    /**
     * Returns the first selectable cell in the given array of cells.
     *
     * @returns the first cell for which {@link AbstractGraph.isCellSelectable} returns `true`, or `null` if no
     * such cell exists (including when `cells` is empty).
     */
    getFirstSelectableCell(cells) {
      var _a2;
      return (_a2 = cells.find((cell) => this.graph.isCellSelectable(cell))) != null ? _a2 : null;
    }
    /**
     * Adds the given {@link Cell} to the selection and fires a {@link InternalEvent.CHANGE} event.
     *
     * @param cell {@link Cell} to add to the selection.
     */
    addCell(cell) {
      this.addCells([cell]);
    }
    /**
     * Adds the given array of {@link Cell} to the selection and fires a {@link InternalEvent.CHANGE} event.
     *
     * @param cells Array of {@link Cell} to add to the selection.
     */
    addCells(cells) {
      if (this.singleSelection) {
        const firstSelectable = this.getFirstSelectableCell(cells);
        const toAdd2 = firstSelectable && !this.isSelected(firstSelectable) ? [firstSelectable] : [];
        this.changeSelection(toAdd2, this.cells);
        return;
      }
      const toAdd = cells.filter((cell) => !this.isSelected(cell) && this.graph.isCellSelectable(cell));
      this.changeSelection(toAdd, null);
    }
    /**
     * Removes the specified {@link Cell} from the selection and fires a {@link InternalEvent.CHANGE} event
     * for the remaining cells.
     *
     * @param cell {@link Cell} to remove from the selection.
     */
    removeCell(cell) {
      this.removeCells([cell]);
    }
    /**
     * Removes the specified {@link Cell} from the selection and fires a {@link InternalEvent.CHANGE} event
     * for the remaining cells.
     *
     * @param cells {@link Cell}s to remove from the selection.
     */
    removeCells(cells) {
      const toRemove = cells.filter((cell) => this.isSelected(cell));
      this.changeSelection(null, toRemove);
    }
    /**
     * Adds/removes the specified arrays of {@link Cell} to/from the selection.
     *
     * @param added Array of {@link Cell} to add to the selection.
     * @param removed Array of {@link Cell} to remove from the selection.
     */
    changeSelection(added = null, removed = null) {
      const toAdd = (added != null ? added : []).filter((cell) => cell != null);
      const toRemove = (removed != null ? removed : []).filter((cell) => cell != null);
      if (toAdd.length > 0 || toRemove.length > 0) {
        const change = new SelectionChange_default(this.graph, toAdd, toRemove);
        change.execute();
        const edit = new UndoableEdit_default(this.graph, false);
        edit.add(change);
        this.fireEvent(new EventObject_default(InternalEvent_default.UNDO, { edit }));
      }
    }
    /**
     * Inner callback to add the specified {@link Cell} to the selection. No event
     * is fired in this implementation.
     *
     * @param cell {@link Cell} to add to the selection.
     */
    cellAdded(cell) {
      if (!this.isSelected(cell)) {
        this.cells.push(cell);
      }
    }
    /**
     * Inner callback to remove the specified {@link Cell} from the selection. No
     * event is fired in this implementation.
     *
     * @param cell {@link Cell} to remove from the selection.
     */
    cellRemoved(cell) {
      const index = this.cells.indexOf(cell);
      if (index >= 0) {
        this.cells.splice(index, 1);
      }
    }
  };
  var GraphSelectionModel_default = GraphSelectionModel;

  // node_modules/@maxgraph/core/lib/esm/view/undoable-change/CurrentRootChange.js
  var CurrentRootChange = class {
    constructor(view, root) {
      this.view = view;
      this.root = root;
      this.previous = root;
      this.isUp = root === null;
      if (!this.isUp) {
        let tmp = this.view.currentRoot;
        while (tmp) {
          if (tmp === root) {
            this.isUp = true;
            break;
          }
          tmp = tmp.getParent();
        }
      }
    }
    /**
     * Changes the current root of the view.
     */
    execute() {
      const tmp = this.view.currentRoot;
      this.view.currentRoot = this.previous;
      this.previous = tmp;
      const translate2 = this.view.graph.getTranslateForRoot(this.view.currentRoot);
      if (translate2) {
        this.view.translate = new Point_default(-translate2.x, -translate2.y);
      }
      if (this.isUp) {
        this.view.clear(this.view.currentRoot, true, true);
        this.view.validate(null);
      } else {
        this.view.refresh();
      }
      const name = this.isUp ? InternalEvent_default.UP : InternalEvent_default.DOWN;
      this.view.fireEvent(new EventObject_default(name, { root: this.view.currentRoot, previous: this.previous }));
      this.isUp = !this.isUp;
    }
  };
  var CurrentRootChange_default = CurrentRootChange;

  // node_modules/@maxgraph/core/lib/esm/view/style/perimeter/PerimeterRegistry.js
  var PerimeterRegistry = new BaseRegistry();

  // node_modules/@maxgraph/core/lib/esm/view/GraphView.js
  var GraphView = class extends EventSource_default {
    constructor(graph) {
      super();
      this.backgroundImage = null;
      this.backgroundPageShape = null;
      this.EMPTY_POINT = new Point_default();
      this.doneResource = isI18nEnabled() ? "done" : "";
      this.updatingDocumentResource = isI18nEnabled() ? "updatingDocument" : "";
      this.allowEval = false;
      this.captureDocumentGesture = true;
      this.rendering = true;
      this.currentRoot = null;
      this.graphBounds = new Rectangle_default();
      this.scale = 1;
      this.translate = new Point_default();
      this.states = /* @__PURE__ */ new Map();
      this.updateStyle = false;
      this.lastNode = null;
      this.lastHtmlNode = null;
      this.lastForegroundNode = null;
      this.lastForegroundHtmlNode = null;
      this.endHandler = null;
      this.moveHandler = null;
      this.graph = graph;
    }
    /**
     * Returns {@link graphBounds}.
     */
    getGraphBounds() {
      return this.graphBounds;
    }
    /**
     * Sets {@link graphBounds}.
     */
    setGraphBounds(value) {
      this.graphBounds = value;
    }
    /**
     * Returns the {@link scale}.
     */
    getScale() {
      return this.scale;
    }
    /**
     * Sets the scale and fires a {@link scale} event before calling {@link revalidate} followed
     * by {@link AbstractGraph.sizeDidChange}.
     *
     * @param value Decimal value that specifies the new scale (1 is 100%).
     */
    setScale(value) {
      const previousScale = this.scale;
      if (previousScale !== value) {
        this.scale = value;
        if (this.isEventsEnabled()) {
          this.viewStateChanged();
        }
      }
      this.fireEvent(new EventObject_default(InternalEvent_default.SCALE, { scale: value, previousScale }));
    }
    /**
     * Returns the {@link translate}.
     */
    getTranslate() {
      return this.translate;
    }
    isRendering() {
      return this.rendering;
    }
    setRendering(value) {
      this.rendering = value;
    }
    /**
     * Sets the translation and fires a {@link translate} event before calling
     * {@link revalidate} followed by {@link AbstractGraph.sizeDidChange}. The translation is the
     * negative of the origin.
     *
     * @param dx X-coordinate of the translation.
     * @param dy Y-coordinate of the translation.
     */
    setTranslate(dx, dy) {
      const previousTranslate = new Point_default(this.translate.x, this.translate.y);
      if (this.translate.x !== dx || this.translate.y !== dy) {
        this.translate.x = dx;
        this.translate.y = dy;
        if (this.isEventsEnabled()) {
          this.viewStateChanged();
        }
      }
      this.fireEvent(new EventObject_default(InternalEvent_default.TRANSLATE, {
        translate: this.translate,
        previousTranslate
      }));
    }
    /**
     * Returns {@link allowEval}.
     */
    isAllowEval() {
      return this.allowEval;
    }
    /**
     * Sets {@link allowEval}.
     */
    setAllowEval(value) {
      this.allowEval = value;
    }
    /**
     * Returns {@link states}.
     */
    getStates() {
      return this.states;
    }
    /**
     * Sets {@link states}.
     */
    setStates(value) {
      this.states = value;
    }
    /**
     * Returns the DOM node that contains the background-, draw-, overlay- and decorator- panes.
     */
    getCanvas() {
      return this.canvas;
    }
    /**
     * Returns the DOM node that represents the background layer.
     */
    getBackgroundPane() {
      return this.backgroundPane;
    }
    /**
     * Returns the DOM node that represents the main drawing layer.
     */
    getDrawPane() {
      return this.drawPane;
    }
    /**
     * Returns the DOM node that represents the layer above the drawing layer.
     */
    getOverlayPane() {
      return this.overlayPane;
    }
    /**
     * Returns the DOM node that represents the topmost drawing layer.
     */
    getDecoratorPane() {
      return this.decoratorPane;
    }
    /**
     * Returns the union of all {@link CellState}s for the given array of {@link Cell}.
     *
     * @param cells Array of {@link Cell} whose bounds should be returned.
     */
    getBounds(cells) {
      let result = null;
      if (cells.length > 0) {
        for (let i = 0; i < cells.length; i += 1) {
          if (cells[i].isVertex() || cells[i].isEdge()) {
            const state = this.getState(cells[i]);
            if (state) {
              if (!result) {
                result = Rectangle_default.fromRectangle(state);
              } else {
                result.add(state);
              }
            }
          }
        }
      }
      return result;
    }
    /**
     * Sets and returns the current root and fires an {@link undo} event before
     * calling {@link AbstractGraph.sizeDidChange}.
     *
     * @param root {@link Cell} that specifies the root of the displayed cell hierarchy.
     */
    setCurrentRoot(root) {
      if (this.currentRoot !== root) {
        const change = new CurrentRootChange_default(this, root);
        change.execute();
        const edit = new UndoableEdit_default(this, true);
        edit.add(change);
        this.fireEvent(new EventObject_default(InternalEvent_default.UNDO, { edit }));
        this.graph.sizeDidChange();
        this.currentRoot = root;
      }
      return root;
    }
    /**
     * Sets the scale and translation and fires a {@link scale} and {@link translate} event
     * before calling {@link revalidate} followed by {@link AbstractGraph.sizeDidChange}.
     *
     * @param scale Decimal value that specifies the new scale (1 is 100%).
     * @param dx X-coordinate of the translation.
     * @param dy Y-coordinate of the translation.
     */
    scaleAndTranslate(scale, dx, dy) {
      const previousScale = this.scale;
      const previousTranslate = new Point_default(this.translate.x, this.translate.y);
      if (this.scale !== scale || this.translate.x !== dx || this.translate.y !== dy) {
        this.scale = scale;
        this.translate.x = dx;
        this.translate.y = dy;
        if (this.isEventsEnabled()) {
          this.viewStateChanged();
        }
      }
      this.fireEvent(new EventObject_default(InternalEvent_default.SCALE_AND_TRANSLATE, {
        scale,
        previousScale,
        translate: this.translate,
        previousTranslate
      }));
    }
    /**
     * Invoked after {@link scale} and/or {@link translate} has changed.
     */
    viewStateChanged() {
      this.revalidate();
      this.graph.sizeDidChange();
    }
    /**
     * Clears the view if {@link currentRoot} is not null and revalidates.
     */
    refresh() {
      if (this.currentRoot) {
        this.clear();
      }
      this.revalidate();
    }
    /**
     * Revalidates the complete view with all cell states.
     */
    revalidate() {
      this.invalidate();
      this.validate();
    }
    /**
     * Removes the state of the given cell and all descendants if the given cell is not the current root.
     *
     * @param cell Optional {@link Cell} for which the state should be removed. Default is the root of the model.
     * @param force Optional boolean indicating if the current root should be ignored for recursion. Default is `false`.
     * @param recurse Optional boolean indicating if the descendants should be cleared as well. Default is `true`.
     */
    clear(cell, force = false, recurse = true) {
      if (!cell) {
        cell = this.graph.getDataModel().getRoot();
      }
      if (cell) {
        this.removeState(cell);
        if (recurse && (force || cell !== this.currentRoot)) {
          const childCount = cell.getChildCount();
          for (let i = 0; i < childCount; i += 1) {
            this.clear(cell.getChildAt(i), force);
          }
        } else {
          this.invalidate(cell);
        }
      }
    }
    /**
     * Invalidates the state of the given cell, all its descendants and
     * connected edges.
     *
     * @param cell Optional {@link Cell} to be invalidated. Default is the root of the
     * model.
     */
    invalidate(cell = null, recurse = true, includeEdges = true) {
      const model = this.graph.getDataModel();
      cell = cell != null ? cell : model.getRoot();
      if (cell) {
        const state = this.getState(cell);
        if (state) {
          state.invalid = true;
        }
        if (!cell.invalidating) {
          cell.invalidating = true;
          if (recurse) {
            const childCount = cell.getChildCount();
            for (let i = 0; i < childCount; i += 1) {
              const child = cell.getChildAt(i);
              this.invalidate(child, recurse, includeEdges);
            }
          }
          if (includeEdges) {
            const edgeCount = cell.getEdgeCount();
            for (let i = 0; i < edgeCount; i += 1) {
              this.invalidate(cell.getEdgeAt(i), recurse, includeEdges);
            }
          }
          cell.invalidating = false;
        }
      }
    }
    /**
     * Calls {@link validateCell} and {@link validateCellState} and updates the {@link graphBounds}
     * using {@link getBoundingBox}. Finally, the background is validated using
     * {@link validateBackground}.
     *
     * @param cell Optional {@link Cell} to be used as the root of the validation.
     * Default is {@link currentRoot} or the root of the model.
     */
    validate(cell = null) {
      var _a2;
      const t0 = log().enter("GraphView.validate");
      this.resetValidationState();
      const c = cell || ((_a2 = this.currentRoot) != null ? _a2 : this.graph.getDataModel().getRoot());
      if (c) {
        const graphBounds = this.getBoundingBox(this.validateCellState(c ? this.validateCell(c) : null));
        this.setGraphBounds(graphBounds != null ? graphBounds : this.getEmptyBounds());
        this.validateBackground();
        this.resetValidationState();
      }
      log().leave("GraphView.validate", t0);
    }
    /**
     * Returns the bounds for an empty graph. This returns a rectangle at
     * {@link translate} with the size of 0 x 0.
     */
    getEmptyBounds() {
      return new Rectangle_default(this.translate.x * this.scale, this.translate.y * this.scale);
    }
    /**
     * Returns the bounding box of the shape and the label for the given
     * {@link CellState} and its children if recurse is true.
     *
     * @param state {@link CellState} whose bounding box should be returned.
     * @param recurse Optional boolean indicating if the children should be included.
     * Default is true.
     */
    getBoundingBox(state = null, recurse = true) {
      let bbox = null;
      if (state) {
        if (state.shape && state.shape.boundingBox) {
          bbox = state.shape.boundingBox.clone();
        }
        if (state.text && state.text.boundingBox) {
          if (bbox) {
            bbox.add(state.text.boundingBox);
          } else {
            bbox = state.text.boundingBox.clone();
          }
        }
        if (recurse) {
          const childCount = state.cell.getChildCount();
          for (let i = 0; i < childCount; i += 1) {
            const bounds = this.getBoundingBox(this.getState(state.cell.getChildAt(i)));
            if (bounds) {
              if (!bbox) {
                bbox = bounds;
              } else {
                bbox.add(bounds);
              }
            }
          }
        }
      }
      return bbox;
    }
    /**
     * Creates and returns the shape used as the background page.
     *
     * @param bounds {@link Rectangle} that represents the bounds of the shape.
     */
    createBackgroundPageShape(bounds) {
      return new RectangleShape_default(bounds, "white", "black");
    }
    /**
     * Calls {@link validateBackgroundImage} and {@link validateBackgroundPage}.
     */
    validateBackground() {
      this.validateBackgroundImage();
      this.validateBackgroundPage();
    }
    /**
     * Validates the background image.
     */
    validateBackgroundImage() {
      const bg = this.graph.getBackgroundImage();
      if (bg) {
        if (!this.backgroundImage || this.backgroundImage.imageSrc !== bg.src) {
          if (this.backgroundImage) {
            this.backgroundImage.destroy();
          }
          const bounds = new Rectangle_default(0, 0, 1, 1);
          this.backgroundImage = new ImageShape_default(bounds, bg.src);
          this.backgroundImage.dialect = this.graph.dialect;
          this.backgroundImage.init(this.backgroundPane);
          this.backgroundImage.redraw();
        }
        this.redrawBackgroundImage(this.backgroundImage, bg);
      } else if (this.backgroundImage) {
        this.backgroundImage.destroy();
        this.backgroundImage = null;
      }
    }
    /**
     * Validates the background page.
     */
    validateBackgroundPage() {
      const graph = this.graph;
      if (graph.pageVisible) {
        const bounds = this.getBackgroundPageBounds();
        if (this.backgroundPageShape == null) {
          this.backgroundPageShape = this.createBackgroundPageShape(bounds);
          this.backgroundPageShape.scale = this.scale;
          this.backgroundPageShape.isShadow = true;
          this.backgroundPageShape.dialect = this.graph.dialect;
          this.backgroundPageShape.init(this.backgroundPane);
          this.backgroundPageShape.redraw();
          if (this.backgroundPageShape.node) {
            if (graph.isNativeDblClickEnabled()) {
              InternalEvent_default.addListener(this.backgroundPageShape.node, "dblclick", ((evt) => {
                graph.dblClick(evt);
              }));
            }
            InternalEvent_default.addGestureListeners(this.backgroundPageShape.node, (evt) => {
              graph.fireMouseEvent(InternalEvent_default.MOUSE_DOWN, new InternalMouseEvent_default(evt));
            }, (evt) => {
              const tooltipHandler = graph.getPlugin("TooltipHandler");
              if (tooltipHandler && tooltipHandler.isHideOnHover()) {
                tooltipHandler.hide();
              }
              if (graph.isMouseDown && !isConsumed(evt)) {
                graph.fireMouseEvent(InternalEvent_default.MOUSE_MOVE, new InternalMouseEvent_default(evt));
              }
            }, (evt) => {
              graph.fireMouseEvent(InternalEvent_default.MOUSE_UP, new InternalMouseEvent_default(evt));
            });
          }
        } else {
          this.backgroundPageShape.scale = this.scale;
          this.backgroundPageShape.bounds = bounds;
          this.backgroundPageShape.redraw();
        }
      } else if (this.backgroundPageShape) {
        this.backgroundPageShape.destroy();
        this.backgroundPageShape = null;
      }
    }
    /**
     * Returns the bounds for the background page.
     */
    getBackgroundPageBounds() {
      const fmt = this.graph.pageFormat;
      const ps = this.scale * this.graph.pageScale;
      return new Rectangle_default(this.scale * this.translate.x, this.scale * this.translate.y, fmt.width * ps, fmt.height * ps);
    }
    /**
     * Updates the bounds and redraws the background image.
     *
     * Example:
     *
     * If the background image should not be scaled, this can be replaced with
     * the following.
     *
     * @example
     * ```javascript
     * redrawBackground(backgroundImage, bg)
     * {
     *   backgroundImage.bounds.x = this.translate.x;
     *   backgroundImage.bounds.y = this.translate.y;
     *   backgroundImage.bounds.width = bg.width;
     *   backgroundImage.bounds.height = bg.height;
     *
     *   backgroundImage.redraw();
     * };
     * ```
     *
     * @param backgroundImage {@link ImageShape} that represents the background image.
     * @param bg {@link Image} that specifies the image and its dimensions.
     */
    redrawBackgroundImage(backgroundImage, bg) {
      backgroundImage.scale = this.scale;
      if (backgroundImage.bounds) {
        const bounds = backgroundImage.bounds;
        bounds.x = this.scale * this.translate.x;
        bounds.y = this.scale * this.translate.y;
        bounds.width = this.scale * bg.width;
        bounds.height = this.scale * bg.height;
      }
      backgroundImage.redraw();
    }
    /**
     * Recursively creates the cell state for the given cell if visible is true and
     * the given cell is visible. If the cell is not visible but the state exists
     * then it is removed using {@link removeState}.
     *
     * @param cell {@link Cell} whose {@link CellState} should be created.
     * @param visible Optional boolean indicating if the cell should be visible. Default
     * is true.
     */
    validateCell(cell, visible = true) {
      visible = visible && cell.isVisible();
      const state = this.getState(cell, visible);
      if (state && !visible) {
        this.removeState(cell);
      } else {
        const childCount = cell.getChildCount();
        for (let i = 0; i < childCount; i += 1) {
          this.validateCell(cell.getChildAt(i), visible && (!cell.isCollapsed() || cell === this.currentRoot));
        }
      }
      return cell;
    }
    /**
     * Validates and repaints the {@link CellState} for the given {@link Cell}.
     *
     * @param cell {@link Cell} whose {@link CellState} should be validated.
     * @param recurse Optional boolean indicating if the children of the cell should be
     * validated. Default is true.
     */
    validateCellState(cell, recurse = true) {
      let state = null;
      if (cell) {
        state = this.getState(cell);
        if (state) {
          if (state.invalid) {
            state.invalid = false;
            if (!state.style || state.invalidStyle) {
              state.style = this.graph.getCellStyle(state.cell);
              state.invalidStyle = false;
            }
            if (cell !== this.currentRoot) {
              this.validateCellState(cell.getParent(), false);
            }
            state.setVisibleTerminalState(this.validateCellState(this.getVisibleTerminal(cell, true), false), true);
            state.setVisibleTerminalState(this.validateCellState(this.getVisibleTerminal(cell, false), false), false);
            this.updateCellState(state);
            if (cell !== this.currentRoot && !state.invalid) {
              this.graph.cellRenderer.redraw(state, false, this.isRendering());
              state.updateCachedBounds();
            }
          }
          if (recurse && !state.invalid) {
            if (state.shape) {
              this.stateValidated(state);
            }
            const childCount = cell.getChildCount();
            for (let i = 0; i < childCount; i += 1) {
              this.validateCellState(cell.getChildAt(i));
            }
          }
        }
      }
      return state;
    }
    /**
     * Updates the given {@link CellState}.
     *
     * @param state {@link CellState} to be updated.
     */
    updateCellState(state) {
      const absoluteOffset = state.absoluteOffset;
      const origin = state.origin;
      absoluteOffset.x = 0;
      absoluteOffset.y = 0;
      origin.x = 0;
      origin.y = 0;
      state.length = 0;
      if (state.cell !== this.currentRoot) {
        const parent = state.cell.getParent();
        const pState = parent ? this.getState(parent) : null;
        if (pState && pState.cell !== this.currentRoot) {
          origin.x += pState.origin.x;
          origin.y += pState.origin.y;
        }
        let offset = this.graph.getChildOffsetForCell(state.cell);
        if (offset) {
          origin.x += offset.x;
          origin.y += offset.y;
        }
        const geo = state.cell.getGeometry();
        if (geo) {
          if (!state.cell.isEdge()) {
            offset = geo.offset ? geo.offset : this.EMPTY_POINT;
            if (geo.relative && pState) {
              if (pState.cell.isEdge()) {
                const point = this.getPoint(pState, geo);
                if (point) {
                  origin.x += point.x / this.scale - pState.origin.x - this.translate.x;
                  origin.y += point.y / this.scale - pState.origin.y - this.translate.y;
                }
              } else {
                origin.x += geo.x * pState.unscaledWidth + offset.x;
                origin.y += geo.y * pState.unscaledHeight + offset.y;
              }
            } else {
              absoluteOffset.x = this.scale * offset.x;
              absoluteOffset.y = this.scale * offset.y;
              origin.x += geo.x;
              origin.y += geo.y;
            }
          }
          state.x = this.scale * (this.translate.x + origin.x);
          state.y = this.scale * (this.translate.y + origin.y);
          state.width = this.scale * geo.width;
          state.unscaledWidth = geo.width;
          state.height = this.scale * geo.height;
          state.unscaledHeight = geo.height;
          if (state.cell.isVertex()) {
            this.updateVertexState(state, geo);
          }
          if (state.cell.isEdge()) {
            this.updateEdgeState(state, geo);
          }
        }
      }
      state.updateCachedBounds();
    }
    /**
     * Validates the given cell state.
     */
    updateVertexState(state, geo) {
      var _a2;
      const parent = state.cell.getParent();
      const pState = parent ? this.getState(parent) : null;
      if (geo.relative && pState && !pState.cell.isEdge()) {
        const alpha = toRadians((_a2 = pState.style.rotation) != null ? _a2 : 0);
        if (alpha !== 0) {
          const cos = Math.cos(alpha);
          const sin = Math.sin(alpha);
          const ct = new Point_default(state.getCenterX(), state.getCenterY());
          const cx = new Point_default(pState.getCenterX(), pState.getCenterY());
          const pt = getRotatedPoint(ct, cos, sin, cx);
          state.x = pt.x - state.width / 2;
          state.y = pt.y - state.height / 2;
        }
      }
      this.updateVertexLabelOffset(state);
    }
    /**
     * Validates the given cell state.
     */
    updateEdgeState(state, geo) {
      const source = state.getVisibleTerminalState(true);
      const target = state.getVisibleTerminalState(false);
      if (state.cell.getTerminal(true) && !source || !source && !geo.getTerminalPoint(true) || state.cell.getTerminal(false) && !target || !target && !geo.getTerminalPoint(false)) {
        this.clear(state.cell, true);
      } else {
        this.updateFixedTerminalPoints(state, source, target);
        this.updatePoints(state, geo.points, source, target);
        this.updateFloatingTerminalPoints(state, source, target);
        const pts = state.absolutePoints;
        if (state.cell !== this.currentRoot && (pts == null || pts.length < 2 || pts[0] == null || pts[pts.length - 1] == null)) {
          this.clear(state.cell, true);
        } else {
          this.updateEdgeBounds(state);
          this.updateEdgeLabelOffset(state);
        }
      }
    }
    /**
     * Updates the absoluteOffset of the given vertex cell state. This takes
     * into account the label position styles.
     *
     * @param state {@link CellState} whose absolute offset should be updated.
     */
    updateVertexLabelOffset(state) {
      var _a2, _b, _c, _d, _e;
      const h = (_a2 = state.style.labelPosition) != null ? _a2 : "center";
      switch (h) {
        case "left": {
          let lw = (_b = state.style.labelWidth) != null ? _b : null;
          if (lw != null) {
            lw *= this.scale;
          } else {
            lw = state.width;
          }
          state.absoluteOffset.x -= lw;
          break;
        }
        case "right": {
          state.absoluteOffset.x += state.width;
          break;
        }
        case "center": {
          const lw = (_c = state.style.labelWidth) != null ? _c : null;
          if (lw != null) {
            const align = (_d = state.style.align) != null ? _d : "center";
            let dx = 0;
            if (align === "center") {
              dx = 0.5;
            } else if (align === "right") {
              dx = 1;
            }
            if (dx !== 0) {
              state.absoluteOffset.x -= (lw * this.scale - state.width) * dx;
            }
          }
          break;
        }
      }
      const v = (_e = state.style.verticalLabelPosition) != null ? _e : "middle";
      if (v === "top") {
        state.absoluteOffset.y -= state.height;
      } else if (v === "bottom") {
        state.absoluteOffset.y += state.height;
      }
    }
    /**
     * Resets the current validation state.
     */
    resetValidationState() {
      this.lastNode = null;
      this.lastHtmlNode = null;
      this.lastForegroundNode = null;
      this.lastForegroundHtmlNode = null;
    }
    /**
     * Invoked when a state has been processed in {@link validatePoints}. This is used
     * to update the order of the DOM nodes of the shape.
     *
     * @param state {@link CellState} that represents the cell state.
     */
    stateValidated(state) {
      const graph = this.graph;
      const fg = state.cell.isEdge() && graph.keepEdgesInForeground || state.cell.isVertex() && graph.keepEdgesInBackground;
      const htmlNode = fg ? this.lastForegroundHtmlNode || this.lastHtmlNode : this.lastHtmlNode;
      const node = fg ? this.lastForegroundNode || this.lastNode : this.lastNode;
      const result = graph.cellRenderer.insertStateAfter(state, node, htmlNode);
      if (fg) {
        this.lastForegroundHtmlNode = result[1];
        this.lastForegroundNode = result[0];
      } else {
        this.lastHtmlNode = result[1];
        this.lastNode = result[0];
      }
    }
    /**
     * Sets the initial absolute terminal points in the given state before the edge
     * style is computed.
     *
     * @param edge {@link CellState} whose initial terminal points should be updated.
     * @param source {@link CellState} which represents the source terminal.
     * @param target {@link CellState} which represents the target terminal.
     */
    updateFixedTerminalPoints(edge, source, target) {
      this.updateFixedTerminalPoint(edge, source, true, this.graph.getConnectionConstraint(edge, source, true));
      this.updateFixedTerminalPoint(edge, target, false, this.graph.getConnectionConstraint(edge, target, false));
    }
    /**
     * Sets the fixed source or target terminal point on the given edge.
     *
     * @param edge {@link CellState} whose terminal point should be updated.
     * @param terminal {@link CellState} which represents the actual terminal.
     * @param source Boolean that specifies if the terminal is the source.
     * @param constraint {@link ConnectionConstraint} that specifies the connection.
     */
    updateFixedTerminalPoint(edge, terminal, source, constraint) {
      edge.setAbsoluteTerminalPoint(this.getFixedTerminalPoint(edge, terminal, source, constraint), source);
    }
    /**
     * Returns the fixed source or target terminal point for the given edge.
     *
     * @param edge {@link CellState} whose terminal point should be returned.
     * @param terminal {@link CellState} which represents the actual terminal.
     * @param source Boolean that specifies if the terminal is the source.
     * @param constraint {@link ConnectionConstraint} that specifies the connection.
     */
    getFixedTerminalPoint(edge, terminal, source, constraint) {
      let pt = null;
      if (constraint && terminal) {
        pt = this.graph.getConnectionPoint(terminal, constraint, false);
      }
      if (!pt && !terminal) {
        const s = this.scale;
        const tr = this.translate;
        const orig = edge.origin;
        const geo = edge.cell.getGeometry();
        pt = geo.getTerminalPoint(source);
        if (pt) {
          pt = new Point_default(s * (tr.x + pt.x + orig.x), s * (tr.y + pt.y + orig.y));
        }
      }
      return pt;
    }
    /**
     * Updates the bounds of the given cell state to reflect the bounds of the stencil
     * if it has a fixed aspect and returns the previous bounds as an {@link Rectangle} if
     * the bounds have been modified or null otherwise.
     *
     * @param state {@link CellState} whose bounds should be updated.
     */
    updateBoundsFromStencil(state) {
      let previous = null;
      if (state && state.shape && state.shape.stencil && state.shape.stencil.aspect === "fixed") {
        previous = Rectangle_default.fromRectangle(state);
        const asp = state.shape.stencil.computeAspect(
          null,
          // this argument is not used
          state.x,
          state.y,
          state.width,
          state.height
        );
        state.setRect(asp.x, asp.y, state.shape.stencil.w0 * asp.width, state.shape.stencil.h0 * asp.height);
      }
      return previous;
    }
    /**
     * Updates the absolute points in the given state using the specified array
     * of {@link Point} as the relative points.
     *
     * @param edge {@link CellState} whose absolute points should be updated.
     * @param points Array of {@link Point} that constitute the relative points.
     * @param source {@link CellState} that represents the source terminal.
     * @param target {@link CellState} that represents the target terminal.
     */
    updatePoints(edge, points, source, target) {
      const pts = [];
      pts.push(edge.absolutePoints[0]);
      const edgeStyle = this.getEdgeStyle(edge, points, source, target);
      if (edgeStyle && source) {
        const src = this.getTerminalPort(edge, source, true);
        const trg = target ? this.getTerminalPort(edge, target, false) : null;
        const srcBounds = this.updateBoundsFromStencil(src);
        const trgBounds = this.updateBoundsFromStencil(trg);
        edgeStyle(edge, src, trg, points, pts);
        if (src && srcBounds) {
          src.setRect(srcBounds.x, srcBounds.y, srcBounds.width, srcBounds.height);
        }
        if (trg && trgBounds) {
          trg.setRect(trgBounds.x, trgBounds.y, trgBounds.width, trgBounds.height);
        }
      } else if (points) {
        for (let i = 0; i < points.length; i += 1) {
          if (points[i]) {
            const pt = clone(points[i]);
            pts.push(this.transformControlPoint(edge, pt));
          }
        }
      }
      const tmp = edge.absolutePoints;
      pts.push(tmp[tmp.length - 1]);
      edge.absolutePoints = pts;
    }
    /**
     * Transforms the given control point to an absolute point.
     */
    transformControlPoint(state, pt, ignoreScale = false) {
      if (state && pt) {
        const orig = state.origin;
        const scale = ignoreScale ? 1 : this.scale;
        return new Point_default(scale * (pt.x + this.translate.x + orig.x), scale * (pt.y + this.translate.y + orig.y));
      }
      return null;
    }
    /**
     * Returns `true` if the given edge should be routed with {@link AbstractGraph.defaultLoopStyle}
     * or the {@link CellStateStyle.orthogonalLoop} defined for the given edge.
     * This implementation returns `true` if the given edge is a loop and does not
     */
    isLoopStyleEnabled(edge, points = [], source = null, target = null) {
      var _a2;
      const sc = this.graph.getConnectionConstraint(edge, source, true);
      const tc = this.graph.getConnectionConstraint(edge, target, false);
      if ((points == null || points.length < 2) && !(((_a2 = edge.style.orthogonalLoop) != null ? _a2 : false) || (sc == null || sc.point == null) && (tc == null || tc.point == null))) {
        return source != null && source === target;
      }
      return false;
    }
    /**
     * Returns the edge style function to be used to render the given edge state.
     */
    getEdgeStyle(edge, points = [], source = null, target = null) {
      var _a2, _b;
      let edgeStyle = this.isLoopStyleEnabled(edge, points, source, target) ? (_a2 = edge.style.loopStyle) != null ? _a2 : this.graph.defaultLoopStyle : !((_b = edge.style.noEdgeStyle) != null ? _b : false) ? edge.style.edgeStyle : null;
      if (typeof edgeStyle === "string") {
        let tmp = EdgeStyleRegistry.get(edgeStyle);
        if (!tmp && this.isAllowEval()) {
          tmp = doEval(edgeStyle);
        }
        edgeStyle = tmp;
      }
      if (typeof edgeStyle === "function") {
        return edgeStyle;
      }
      return null;
    }
    /**
     * Updates the terminal points in the given state after the edge style was
     * computed for the edge.
     *
     * @param state {@link CellState} whose terminal points should be updated.
     * @param source {@link CellState} that represents the source terminal.
     * @param target {@link CellState} that represents the target terminal.
     */
    updateFloatingTerminalPoints(state, source, target) {
      const pts = state.absolutePoints;
      const p0 = pts[0];
      const pe = pts[pts.length - 1];
      if (!pe && target) {
        this.updateFloatingTerminalPoint(state, target, source, false);
      }
      if (!p0 && source) {
        this.updateFloatingTerminalPoint(state, source, target, true);
      }
    }
    /**
     * Updates the absolute terminal point in the given state for the given
     * start and end state, where start is the source if source is true.
     *
     * @param edge {@link CellState} whose terminal point should be updated.
     * @param start {@link CellState} for the terminal on "this" side of the edge.
     * @param end {@link CellState} for the terminal on the other side of the edge.
     * @param source Boolean indicating if start is the source terminal state.
     */
    updateFloatingTerminalPoint(edge, start, end, source) {
      edge.setAbsoluteTerminalPoint(this.getFloatingTerminalPoint(edge, start, end, source), source);
    }
    /**
     * Returns the floating terminal point for the given edge, start and end
     * state, where start is the source if source is true.
     *
     * @param edge {@link CellState} whose terminal point should be returned.
     * @param start {@link CellState} for the terminal on "this" side of the edge.
     * @param end {@link CellState} for the terminal on the other side of the edge.
     * @param source Boolean indicating if start is the source terminal state.
     */
    getFloatingTerminalPoint(edge, start, end, source) {
      var _a2, _b, _c;
      start = this.getTerminalPort(edge, start, source);
      let next = this.getNextPoint(edge, end, source);
      const orth = this.graph.isOrthogonal(edge);
      const alpha = toRadians((_a2 = start.style.rotation) != null ? _a2 : 0);
      const center = new Point_default(start.getCenterX(), start.getCenterY());
      if (alpha !== 0) {
        const cos = Math.cos(-alpha);
        const sin = Math.sin(-alpha);
        next = getRotatedPoint(next, cos, sin, center);
      }
      let border = (_b = edge.style.perimeterSpacing) != null ? _b : 0;
      border += (_c = edge.style[source ? "sourcePerimeterSpacing" : "targetPerimeterSpacing"]) != null ? _c : 0;
      let pt = this.getPerimeterPoint(start, next, alpha === 0 && orth, border);
      if (pt && alpha !== 0) {
        const cos = Math.cos(alpha);
        const sin = Math.sin(alpha);
        pt = getRotatedPoint(pt, cos, sin, center);
      }
      return pt;
    }
    /**
     * Returns an {@link CellState} that represents the source or target terminal or
     * port for the given edge.
     *
     * @param state {@link CellState} that represents the state of the edge.
     * @param terminal {@link CellState} that represents the terminal.
     * @param source Boolean indicating if the given terminal is the source terminal.
     */
    getTerminalPort(state, terminal, source = false) {
      const key = source ? "sourcePort" : "targetPort";
      const id = state.style[key];
      if (id) {
        const cell = this.graph.getDataModel().getCell(id);
        if (cell) {
          const tmp = this.getState(cell, false);
          if (tmp) {
            terminal = tmp;
          }
        }
      }
      return terminal;
    }
    /**
     * Returns an {@link Point} that defines the location of the intersection point between
     * the perimeter and the line between the center of the shape and the given point.
     *
     * @param terminal {@link CellState} for the source or target terminal.
     * @param next {@link Point} that lies outside the given terminal.
     * @param orthogonal Boolean that specifies if the orthogonal projection onto
     * the perimeter should be returned. If this is false then the intersection
     * of the perimeter and the line between the next and the center point is
     * returned.
     * @param border Optional border between the perimeter and the shape.
     */
    getPerimeterPoint(terminal, next, orthogonal, border = 0) {
      let point = null;
      if (terminal != null) {
        const perimeter = this.getPerimeterFunction(terminal);
        if (perimeter != null && next != null) {
          const bounds = this.getPerimeterBounds(terminal, border);
          if (bounds.width > 0 || bounds.height > 0) {
            point = new Point_default(next.x, next.y);
            let flipH = false;
            let flipV = false;
            if (terminal.cell.isVertex()) {
              flipH = !!terminal.style.flipH;
              flipV = !!terminal.style.flipV;
              if (flipH) {
                point.x = 2 * bounds.getCenterX() - point.x;
              }
              if (flipV) {
                point.y = 2 * bounds.getCenterY() - point.y;
              }
            }
            point = perimeter(bounds, terminal, point, orthogonal);
            if (point != null) {
              if (flipH) {
                point.x = 2 * bounds.getCenterX() - point.x;
              }
              if (flipV) {
                point.y = 2 * bounds.getCenterY() - point.y;
              }
            }
          }
        }
        if (point == null) {
          point = this.getPoint(terminal);
        }
      }
      return point;
    }
    /**
     * Returns the x-coordinate of the center point for automatic routing.
     */
    getRoutingCenterX(state) {
      var _a2;
      const f = state.style ? (_a2 = state.style.routingCenterX) != null ? _a2 : 0 : 0;
      return state.getCenterX() + f * state.width;
    }
    /**
     * Returns the y-coordinate of the center point for automatic routing.
     */
    getRoutingCenterY(state) {
      var _a2;
      const f = state.style ? (_a2 = state.style.routingCenterY) != null ? _a2 : 0 : 0;
      return state.getCenterY() + f * state.height;
    }
    /**
     * Returns the perimeter bounds for the given terminal, edge pair as an
     * {@link Rectangle}.
     *
     * If you have a model where each terminal has a relative child that should
     * act as the graphical endpoint for a connection from/to the terminal, then
     * this method can be replaced as follows:
     *
     * @example
     * ```javascript
     * var oldGetPerimeterBounds = getPerimeterBounds;
     * getPerimeterBounds(terminal, edge, isSource)
     * {
     *   var model = this.graph.getDataModel();
     *   var childCount = model.getChildCount(terminal.cell);
     *
     *   if (childCount > 0)
     *   {
     *     var child = model.getChildAt(terminal.cell, 0);
     *     var geo = model.getGeometry(child);
     *
     *     if (geo != null &&
     *         geo.relative)
     *     {
     *       var state = this.getState(child);
     *
     *       if (state != null)
     *       {
     *         terminal = state;
     *       }
     *     }
     *   }
     *
     *   return oldGetPerimeterBounds.apply(this, arguments);
     * };
     * ```
     *
     * @param terminal CellState that represents the terminal.
     * @param border Number that adds a border between the shape and the perimeter.
     */
    getPerimeterBounds(terminal, border = 0) {
      var _a2;
      border += (_a2 = terminal.style.perimeterSpacing) != null ? _a2 : 0;
      return terminal.getPerimeterBounds(border * this.scale);
    }
    /**
     * Returns the perimeter function for the given state.
     */
    getPerimeterFunction(state) {
      let perimeter = state.style.perimeter;
      if (typeof perimeter === "string") {
        let tmp = PerimeterRegistry.get(perimeter);
        if (tmp == null && this.isAllowEval()) {
          tmp = doEval(perimeter);
        }
        perimeter = tmp;
      }
      if (typeof perimeter === "function") {
        return perimeter;
      }
      return null;
    }
    /**
     * Returns the nearest point in the list of absolute points or the center
     * of the opposite terminal.
     *
     * @param edge {@link CellState} that represents the edge.
     * @param opposite {@link CellState} that represents the opposite terminal.
     * @param source Boolean indicating if the next point for the source or target
     * should be returned.
     */
    getNextPoint(edge, opposite, source = false) {
      const pts = edge.absolutePoints;
      let point = null;
      if (pts.length >= 2) {
        const count = pts.length;
        point = pts[source ? Math.min(1, count - 1) : Math.max(0, count - 2)];
      }
      if (!point && opposite) {
        point = new Point_default(opposite.getCenterX(), opposite.getCenterY());
      }
      return point;
    }
    /**
     * Returns the nearest ancestor terminal that is visible. The edge appears
     * to be connected to this terminal on the display. The result of this method
     * is cached in {@link CellState.getVisibleTerminalState}.
     *
     * @param edge {@link Cell} whose visible terminal should be returned.
     * @param source Boolean that specifies if the source or target terminal
     * should be returned.
     */
    getVisibleTerminal(edge, source) {
      const model = this.graph.getDataModel();
      let result = edge.getTerminal(source);
      let best = result;
      while (result && result !== this.currentRoot) {
        if (best && !best.isVisible() || result.isCollapsed()) {
          best = result;
        }
        result = result.getParent();
      }
      if (best && (!model.contains(best) || best.getParent() === model.getRoot() || best === this.currentRoot)) {
        best = null;
      }
      return best;
    }
    /**
     * Updates the given state using the bounding box of the absolute points.
     * Also updates {@link CellState.terminalDistance}, {@link CellState.length} and
     * {@link CellState.segments}.
     *
     * @param state {@link CellState} whose bounds should be updated.
     */
    updateEdgeBounds(state) {
      const points = state.absolutePoints;
      const p0 = points[0];
      const pe = points[points.length - 1];
      if (p0 && pe && (p0.x !== pe.x || p0.y !== pe.y)) {
        const dx = pe.x - p0.x;
        const dy = pe.y - p0.y;
        state.terminalDistance = Math.sqrt(dx * dx + dy * dy);
      } else {
        state.terminalDistance = 0;
      }
      let length = 0;
      const segments = [];
      let pt = p0;
      if (pt) {
        let minX = pt.x;
        let minY = pt.y;
        let maxX = minX;
        let maxY = minY;
        for (let i = 1; i < points.length; i += 1) {
          const tmp = points[i];
          if (tmp) {
            const dx = pt.x - tmp.x;
            const dy = pt.y - tmp.y;
            const segment = Math.sqrt(dx * dx + dy * dy);
            segments.push(segment);
            length += segment;
            pt = tmp;
            minX = Math.min(pt.x, minX);
            minY = Math.min(pt.y, minY);
            maxX = Math.max(pt.x, maxX);
            maxY = Math.max(pt.y, maxY);
          }
        }
        state.length = length;
        state.segments = segments;
        const markerSize = 1;
        state.x = minX;
        state.y = minY;
        state.width = Math.max(markerSize, maxX - minX);
        state.height = Math.max(markerSize, maxY - minY);
      }
    }
    /**
     * Returns the absolute point on the edge for the given relative
     * {@link Geometry} as an {@link Point}. The edge is represented by the given
     * {@link CellState}.
     *
     * @param state {@link CellState} that represents the state of the parent edge.
     * @param geometry {@link Geometry} that represents the relative location.
     */
    getPoint(state, geometry = null) {
      let x = state.getCenterX();
      let y = state.getCenterY();
      if (state.segments != null && (geometry == null || geometry.relative)) {
        const gx = geometry != null ? geometry.x / 2 : 0;
        const pointCount = state.absolutePoints.length;
        const dist = Math.round((gx + 0.5) * state.length);
        let segment = state.segments[0];
        let length = 0;
        let index = 1;
        while (dist >= Math.round(length + segment) && index < pointCount - 1) {
          length += segment;
          segment = state.segments[index++];
        }
        const factor = segment === 0 ? 0 : (dist - length) / segment;
        const p0 = state.absolutePoints[index - 1];
        const pe = state.absolutePoints[index];
        if (p0 != null && pe != null) {
          let gy = 0;
          let offsetX = 0;
          let offsetY = 0;
          if (geometry != null) {
            gy = geometry.y;
            const { offset } = geometry;
            if (offset != null) {
              offsetX = offset.x;
              offsetY = offset.y;
            }
          }
          const dx = pe.x - p0.x;
          const dy = pe.y - p0.y;
          const nx = segment === 0 ? 0 : dy / segment;
          const ny = segment === 0 ? 0 : dx / segment;
          x = p0.x + dx * factor + (nx * gy + offsetX) * this.scale;
          y = p0.y + dy * factor - (ny * gy - offsetY) * this.scale;
        }
      } else if (geometry != null) {
        const { offset } = geometry;
        if (offset != null) {
          x += offset.x;
          y += offset.y;
        }
      }
      return new Point_default(x, y);
    }
    /**
     * Gets the relative point that describes the given, absolute label
     * position for the given edge state.
     *
     * @param edgeState {@link CellState} that represents the state of the parent edge.
     * @param x Specifies the x-coordinate of the absolute label location.
     * @param y Specifies the y-coordinate of the absolute label location.
     */
    getRelativePoint(edgeState, x, y) {
      const geometry = edgeState.cell.getGeometry();
      if (geometry) {
        const absolutePoints = edgeState.absolutePoints;
        const pointCount = absolutePoints.length;
        if (geometry.relative && pointCount > 1) {
          const totalLength = edgeState.length;
          const { segments } = edgeState;
          let p0 = absolutePoints[0];
          let pe = absolutePoints[1];
          let minDist = ptSegDistSq(p0.x, p0.y, pe.x, pe.y, x, y);
          let length = 0;
          let index = 0;
          let tmp = 0;
          for (let i = 2; i < pointCount; i += 1) {
            p0 = pe;
            pe = absolutePoints[i];
            const dist = ptSegDistSq(p0.x, p0.y, pe.x, pe.y, x, y);
            tmp += segments[i - 2];
            if (dist <= minDist) {
              minDist = dist;
              index = i - 1;
              length = tmp;
            }
          }
          const seg = segments[index];
          p0 = absolutePoints[index];
          pe = absolutePoints[index + 1];
          const x2 = p0.x;
          const y2 = p0.y;
          const x1 = pe.x;
          const y1 = pe.y;
          let px = x;
          let py = y;
          const xSegment = x2 - x1;
          const ySegment = y2 - y1;
          px -= x1;
          py -= y1;
          let projlenSq = 0;
          px = xSegment - px;
          py = ySegment - py;
          const dotprod = px * xSegment + py * ySegment;
          if (dotprod <= 0) {
            projlenSq = 0;
          } else {
            projlenSq = dotprod * dotprod / (xSegment * xSegment + ySegment * ySegment);
          }
          let projlen = Math.sqrt(projlenSq);
          if (projlen > seg) {
            projlen = seg;
          }
          let yDistance = Math.sqrt(ptSegDistSq(p0.x, p0.y, pe.x, pe.y, x, y));
          const direction = relativeCcw(p0.x, p0.y, pe.x, pe.y, x, y);
          if (direction === -1) {
            yDistance = -yDistance;
          }
          return new Point_default((totalLength / 2 - length - projlen) / totalLength * -2, yDistance / this.scale);
        }
      }
      return new Point_default();
    }
    /**
     * Updates {@link CellState.absoluteOffset} for the given state. The absolute
     * offset is normally used for the position of the edge label. It is
     * calculated from the geometry as an absolute offset from the center
     * between the two endpoints if the geometry is absolute, or as the
     * relative distance between the center along the line and the absolute
     * orthogonal distance if the geometry is relative.
     *
     * @param state {@link CellState} whose absolute offset should be updated.
     */
    updateEdgeLabelOffset(state) {
      const points = state.absolutePoints;
      const absoluteOffset = state.absoluteOffset;
      absoluteOffset.x = state.getCenterX();
      absoluteOffset.y = state.getCenterY();
      if (points.length > 0 && state.segments) {
        const geometry = state.cell.getGeometry();
        if (geometry) {
          if (geometry.relative) {
            const offset = this.getPoint(state, geometry);
            state.absoluteOffset = offset;
          } else {
            const p0 = points[0];
            const pe = points[points.length - 1];
            if (p0 && pe) {
              const dx = pe.x - p0.x;
              const dy = pe.y - p0.y;
              let x0 = 0;
              let y0 = 0;
              const off = geometry.offset;
              if (off) {
                x0 = off.x;
                y0 = off.y;
              }
              const x = p0.x + dx / 2 + x0 * this.scale;
              const y = p0.y + dy / 2 + y0 * this.scale;
              absoluteOffset.x = x;
              absoluteOffset.y = y;
            }
          }
        }
      }
    }
    /**
     * Returns the {@link CellState} for the given cell. If create is true, then
     * the state is created if it does not yet exist.
     *
     * @param cell {@link Cell} for which the {@link CellState} should be returned.
     * @param create Optional boolean indicating if a new state should be created if it does not yet exist. Default is false.
     */
    getState(cell, create2 = false) {
      var _a2;
      let state = (_a2 = this.states.get(cell)) != null ? _a2 : null;
      if (create2 && (!state || this.updateStyle) && cell.isVisible()) {
        if (!state) {
          state = this.createState(cell);
          this.states.set(cell, state);
        } else {
          state.style = this.graph.getCellStyle(cell);
        }
      }
      return state;
    }
    /**
     * Returns the {@link CellState}s for the given array of {@link Cell}.
     *
     * The array contains all states that are not null, that is, the returned array may have fewer elements than the given array.
     *
     * If no argument is given, then this returns the {@link CellState} of {@link states}.
     */
    getCellStates(cells = null) {
      if (!cells) {
        return Array.from(this.states.values());
      }
      const result = [];
      for (const cell of cells) {
        const state = this.getState(cell);
        if (state) {
          result.push(state);
        }
      }
      return result;
    }
    /**
     * Removes and returns the {@link CellState} for the given cell.
     *
     * @param cell {@link Cell} for which the {@link CellState} should be removed.
     */
    removeState(cell) {
      var _a2;
      const state = (_a2 = this.states.get(cell)) != null ? _a2 : null;
      this.states.delete(cell);
      if (state) {
        this.graph.cellRenderer.destroy(state);
        state.invalid = true;
        state.destroy();
      }
      return state;
    }
    /**
     * Creates and returns an {@link CellState} for the given cell and initializes
     * it using {@link cellRenderer.initialize}.
     *
     * @param cell {@link Cell} for which a new {@link CellState} should be created.
     */
    createState(cell) {
      return new CellState_default(this, cell, this.graph.getCellStyle(cell));
    }
    /**
     * Returns true if the event origin is one of the drawing panes or
     * containers of the view.
     */
    isContainerEvent(evt) {
      const source = getSource(evt);
      return source && (source === this.graph.container || // @ts-ignore parentNode may exist
      source.parentNode === this.backgroundPane || // @ts-ignore parentNode may exist
      source.parentNode && source.parentNode.parentNode === this.backgroundPane || source === this.canvas.parentNode || source === this.canvas || source === this.backgroundPane || source === this.drawPane || source === this.overlayPane || source === this.decoratorPane);
    }
    isScrollEvent(evt) {
      const graph = this.graph;
      const offset = getOffset(graph.container);
      const eventClientPosition = evt instanceof MouseEvent ? [evt.clientX, evt.clientY] : [evt.touches[0].clientX, evt.touches[0].clientY];
      const pt = new Point_default(eventClientPosition[0] - offset.x, eventClientPosition[1] - offset.y);
      const container = graph.container;
      const outWidth = container.offsetWidth;
      const inWidth = container.clientWidth;
      if (outWidth > inWidth && pt.x > inWidth + 2 && pt.x <= outWidth) {
        return true;
      }
      const outHeight = container.offsetHeight;
      const inHeight = container.clientHeight;
      return outHeight > inHeight && pt.y > inHeight + 2 && pt.y <= outHeight;
    }
    /**
     * Initializes the graph event dispatch loop for the specified container
     * and invokes {@link create} to create the required DOM nodes for the display.
     */
    init() {
      this.installListeners();
      this.createSvg();
    }
    /**
     * Installs the required listeners in the container.
     */
    installListeners() {
      const graph = this.graph;
      const { container } = graph;
      if (Client_default.IS_TOUCH) {
        InternalEvent_default.addListener(container, "gesturestart", ((evt) => {
          graph.fireGestureEvent(evt);
          InternalEvent_default.consume(evt);
        }));
        InternalEvent_default.addListener(container, "gesturechange", ((evt) => {
          graph.fireGestureEvent(evt);
          InternalEvent_default.consume(evt);
        }));
        InternalEvent_default.addListener(container, "gestureend", ((evt) => {
          graph.fireGestureEvent(evt);
          InternalEvent_default.consume(evt);
        }));
      }
      let pointerId = null;
      InternalEvent_default.addGestureListeners(container, ((evt) => {
        if (this.isContainerEvent(evt) && (!Client_default.IS_GC && !Client_default.IS_SF || !this.isScrollEvent(evt))) {
          graph.fireMouseEvent(InternalEvent_default.MOUSE_DOWN, new InternalMouseEvent_default(evt));
          pointerId = evt.pointerId;
        }
      }), (evt) => {
        if (this.isContainerEvent(evt) && // @ts-ignore
        (pointerId === null || evt.pointerId === pointerId)) {
          graph.fireMouseEvent(InternalEvent_default.MOUSE_MOVE, new InternalMouseEvent_default(evt));
        }
      }, (evt) => {
        if (this.isContainerEvent(evt)) {
          graph.fireMouseEvent(InternalEvent_default.MOUSE_UP, new InternalMouseEvent_default(evt));
        }
        pointerId = null;
      });
      InternalEvent_default.addListener(container, "dblclick", ((evt) => {
        if (this.isContainerEvent(evt)) {
          graph.dblClick(evt);
        }
      }));
      const getState = (evt) => {
        let state = null;
        if (Client_default.IS_TOUCH) {
          const x = getClientX(evt);
          const y = getClientY(evt);
          const pt = convertPoint(container, x, y);
          const cell = graph.getCellAt(pt.x, pt.y);
          if (cell)
            state = graph.view.getState(cell);
        }
        return state;
      };
      graph.addMouseListener({
        mouseDown: () => {
          const popupMenuHandler = graph.getPlugin("PopupMenuHandler");
          popupMenuHandler == null ? void 0 : popupMenuHandler.hideMenu();
        },
        mouseMove: () => {
        },
        mouseUp: () => {
        }
      });
      this.moveHandler = (evt) => {
        const tooltipHandler = graph.getPlugin("TooltipHandler");
        if (tooltipHandler && tooltipHandler.isHideOnHover()) {
          tooltipHandler.hide();
        }
        if (this.captureDocumentGesture && graph.isMouseDown && graph.container != null && !this.isContainerEvent(evt) && graph.container.style.display !== "none" && graph.container.style.visibility !== "hidden" && !isConsumed(evt)) {
          graph.fireMouseEvent(InternalEvent_default.MOUSE_MOVE, new InternalMouseEvent_default(evt, getState(evt)));
        }
      };
      this.endHandler = (evt) => {
        if (this.captureDocumentGesture && graph.isMouseDown && graph.container != null && !this.isContainerEvent(evt) && graph.container.style.display !== "none" && graph.container.style.visibility !== "hidden") {
          graph.fireMouseEvent(InternalEvent_default.MOUSE_UP, new InternalMouseEvent_default(evt));
        }
      };
      InternalEvent_default.addGestureListeners(document, null, this.moveHandler, this.endHandler);
    }
    /**
     * Creates and returns the DOM nodes for the SVG display.
     */
    createSvg() {
      const { container } = this.graph;
      const canvas = this.canvas = document.createElementNS(NS_SVG, "g");
      this.backgroundPane = document.createElementNS(NS_SVG, "g");
      canvas.appendChild(this.backgroundPane);
      this.drawPane = document.createElementNS(NS_SVG, "g");
      canvas.appendChild(this.drawPane);
      this.overlayPane = document.createElementNS(NS_SVG, "g");
      canvas.appendChild(this.overlayPane);
      this.decoratorPane = document.createElementNS(NS_SVG, "g");
      canvas.appendChild(this.decoratorPane);
      const root = document.createElementNS(NS_SVG, "svg");
      root.style.left = "0px";
      root.style.top = "0px";
      root.style.width = "100%";
      root.style.height = "100%";
      root.style.display = "block";
      root.appendChild(this.canvas);
      if (container != null) {
        container.appendChild(root);
        this.updateContainerStyle(container);
      }
    }
    /**
     * Creates the DOM nodes for the HTML display.
     */
    createHtml() {
      const container = this.graph.container;
      if (container != null) {
        this.canvas = this.createHtmlPane("100%", "100%");
        this.canvas.style.overflow = "hidden";
        this.backgroundPane = this.createHtmlPane("1px", "1px");
        this.drawPane = this.createHtmlPane("1px", "1px");
        this.overlayPane = this.createHtmlPane("1px", "1px");
        this.decoratorPane = this.createHtmlPane("1px", "1px");
        this.canvas.appendChild(this.backgroundPane);
        this.canvas.appendChild(this.drawPane);
        this.canvas.appendChild(this.overlayPane);
        this.canvas.appendChild(this.decoratorPane);
        container.appendChild(this.canvas);
        this.updateContainerStyle(container);
      }
    }
    /**
     * Updates the size of the HTML canvas.
     */
    updateHtmlCanvasSize(width, height) {
      if (this.graph.container != null) {
        const ow = this.graph.container.offsetWidth;
        const oh = this.graph.container.offsetHeight;
        if (ow < width) {
          this.canvas.style.width = width + "px";
        } else {
          this.canvas.style.width = "100%";
        }
        if (oh < height) {
          this.canvas.style.height = height + "px";
        } else {
          this.canvas.style.height = "100%";
        }
      }
    }
    /**
     * Creates and returns a drawing pane in HTML (DIV).
     */
    createHtmlPane(width, height) {
      const pane = document.createElement("DIV");
      if (width != null && height != null) {
        pane.style.position = "absolute";
        pane.style.left = "0px";
        pane.style.top = "0px";
        pane.style.width = width;
        pane.style.height = height;
      } else {
        pane.style.position = "relative";
      }
      return pane;
    }
    /**
     * Updates the style of the container after installing the SVG DOM elements.
     */
    updateContainerStyle(container) {
      const style = getCurrentStyle(container);
      if (style != null && style.position == "static") {
        container.style.position = "relative";
      }
      if (Client_default.IS_POINTER) {
        container.style.touchAction = "none";
      }
    }
    /**
     * Destroys the view and all its resources.
     */
    destroy() {
      let root = null;
      if (this.canvas && this.canvas instanceof SVGElement) {
        root = this.canvas.ownerSVGElement;
      }
      if (!root) {
        root = this.canvas;
      }
      if (root && root.parentNode) {
        this.clear(this.currentRoot, true);
        InternalEvent_default.removeGestureListeners(document, null, this.moveHandler, this.endHandler);
        InternalEvent_default.release(this.graph.container);
        root.parentNode.removeChild(root);
        this.moveHandler = null;
        this.endHandler = null;
        this.canvas = null;
        this.backgroundPane = null;
        this.drawPane = null;
        this.overlayPane = null;
        this.decoratorPane = null;
        super.destroy();
      }
    }
  };
  var GraphView_default = GraphView;

  // node_modules/@maxgraph/core/lib/esm/view/shape/node/RhombusShape.js
  var RhombusShape = class extends Shape_default {
    constructor(bounds, fill, stroke, strokewidth = 1) {
      super();
      this.bounds = bounds;
      this.fill = fill;
      this.stroke = stroke;
      this.strokeWidth = strokewidth;
    }
    /**
     * Adds roundable support.
     */
    // isRoundable(): boolean;
    isRoundable() {
      return true;
    }
    /**
     * Generic painting implementation.
     * @param {mxAbstractCanvas2D} c
     * @param {number} x
     * @param {number} y
     * @param {number} w
     * @param {number} h
     */
    paintVertexShape(c, x, y, w, h) {
      const hw = w / 2;
      const hh = h / 2;
      const arcSize = this.getBaseArcSize();
      c.begin();
      this.addPoints(c, [
        new Point_default(x + hw, y),
        new Point_default(x + w, y + hh),
        new Point_default(x + hw, y + h),
        new Point_default(x, y + hh)
      ], this.isRounded, arcSize, true);
      c.fillAndStroke();
    }
  };
  var RhombusShape_default = RhombusShape;

  // node_modules/@maxgraph/core/lib/esm/view/shape/node/CylinderShape.js
  var CylinderShape = class extends Shape_default {
    constructor(bounds, fill, stroke, strokeWidth = 1) {
      super();
      this.maxHeight = 40;
      this.svgStrokeTolerance = 0;
      this.bounds = bounds;
      this.fill = fill;
      this.stroke = stroke;
      this.strokeWidth = strokeWidth;
    }
    /**
     * Redirects to redrawPath for subclasses to work.
     */
    paintVertexShape(c, x, y, w, h) {
      var _a2;
      c.translate(x, y);
      c.begin();
      this.redrawPath(c, x, y, w, h, false);
      c.fillAndStroke();
      if (!this.outline || !this.style || !((_a2 = this.style.backgroundOutline) != null ? _a2 : false)) {
        c.setShadow(false);
        c.begin();
        this.redrawPath(c, x, y, w, h, true);
        c.stroke();
      }
    }
    /**
     * Redirects to redrawPath for subclasses to work.
     */
    getCylinderSize(x, y, w, h) {
      return Math.min(this.maxHeight, Math.round(h / 5));
    }
    /**
     * Draws the path for this shape.
     */
    redrawPath(c, x, y, w, h, isForeground = false) {
      const dy = this.getCylinderSize(x, y, w, h);
      if (isForeground && this.fill !== NONE || !isForeground && this.fill === NONE) {
        c.moveTo(0, dy);
        c.curveTo(0, 2 * dy, w, 2 * dy, w, dy);
        if (!isForeground) {
          c.stroke();
          c.begin();
        }
      }
      if (!isForeground) {
        c.moveTo(0, dy);
        c.curveTo(0, -dy / 3, w, -dy / 3, w, dy);
        c.lineTo(w, h - dy);
        c.curveTo(w, h + dy / 3, 0, h + dy / 3, 0, h - dy);
        c.close();
      }
    }
  };
  var CylinderShape_default = CylinderShape;

  // node_modules/@maxgraph/core/lib/esm/view/shape/node/AbstractPathShape.js
  var AbstractPathShape = class extends Shape_default {
    constructor(bounds = null, fill = NONE, stroke = NONE, strokeWidth = 1) {
      super();
      this.bounds = bounds;
      this.fill = fill;
      this.stroke = stroke;
      this.strokeWidth = strokeWidth;
    }
    /**
     * Redirects to redrawPath for subclasses to work.
     */
    paintVertexShape(c, x, y, w, h) {
      c.translate(x, y);
      c.begin();
      this.redrawPath(c, x, y, w, h);
      c.fillAndStroke();
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/shape/node/ActorShape.js
  var ActorShape = class extends AbstractPathShape {
    /**
     * Draws the path for this shape.
     */
    redrawPath(c, x, y, w, h) {
      const width = w / 3;
      c.moveTo(0, h);
      c.curveTo(0, 3 * h / 5, 0, 2 * h / 5, w / 2, 2 * h / 5);
      c.curveTo(w / 2 - width, 2 * h / 5, w / 2 - width, 0, w / 2, 0);
      c.curveTo(w / 2 + width, 0, w / 2 + width, 2 * h / 5, w / 2, 2 * h / 5);
      c.curveTo(w, 2 * h / 5, w, 3 * h / 5, w, h);
      c.close();
    }
  };
  var ActorShape_default = ActorShape;

  // node_modules/@maxgraph/core/lib/esm/view/shape/node/TriangleShape.js
  var TriangleShape = class extends AbstractPathShape {
    /**
     * Adds roundable support.
     * @returns {boolean}
     */
    isRoundable() {
      return true;
    }
    /**
     * Draws the path for this shape.
     */
    redrawPath(c, x, y, w, h) {
      const arcSize = this.getBaseArcSize();
      this.addPoints(c, [new Point_default(0, 0), new Point_default(w, 0.5 * h), new Point_default(0, h)], this.isRounded, arcSize, true);
    }
  };
  var TriangleShape_default = TriangleShape;

  // node_modules/@maxgraph/core/lib/esm/view/shape/node/HexagonShape.js
  var HexagonShape = class extends AbstractPathShape {
    /**
     * Draws the path for this shape.
     */
    redrawPath(c, x, y, w, h) {
      const arcSize = this.getBaseArcSize();
      this.addPoints(c, [
        new Point_default(0.25 * w, 0),
        new Point_default(0.75 * w, 0),
        new Point_default(w, 0.5 * h),
        new Point_default(0.75 * w, h),
        new Point_default(0.25 * w, h),
        new Point_default(0, 0.5 * h)
      ], this.isRounded, arcSize, true);
    }
  };
  var HexagonShape_default = HexagonShape;

  // node_modules/@maxgraph/core/lib/esm/view/shape/node/CloudShape.js
  var CloudShape = class extends AbstractPathShape {
    /**
     * Draws the path for this shape.
     */
    redrawPath(c, x, y, w, h) {
      c.moveTo(0.25 * w, 0.25 * h);
      c.curveTo(0.05 * w, 0.25 * h, 0, 0.5 * h, 0.16 * w, 0.55 * h);
      c.curveTo(0, 0.66 * h, 0.18 * w, 0.9 * h, 0.31 * w, 0.8 * h);
      c.curveTo(0.4 * w, h, 0.7 * w, h, 0.8 * w, 0.8 * h);
      c.curveTo(w, 0.8 * h, w, 0.6 * h, 0.875 * w, 0.5 * h);
      c.curveTo(w, 0.3 * h, 0.8 * w, 0.1 * h, 0.625 * w, 0.2 * h);
      c.curveTo(0.5 * w, 0.05 * h, 0.3 * w, 0.05 * h, 0.25 * w, 0.25 * h);
      c.close();
    }
  };
  var CloudShape_default = CloudShape;

  // node_modules/@maxgraph/core/lib/esm/view/shape/edge/LineShape.js
  var LineShape = class extends Shape_default {
    constructor(bounds, stroke, strokeWidth = 1, vertical = false) {
      super();
      this.bounds = bounds;
      this.stroke = stroke;
      this.strokeWidth = strokeWidth;
      this.vertical = vertical;
    }
    /**
     * Redirects to redrawPath for subclasses to work.
     * @param {AbstractCanvas2D} c
     * @param {number} x
     * @param {number} y
     * @param {number} w
     * @param {number} h
     */
    paintVertexShape(c, x, y, w, h) {
      c.begin();
      if (this.vertical) {
        const mid = x + w / 2;
        c.moveTo(mid, y);
        c.lineTo(mid, y + h);
      } else {
        const mid = y + h / 2;
        c.moveTo(x, mid);
        c.lineTo(x + w, mid);
      }
      c.stroke();
    }
  };
  var LineShape_default = LineShape;

  // node_modules/@maxgraph/core/lib/esm/view/shape/edge/ArrowShape.js
  var ArrowShape = class extends Shape_default {
    constructor(points, fill, stroke, strokeWidth = 1, arrowWidth = ARROW_WIDTH, spacing = ARROW_SPACING, endSize = ARROW_SIZE) {
      super();
      this.points = points;
      this.fill = fill;
      this.stroke = stroke;
      this.strokeWidth = strokeWidth;
      this.arrowWidth = arrowWidth;
      this.spacing = spacing;
      this.endSize = endSize;
    }
    /**
     * Augments the bounding box with the edge width and markers.
     */
    augmentBoundingBox(bbox) {
      super.augmentBoundingBox(bbox);
      const w = Math.max(this.arrowWidth, this.endSize);
      bbox.grow((w / 2 + this.strokeWidth) * this.scale);
    }
    /**
     * Paints the line shape.
     */
    paintEdgeShape(c, pts) {
      const spacing = ARROW_SPACING;
      const width = ARROW_WIDTH;
      const arrow = ARROW_SIZE;
      const p0 = pts[0];
      const pe = pts[pts.length - 1];
      const dx = pe.x - p0.x;
      const dy = pe.y - p0.y;
      const dist = Math.sqrt(dx * dx + dy * dy);
      const length = dist - 2 * spacing - arrow;
      const nx = dx / dist;
      const ny = dy / dist;
      const basex = length * nx;
      const basey = length * ny;
      const floorx = width * ny / 3;
      const floory = -width * nx / 3;
      const p0x = p0.x - floorx / 2 + spacing * nx;
      const p0y = p0.y - floory / 2 + spacing * ny;
      const p1x = p0x + floorx;
      const p1y = p0y + floory;
      const p2x = p1x + basex;
      const p2y = p1y + basey;
      const p3x = p2x + floorx;
      const p3y = p2y + floory;
      const p5x = p3x - 3 * floorx;
      const p5y = p3y - 3 * floory;
      c.begin();
      c.moveTo(p0x, p0y);
      c.lineTo(p1x, p1y);
      c.lineTo(p2x, p2y);
      c.lineTo(p3x, p3y);
      c.lineTo(pe.x - spacing * nx, pe.y - spacing * ny);
      c.lineTo(p5x, p5y);
      c.lineTo(p5x + floorx, p5y + floory);
      c.close();
      c.fillAndStroke();
    }
  };
  var ArrowShape_default = ArrowShape;

  // node_modules/@maxgraph/core/lib/esm/view/shape/edge/ArrowConnectorShape.js
  var ArrowConnectorShape = class extends Shape_default {
    constructor(points, fill, stroke, strokeWidth = 1, arrowWidth = ARROW_WIDTH, spacing = ARROW_SPACING, endSize = ARROW_SIZE / 5) {
      super();
      this.useSvgBoundingBox = true;
      this.points = points;
      this.fill = fill;
      this.stroke = stroke;
      this.strokeWidth = strokeWidth;
      this.arrowWidth = arrowWidth;
      this.arrowSpacing = spacing;
      this.startSize = ARROW_SIZE / 5;
      this.endSize = endSize;
    }
    /**
     * Hook for subclassers.
     */
    isRoundable() {
      return true;
    }
    /**
     * Overrides mxShape to reset spacing.
     */
    resetStyles() {
      super.resetStyles();
      this.arrowSpacing = ARROW_SPACING;
    }
    /**
     * Overrides apply to get smooth transition from default start- and endsize.
     */
    apply(state) {
      super.apply(state);
      if (this.style && this.style.startSize != null && this.style.endSize != null) {
        this.startSize = this.style.startSize * 3;
        this.endSize = this.style.endSize * 3;
      }
    }
    /**
     * Augments the bounding box with the edge width and markers.
     */
    augmentBoundingBox(bbox) {
      super.augmentBoundingBox(bbox);
      let w = this.getEdgeWidth();
      if (this.isMarkerStart()) {
        w = Math.max(w, this.getStartArrowWidth());
      }
      if (this.isMarkerEnd()) {
        w = Math.max(w, this.getEndArrowWidth());
      }
      bbox.grow((w / 2 + this.strokeWidth) * this.scale);
    }
    /**
     * Paints the line shape.
     */
    paintEdgeShape(c, pts) {
      var _a2, _b;
      let strokeWidth = this.strokeWidth;
      if (this.outline) {
        strokeWidth = Math.max(1, (_b = (_a2 = this.style) == null ? void 0 : _a2.strokeWidth) != null ? _b : 0);
      }
      const startWidth = this.getStartArrowWidth() + strokeWidth;
      const endWidth = this.getEndArrowWidth() + strokeWidth;
      const edgeWidth = this.outline ? this.getEdgeWidth() + strokeWidth : this.getEdgeWidth();
      const openEnded = this.isOpenEnded();
      const markerStart = this.isMarkerStart();
      const markerEnd = this.isMarkerEnd();
      const spacing = openEnded ? 0 : this.arrowSpacing + strokeWidth / 2;
      const startSize = this.startSize + strokeWidth;
      const endSize = this.endSize + strokeWidth;
      const isRounded = this.isArrowRounded();
      const pe = pts[pts.length - 1];
      let i0 = 1;
      while (i0 < pts.length - 1 && pts[i0].x === pts[0].x && pts[i0].y === pts[0].y) {
        i0++;
      }
      const dx = pts[i0].x - pts[0].x;
      const dy = pts[i0].y - pts[0].y;
      const dist = Math.sqrt(dx * dx + dy * dy);
      if (dist === 0) {
        return;
      }
      let nx = dx / dist;
      let nx2;
      let nx1 = nx;
      let ny = dy / dist;
      let ny2;
      let ny1 = ny;
      let orthx = edgeWidth * ny;
      let orthy = -edgeWidth * nx;
      const fns = [];
      if (isRounded) {
        c.setLineJoin("round");
      } else if (pts.length > 2) {
        c.setMiterLimit(1.42);
      }
      c.begin();
      const startNx = nx;
      const startNy = ny;
      if (markerStart && !openEnded) {
        this.paintMarker(c, pts[0].x, pts[0].y, nx, ny, startSize, startWidth, edgeWidth, spacing, true);
      } else {
        const outStartX = pts[0].x + orthx / 2 + spacing * nx;
        const outStartY = pts[0].y + orthy / 2 + spacing * ny;
        const inEndX = pts[0].x - orthx / 2 + spacing * nx;
        const inEndY = pts[0].y - orthy / 2 + spacing * ny;
        if (openEnded) {
          c.moveTo(outStartX, outStartY);
          fns.push(() => {
            c.lineTo(inEndX, inEndY);
          });
        } else {
          c.moveTo(inEndX, inEndY);
          c.lineTo(outStartX, outStartY);
        }
      }
      let dx1 = 0;
      let dy1 = 0;
      let dist1 = 0;
      for (let i = 0; i < pts.length - 2; i += 1) {
        const pos = relativeCcw(pts[i].x, pts[i].y, pts[i + 1].x, pts[i + 1].y, pts[i + 2].x, pts[i + 2].y);
        dx1 = pts[i + 2].x - pts[i + 1].x;
        dy1 = pts[i + 2].y - pts[i + 1].y;
        dist1 = Math.sqrt(dx1 * dx1 + dy1 * dy1);
        if (dist1 !== 0) {
          nx1 = dx1 / dist1;
          ny1 = dy1 / dist1;
          const tmp1 = nx * nx1 + ny * ny1;
          const tmp = Math.max(Math.sqrt((tmp1 + 1) / 2), 0.04);
          nx2 = nx + nx1;
          ny2 = ny + ny1;
          const dist2 = Math.sqrt(nx2 * nx2 + ny2 * ny2);
          if (dist2 !== 0) {
            nx2 /= dist2;
            ny2 /= dist2;
            const strokeWidthFactor = Math.max(tmp, Math.min(this.strokeWidth / 200 + 0.04, 0.35));
            const angleFactor = pos !== 0 && isRounded ? Math.max(0.1, strokeWidthFactor) : Math.max(tmp, 0.06);
            const outX = pts[i + 1].x + ny2 * edgeWidth / 2 / angleFactor;
            const outY = pts[i + 1].y - nx2 * edgeWidth / 2 / angleFactor;
            const inX = pts[i + 1].x - ny2 * edgeWidth / 2 / angleFactor;
            const inY = pts[i + 1].y + nx2 * edgeWidth / 2 / angleFactor;
            if (pos === 0 || !isRounded) {
              c.lineTo(outX, outY);
              ((x, y) => {
                fns.push(() => {
                  c.lineTo(x, y);
                });
              })(inX, inY);
            } else if (pos === -1) {
              const c1x = inX + ny * edgeWidth;
              const c1y = inY - nx * edgeWidth;
              const c2x = inX + ny1 * edgeWidth;
              const c2y = inY - nx1 * edgeWidth;
              c.lineTo(c1x, c1y);
              c.quadTo(outX, outY, c2x, c2y);
              ((x, y) => {
                fns.push(() => {
                  c.lineTo(x, y);
                });
              })(inX, inY);
            } else {
              c.lineTo(outX, outY);
              ((x, y) => {
                const c1x = outX - ny * edgeWidth;
                const c1y = outY + nx * edgeWidth;
                const c2x = outX - ny1 * edgeWidth;
                const c2y = outY + nx1 * edgeWidth;
                fns.push(() => {
                  c.quadTo(x, y, c1x, c1y);
                });
                fns.push(() => {
                  c.lineTo(c2x, c2y);
                });
              })(inX, inY);
            }
            nx = nx1;
            ny = ny1;
          }
        }
      }
      orthx = edgeWidth * ny1;
      orthy = -edgeWidth * nx1;
      if (markerEnd && !openEnded) {
        this.paintMarker(c, pe.x, pe.y, -nx, -ny, endSize, endWidth, edgeWidth, spacing, false);
      } else {
        c.lineTo(pe.x - spacing * nx1 + orthx / 2, pe.y - spacing * ny1 + orthy / 2);
        const inStartX = pe.x - spacing * nx1 - orthx / 2;
        const inStartY = pe.y - spacing * ny1 - orthy / 2;
        if (!openEnded) {
          c.lineTo(inStartX, inStartY);
        } else {
          c.moveTo(inStartX, inStartY);
          fns.splice(0, 0, () => {
            c.moveTo(inStartX, inStartY);
          });
        }
      }
      for (let i = fns.length - 1; i >= 0; i--) {
        fns[i]();
      }
      if (openEnded) {
        c.end();
        c.stroke();
      } else {
        c.close();
        c.fillAndStroke();
      }
      c.setShadow(false);
      c.setMiterLimit(4);
      if (isRounded) {
        c.setLineJoin("flat");
      }
      if (pts.length > 2) {
        c.setMiterLimit(4);
        if (markerStart && !openEnded) {
          c.begin();
          this.paintMarker(c, pts[0].x, pts[0].y, startNx, startNy, startSize, startWidth, edgeWidth, spacing, true);
          c.stroke();
          c.end();
        }
        if (markerEnd && !openEnded) {
          c.begin();
          this.paintMarker(c, pe.x, pe.y, -nx, -ny, endSize, endWidth, edgeWidth, spacing, true);
          c.stroke();
          c.end();
        }
      }
    }
    /**
     * Paints the marker.
     */
    paintMarker(c, ptX, ptY, nx, ny, size, arrowWidth, edgeWidth, spacing, initialMove) {
      const widthArrowRatio = edgeWidth / arrowWidth;
      const orthx = edgeWidth * ny / 2;
      const orthy = -edgeWidth * nx / 2;
      const spaceX = (spacing + size) * nx;
      const spaceY = (spacing + size) * ny;
      if (initialMove) {
        c.moveTo(ptX - orthx + spaceX, ptY - orthy + spaceY);
      } else {
        c.lineTo(ptX - orthx + spaceX, ptY - orthy + spaceY);
      }
      c.lineTo(ptX - orthx / widthArrowRatio + spaceX, ptY - orthy / widthArrowRatio + spaceY);
      c.lineTo(ptX + spacing * nx, ptY + spacing * ny);
      c.lineTo(ptX + orthx / widthArrowRatio + spaceX, ptY + orthy / widthArrowRatio + spaceY);
      c.lineTo(ptX + orthx + spaceX, ptY + orthy + spaceY);
    }
    /**
     * @returns whether the arrow is rounded
     */
    isArrowRounded() {
      return this.isRounded;
    }
    /**
     * @returns the width of the start arrow
     */
    getStartArrowWidth() {
      return ARROW_WIDTH;
    }
    /**
     * @returns the width of the end arrow
     */
    getEndArrowWidth() {
      return ARROW_WIDTH;
    }
    /**
     * @returns the width of the body of the edge
     */
    getEdgeWidth() {
      return ARROW_WIDTH / 3;
    }
    /**
     * @returns whether the ends of the shape are drawn
     */
    isOpenEnded() {
      return false;
    }
    /**
     * @returns whether the start marker is drawn
     */
    isMarkerStart() {
      var _a2, _b;
      return ((_b = (_a2 = this.style) == null ? void 0 : _a2.startArrow) != null ? _b : NONE) !== NONE;
    }
    /**
     * @returns whether the end marker is drawn
     */
    isMarkerEnd() {
      var _a2, _b;
      return ((_b = (_a2 = this.style) == null ? void 0 : _a2.endArrow) != null ? _b : NONE) !== NONE;
    }
  };
  var ArrowConnectorShape_default = ArrowConnectorShape;

  // node_modules/@maxgraph/core/lib/esm/view/shape/node/DoubleEllipseShape.js
  var DoubleEllipseShape = class extends Shape_default {
    constructor(bounds, fill, stroke, strokeWidth = 1) {
      super();
      this.bounds = bounds;
      this.fill = fill;
      this.stroke = stroke;
      this.strokeWidth = strokeWidth;
    }
    /**
     * Paints the background.
     */
    paintBackground(c, x, y, w, h) {
      c.ellipse(x, y, w, h);
      c.fillAndStroke();
    }
    /**
     * Paints the foreground.
     */
    paintForeground(c, x, y, w, h) {
      var _a2, _b;
      if (!this.outline) {
        const margin = (_b = (_a2 = this.style) == null ? void 0 : _a2.margin) != null ? _b : Math.min(3 + this.strokeWidth, Math.min(w / 5, h / 5));
        x += margin;
        y += margin;
        w -= 2 * margin;
        h -= 2 * margin;
        if (w > 0 && h > 0) {
          c.ellipse(x, y, w, h);
        }
        c.stroke();
      }
    }
    /**
     * @returns the bounds for the label.
     */
    getLabelBounds(rect) {
      var _a2, _b;
      const margin = (_b = (_a2 = this.style) == null ? void 0 : _a2.margin) != null ? _b : Math.min(3 + this.strokeWidth, Math.min(rect.width / 5 / this.scale, rect.height / 5 / this.scale)) * this.scale;
      return new Rectangle_default(rect.x + margin, rect.y + margin, rect.width - 2 * margin, rect.height - 2 * margin);
    }
  };
  var DoubleEllipseShape_default = DoubleEllipseShape;

  // node_modules/@maxgraph/core/lib/esm/view/shape/node/SwimlaneShape.js
  var SwimlaneShape = class extends Shape_default {
    constructor(bounds, fill, stroke, strokeWidth = 1) {
      super();
      this.imageSize = 16;
      this.imageSrc = null;
      this.bounds = bounds;
      this.fill = fill;
      this.stroke = stroke;
      this.strokeWidth = strokeWidth;
    }
    /**
     * Adds roundable support.
     */
    isRoundable(c, x, y, w, h) {
      return true;
    }
    /**
     * Returns the bounding box for the gradient box for this shape.
     */
    getTitleSize() {
      var _a2, _b;
      return Math.max(0, (_b = (_a2 = this.style) == null ? void 0 : _a2.startSize) != null ? _b : StyleDefaultsConfig.startSize);
    }
    /**
     * Returns the bounding box for the gradient box for this shape.
     */
    getLabelBounds(rect) {
      var _a2, _b, _c, _d;
      const start = this.getTitleSize();
      const bounds = new Rectangle_default(rect.x, rect.y, rect.width, rect.height);
      const horizontal = this.isHorizontal();
      const flipH = (_b = (_a2 = this.style) == null ? void 0 : _a2.flipH) != null ? _b : false;
      const flipV = (_d = (_c = this.style) == null ? void 0 : _c.flipV) != null ? _d : false;
      const shapeVertical = this.direction === "north" || this.direction === "south";
      const realHorizontal = horizontal == !shapeVertical;
      const realFlipH = !realHorizontal && flipH !== (this.direction === "south" || this.direction === "west");
      const realFlipV = realHorizontal && flipV !== (this.direction === "south" || this.direction === "west");
      if (!shapeVertical) {
        const tmp = Math.min(bounds.height, start * this.scale);
        if (realFlipH || realFlipV) {
          bounds.y += bounds.height - tmp;
        }
        bounds.height = tmp;
      } else {
        const tmp = Math.min(bounds.width, start * this.scale);
        if (realFlipH || realFlipV) {
          bounds.x += bounds.width - tmp;
        }
        bounds.width = tmp;
      }
      return bounds;
    }
    /**
     * Returns the bounding box for the gradient box for this shape.
     */
    getGradientBounds(c, x, y, w, h) {
      let start = this.getTitleSize();
      if (this.isHorizontal()) {
        start = Math.min(start, h);
        return new Rectangle_default(x, y, w, start);
      }
      start = Math.min(start, w);
      return new Rectangle_default(x, y, start, h);
    }
    /**
     * Returns the arc size for the swimlane.
     */
    getSwimlaneArcSize(w, h, start) {
      var _a2, _b, _c;
      if ((_a2 = this.style) == null ? void 0 : _a2.absoluteArcSize) {
        return Math.min(this.getBaseArcSize(), Math.min(h, w) / 2);
      }
      const roundingFactor = ((_c = (_b = this.style) == null ? void 0 : _b.arcSize) != null ? _c : StyleDefaultsConfig.roundingFactor * 100) / 100;
      return start * roundingFactor * 3;
    }
    /**
     * Paints the swimlane vertex shape.
     */
    isHorizontal() {
      var _a2, _b;
      return (_b = (_a2 = this.style) == null ? void 0 : _a2.horizontal) != null ? _b : true;
    }
    /**
     * Paints the swimlane vertex shape.
     */
    paintVertexShape(c, x, y, w, h) {
      var _a2, _b, _c, _d, _e, _f;
      let start = this.getTitleSize();
      const fill = (_b = (_a2 = this.style) == null ? void 0 : _a2.swimlaneFillColor) != null ? _b : NONE;
      const swimlaneLine = (_d = (_c = this.style) == null ? void 0 : _c.swimlaneLine) != null ? _d : true;
      let r = 0;
      if (this.isHorizontal()) {
        start = Math.min(start, h);
      } else {
        start = Math.min(start, w);
      }
      c.translate(x, y);
      if (!this.isRounded) {
        this.paintSwimlane(c, x, y, w, h, start, fill, swimlaneLine);
      } else {
        r = this.getSwimlaneArcSize(w, h, start);
        r = Math.min((this.isHorizontal() ? h : w) - start, Math.min(start, r));
        this.paintRoundedSwimlane(c, x, y, w, h, start, r, fill, swimlaneLine);
      }
      const sep = (_f = (_e = this.style) == null ? void 0 : _e.separatorColor) != null ? _f : NONE;
      this.paintSeparator(c, x, y, w, h, start, sep);
      if (this.imageSrc) {
        const bounds = this.getImageBounds(x, y, w, h);
        c.image(bounds.x - x, bounds.y - y, bounds.width, bounds.height, this.imageSrc, false, false, false);
      }
      if (this.glass) {
        c.setShadow(false);
        this.paintGlassEffect(c, 0, 0, w, start, r);
      }
    }
    /**
     * Paints the swimlane vertex shape.
     */
    paintSwimlane(c, x, y, w, h, start, fill, swimlaneLine) {
      c.begin();
      let events = true;
      if (this.style && this.style.pointerEvents != null) {
        events = this.style.pointerEvents;
      }
      if (!events && this.fill === NONE) {
        c.pointerEvents = false;
      }
      if (this.isHorizontal()) {
        c.moveTo(0, start);
        c.lineTo(0, 0);
        c.lineTo(w, 0);
        c.lineTo(w, start);
        c.fillAndStroke();
        if (start < h) {
          if (fill === NONE || !events) {
            c.pointerEvents = false;
          }
          if (fill !== NONE) {
            c.setFillColor(fill);
          }
          c.begin();
          c.moveTo(0, start);
          c.lineTo(0, h);
          c.lineTo(w, h);
          c.lineTo(w, start);
          if (fill === NONE) {
            c.stroke();
          } else {
            c.fillAndStroke();
          }
        }
      } else {
        c.moveTo(start, 0);
        c.lineTo(0, 0);
        c.lineTo(0, h);
        c.lineTo(start, h);
        c.fillAndStroke();
        if (start < w) {
          if (fill === NONE || !events) {
            c.pointerEvents = false;
          }
          if (fill !== NONE) {
            c.setFillColor(fill);
          }
          c.begin();
          c.moveTo(start, 0);
          c.lineTo(w, 0);
          c.lineTo(w, h);
          c.lineTo(start, h);
          if (fill === NONE) {
            c.stroke();
          } else {
            c.fillAndStroke();
          }
        }
      }
      if (swimlaneLine) {
        this.paintDivider(c, x, y, w, h, start, fill === NONE);
      }
    }
    /**
     * Paints the swimlane vertex shape.
     */
    paintRoundedSwimlane(c, x, y, w, h, start, r, fill, swimlaneLine) {
      c.begin();
      let events = true;
      if (this.style && this.style.pointerEvents != null) {
        events = this.style.pointerEvents;
      }
      if (!events && this.fill === NONE) {
        c.pointerEvents = false;
      }
      if (this.isHorizontal()) {
        c.moveTo(w, start);
        c.lineTo(w, r);
        c.quadTo(w, 0, w - Math.min(w / 2, r), 0);
        c.lineTo(Math.min(w / 2, r), 0);
        c.quadTo(0, 0, 0, r);
        c.lineTo(0, start);
        c.fillAndStroke();
        if (start < h) {
          if (fill === NONE || !events) {
            c.pointerEvents = false;
          }
          if (fill !== NONE) {
            c.setFillColor(fill);
          }
          c.begin();
          c.moveTo(0, start);
          c.lineTo(0, h - r);
          c.quadTo(0, h, Math.min(w / 2, r), h);
          c.lineTo(w - Math.min(w / 2, r), h);
          c.quadTo(w, h, w, h - r);
          c.lineTo(w, start);
          if (fill === NONE) {
            c.stroke();
          } else {
            c.fillAndStroke();
          }
        }
      } else {
        c.moveTo(start, 0);
        c.lineTo(r, 0);
        c.quadTo(0, 0, 0, Math.min(h / 2, r));
        c.lineTo(0, h - Math.min(h / 2, r));
        c.quadTo(0, h, r, h);
        c.lineTo(start, h);
        c.fillAndStroke();
        if (start < w) {
          if (fill === NONE || !events) {
            c.pointerEvents = false;
          }
          if (fill !== NONE) {
            c.setFillColor(fill);
          }
          c.begin();
          c.moveTo(start, h);
          c.lineTo(w - r, h);
          c.quadTo(w, h, w, h - Math.min(h / 2, r));
          c.lineTo(w, Math.min(h / 2, r));
          c.quadTo(w, 0, w - r, 0);
          c.lineTo(start, 0);
          if (fill === NONE) {
            c.stroke();
          } else {
            c.fillAndStroke();
          }
        }
      }
      if (swimlaneLine) {
        this.paintDivider(c, x, y, w, h, start, fill === NONE);
      }
    }
    /**
     * Paints the divider between swimlane title and content area.
     */
    paintDivider(c, x, y, w, h, start, shadow) {
      if (!shadow) {
        c.setShadow(false);
      }
      c.begin();
      if (this.isHorizontal()) {
        c.moveTo(0, start);
        c.lineTo(w, start);
      } else {
        c.moveTo(start, 0);
        c.lineTo(start, h);
      }
      c.stroke();
    }
    /**
     * Paints the vertical or horizontal separator line between swimlanes.
     */
    paintSeparator(c, x, y, w, h, start, color) {
      if (color !== NONE) {
        c.setStrokeColor(color);
        c.setDashed(true);
        c.begin();
        if (this.isHorizontal()) {
          c.moveTo(w, start);
          c.lineTo(w, h);
        } else {
          c.moveTo(start, 0);
          c.lineTo(w, 0);
        }
        c.stroke();
        c.setDashed(false);
      }
    }
    /**
     * Paints the swimlane vertex shape.
     */
    getImageBounds(x, y, w, h) {
      if (this.isHorizontal()) {
        return new Rectangle_default(x + w - this.imageSize, y, this.imageSize, this.imageSize);
      }
      return new Rectangle_default(x, y, this.imageSize, this.imageSize);
    }
  };
  var SwimlaneShape_default = SwimlaneShape;

  // node_modules/@maxgraph/core/lib/esm/view/shape/node/LabelShape.js
  var LabelShape = class extends RectangleShape_default {
    /**
     * Constructs a new label shape.
     *
     * @param bounds {@link Rectangle} that defines the bounds. This is stored in {@link bounds}.
     * @param fill String that defines the fill color. This is stored in {@link fill}.
     * @param stroke String that defines the stroke color. This is stored in {@link stroke}.
     * @param strokeWidth Optional integer that defines the stroke width. Default is 1. This is stored in {@link strokeWidth}.
     */
    constructor(bounds, fill, stroke, strokeWidth) {
      super(bounds, fill, stroke, strokeWidth);
      this.imageSize = StyleDefaultsConfig.imageSize;
      this.imageSrc = null;
      this.spacing = 2;
      this.indicatorSize = 10;
      this.indicatorSpacing = 2;
      this.indicatorImageSrc = null;
    }
    /**
     * Initializes the shape and the <indicator>.
     */
    init(container) {
      super.init(container);
      if (this.indicatorShape) {
        this.indicator = new this.indicatorShape();
        this.indicator.dialect = this.dialect;
        this.indicator.init(this.node);
      }
    }
    /**
     * Reconfigures this shape. This will update the colors of the indicator
     * and reconfigure it if required.
     */
    redraw() {
      if (this.indicator) {
        this.indicator.fill = this.indicatorColor;
        this.indicator.stroke = this.indicatorStrokeColor;
        this.indicator.gradient = this.indicatorGradientColor;
        this.indicator.direction = this.indicatorDirection;
        this.indicator.redraw();
      }
      super.redraw();
    }
    /**
     * Returns true for non-rounded, non-rotated shapes with no glass gradient and
     * no indicator shape.
     */
    isHtmlAllowed() {
      return super.isHtmlAllowed() && this.indicatorColor === NONE && !!this.indicatorShape;
    }
    /**
     * Generic background painting implementation.
     * @param {mxAbstractCanvas2D} c
     * @param {number} x
     * @param {number} y
     * @param {number} w
     * @param {number} h
     */
    paintForeground(c, x, y, w, h) {
      this.paintImage(c, x, y, w, h);
      this.paintIndicator(c, x, y, w, h);
      super.paintForeground(c, x, y, w, h);
    }
    /**
     * Generic background painting implementation.
     * @param {mxAbstractCanvas2D} c
     * @param {number} x
     * @param {number} y
     * @param {number} w
     * @param {number} h
     */
    paintImage(c, x, y, w, h) {
      if (this.imageSrc) {
        const bounds = this.getImageBounds(x, y, w, h);
        c.image(bounds.x, bounds.y, bounds.width, bounds.height, this.imageSrc, false, false, false);
      }
    }
    /**
     * Generic background painting implementation.
     * @param {number} x
     * @param {number} y
     * @param {number} w
     * @param {number} h
     */
    getImageBounds(x, y, w, h) {
      var _a2, _b, _c, _d, _e, _f, _g, _h, _i, _j;
      const align = (_b = (_a2 = this.style) == null ? void 0 : _a2.imageAlign) != null ? _b : "left";
      const valign = (_d = (_c = this.style) == null ? void 0 : _c.verticalAlign) != null ? _d : "middle";
      const width = (_f = (_e = this.style) == null ? void 0 : _e.imageWidth) != null ? _f : StyleDefaultsConfig.imageSize;
      const height = (_h = (_g = this.style) == null ? void 0 : _g.imageHeight) != null ? _h : StyleDefaultsConfig.imageSize;
      const spacing = (_j = (_i = this.style) == null ? void 0 : _i.spacing) != null ? _j : this.spacing + 5;
      if (align === "center") {
        x += (w - width) / 2;
      } else if (align === "right") {
        x += w - width - spacing;
      } else {
        x += spacing;
      }
      if (valign === "top") {
        y += spacing;
      } else if (valign === "bottom") {
        y += h - height - spacing;
      } else {
        y += (h - height) / 2;
      }
      return new Rectangle_default(x, y, width, height);
    }
    /**
     * Generic background painting implementation.
     * @param {AbstractCanvas2D} c
     * @param {number} x
     * @param {number} y
     * @param {number} w
     * @param {number} h
     */
    paintIndicator(c, x, y, w, h) {
      if (this.indicator) {
        this.indicator.bounds = this.getIndicatorBounds(x, y, w, h);
        this.indicator.paint(c);
      } else if (this.indicatorImageSrc) {
        const bounds = this.getIndicatorBounds(x, y, w, h);
        c.image(bounds.x, bounds.y, bounds.width, bounds.height, this.indicatorImageSrc, false, false, false);
      }
    }
    /**
     * Generic background painting implementation.
     * @param {number} x
     * @param {number} y
     * @param {number} w
     * @param {number} h
     * @returns {Rectangle}
     */
    getIndicatorBounds(x, y, w, h) {
      var _a2, _b, _c, _d, _e, _f, _g, _h;
      const align = (_b = (_a2 = this.style) == null ? void 0 : _a2.imageAlign) != null ? _b : "left";
      const valign = (_d = (_c = this.style) == null ? void 0 : _c.verticalAlign) != null ? _d : "middle";
      const width = (_f = (_e = this.style) == null ? void 0 : _e.indicatorWidth) != null ? _f : this.indicatorSize;
      const height = (_h = (_g = this.style) == null ? void 0 : _g.indicatorHeight) != null ? _h : this.indicatorSize;
      const spacing = this.spacing + 5;
      if (align === "right") {
        x += w - width - spacing;
      } else if (align === "center") {
        x += (w - width) / 2;
      } else {
        x += spacing;
      }
      if (valign === "bottom") {
        y += h - height - spacing;
      } else if (valign === "top") {
        y += spacing;
      } else {
        y += (h - height) / 2;
      }
      return new Rectangle_default(x, y, width, height);
    }
    /**
     * Generic background painting implementation.
     */
    redrawHtmlShape() {
      super.redrawHtmlShape();
      while (this.node.hasChildNodes()) {
        this.node.removeChild(this.node.lastChild);
      }
      if (this.imageSrc && this.bounds) {
        const node = document.createElement("img");
        node.style.position = "relative";
        node.setAttribute("border", "0");
        const bounds = this.getImageBounds(this.bounds.x, this.bounds.y, this.bounds.width, this.bounds.height);
        bounds.x -= this.bounds.x;
        bounds.y -= this.bounds.y;
        node.style.left = `${Math.round(bounds.x)}px`;
        node.style.top = `${Math.round(bounds.y)}px`;
        node.style.width = `${Math.round(bounds.width)}px`;
        node.style.height = `${Math.round(bounds.height)}px`;
        node.src = this.imageSrc;
        this.node.appendChild(node);
      }
    }
  };
  var LabelShape_default = LabelShape;

  // node_modules/@maxgraph/core/lib/esm/view/shape/register-shapes.js
  var isDefaultElementsRegistered = false;
  function registerDefaultShapes() {
    if (!isDefaultElementsRegistered) {
      const shapesToRegister = [
        ["actor", ActorShape_default],
        ["arrow", ArrowShape_default],
        ["arrowConnector", ArrowConnectorShape_default],
        ["cloud", CloudShape_default],
        ["connector", ConnectorShape_default],
        ["cylinder", CylinderShape_default],
        ["doubleEllipse", DoubleEllipseShape_default],
        ["ellipse", EllipseShape_default],
        ["hexagon", HexagonShape_default],
        ["image", ImageShape_default],
        ["label", LabelShape_default],
        ["line", LineShape_default],
        ["rectangle", RectangleShape_default],
        ["rhombus", RhombusShape_default],
        ["swimlane", SwimlaneShape_default],
        ["triangle", TriangleShape_default]
      ];
      for (const [shapeName, shapeClass] of shapesToRegister) {
        ShapeRegistry.add(shapeName, shapeClass);
      }
      isDefaultElementsRegistered = true;
    }
  }

  // node_modules/@maxgraph/core/lib/esm/view/style/register.js
  var registerElbowEdgeStyle = () => {
    EdgeStyleRegistry.add("elbowEdgeStyle", edge_exports.ElbowConnector, {
      handlerKind: "elbow",
      isOrthogonal: true
    });
  };
  var registerEntityRelationEdgeStyle = () => {
    EdgeStyleRegistry.add("entityRelationEdgeStyle", edge_exports.EntityRelation, {
      allowIntermediateHandles: false,
      isOrthogonal: true
    });
  };
  var registerLoopEdgeStyle = () => {
    EdgeStyleRegistry.add("loopEdgeStyle", edge_exports.Loop, {
      handlerKind: "elbow",
      isOrthogonal: false
    });
  };
  var registerManhattanEdgeStyle = () => {
    EdgeStyleRegistry.add("manhattanEdgeStyle", edge_exports.ManhattanConnector, {
      handlerKind: "segment",
      isOrthogonal: true
    });
  };
  var registerOrthogonalEdgeStyle = () => {
    EdgeStyleRegistry.add("orthogonalEdgeStyle", edge_exports.OrthConnector, {
      handlerKind: "segment",
      isOrthogonal: true
    });
  };
  var registerSegmentEdgeStyle = () => {
    EdgeStyleRegistry.add("segmentEdgeStyle", edge_exports.SegmentConnector, {
      handlerKind: "segment",
      isOrthogonal: true
    });
  };
  var registerSideToSideEdgeStyle = () => {
    EdgeStyleRegistry.add("sideToSideEdgeStyle", edge_exports.SideToSide, {
      handlerKind: "elbow",
      isOrthogonal: true
    });
  };
  var registerTopToBottomEdgeStyle = () => {
    EdgeStyleRegistry.add("topToBottomEdgeStyle", edge_exports.TopToBottom, {
      handlerKind: "elbow",
      isOrthogonal: true
    });
  };
  var isDefaultEdgeStylesRegistered = false;
  var registerDefaultEdgeStyles = () => {
    if (!isDefaultEdgeStylesRegistered) {
      registerElbowEdgeStyle();
      registerEntityRelationEdgeStyle();
      registerLoopEdgeStyle();
      registerManhattanEdgeStyle();
      registerOrthogonalEdgeStyle();
      registerSegmentEdgeStyle();
      registerSideToSideEdgeStyle();
      registerTopToBottomEdgeStyle();
      isDefaultEdgeStylesRegistered = true;
    }
  };
  var isDefaultPerimetersRegistered = false;
  var registerDefaultPerimeters = () => {
    if (!isDefaultPerimetersRegistered) {
      const perimetersToRegister = [
        ["ellipsePerimeter", perimeter_exports.EllipsePerimeter],
        ["hexagonPerimeter", perimeter_exports.HexagonPerimeter],
        ["rectanglePerimeter", perimeter_exports.RectanglePerimeter],
        ["rhombusPerimeter", perimeter_exports.RhombusPerimeter],
        ["trianglePerimeter", perimeter_exports.TrianglePerimeter]
      ];
      for (const [name, perimeter] of perimetersToRegister) {
        PerimeterRegistry.add(name, perimeter);
      }
      isDefaultPerimetersRegistered = true;
    }
  };
  var isDefaultMarkersRegistered = false;
  var registerDefaultEdgeMarkers = () => {
    if (!isDefaultMarkersRegistered) {
      const markersToRegister = [
        ["classic", edge_markers_exports.createArrow(2)],
        ["classicThin", edge_markers_exports.createArrow(3)],
        ["block", edge_markers_exports.createArrow(2)],
        ["blockThin", edge_markers_exports.createArrow(3)],
        ["open", edge_markers_exports.createOpenArrow(2)],
        ["openThin", edge_markers_exports.createOpenArrow(3)],
        ["oval", edge_markers_exports.oval],
        ["diamond", edge_markers_exports.diamond],
        ["diamondThin", edge_markers_exports.diamond]
      ];
      for (const [type, factory] of markersToRegister) {
        EdgeMarkerRegistry.add(type, factory);
      }
      isDefaultMarkersRegistered = true;
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/plugin/CellEditorHandler.js
  var CellEditorHandler = class {
    constructor(graph) {
      this.clearOnChange = false;
      this.bounds = null;
      this.resizeThread = null;
      this.textDirection = null;
      this.textarea = null;
      this.editingCell = null;
      this.trigger = null;
      this.modified = false;
      this.autoSize = true;
      this.selectText = true;
      this.emptyLabelText = Client_default.IS_FF ? "<br>" : "";
      this.escapeCancelsEditing = true;
      this.textNode = null;
      this.zIndex = 5;
      this.minResize = new Rectangle_default(0, 20);
      this.wordWrapPadding = 0;
      this.blurEnabled = false;
      this.initialValue = null;
      this.align = null;
      this.graph = graph;
      this.zoomHandler = () => {
        if (this.graph.isEditing()) {
          this.resize();
        }
      };
      this.changeHandler = (_sender) => {
        if (this.editingCell && !this.graph.getView().getState(this.editingCell, false)) {
          this.stopEditing(true);
        }
      };
      this.graph.getView().addListener(InternalEvent_default.SCALE, this.zoomHandler);
      this.graph.getView().addListener(InternalEvent_default.SCALE_AND_TRANSLATE, this.zoomHandler);
      this.graph.getDataModel().addListener(InternalEvent_default.CHANGE, this.changeHandler);
    }
    /**
     * Creates the {@link textarea} and installs the event listeners. The key handler
     * updates the {@link modified} state.
     */
    init() {
      this.textarea = document.createElement("div");
      this.textarea.className = "mxCellEditor mxPlainTextEditor";
      this.textarea.contentEditable = String(true);
      if (Client_default.IS_GC) {
        this.textarea.style.minHeight = "1em";
      }
      this.textarea.style.position = "absolute";
      this.installListeners(this.textarea);
    }
    /**
     * Called in <stopEditing> if cancel is false to invoke {@link AbstractGraph.labelChanged}.
     */
    applyValue(state, value) {
      this.graph.labelChanged(state.cell, value, this.trigger);
    }
    /**
     * Sets the temporary horizontal alignment for the current editing session.
     */
    setAlign(align) {
      if (this.textarea) {
        this.textarea.style.textAlign = align;
      }
      this.align = align;
      this.resize();
    }
    /**
     * Gets the initial editing value for the given cell.
     */
    getInitialValue(state, trigger) {
      let result = htmlEntities(this.graph.getEditingValue(state.cell, trigger), false);
      result = replaceTrailingNewlines(result, "<div><br></div>");
      return result.replace(/\n/g, "<br>");
    }
    /**
     * Returns the current editing value.
     */
    getCurrentValue(state) {
      if (!this.textarea)
        return null;
      return extractTextWithWhitespace(Array.from(this.textarea.childNodes));
    }
    /**
     * Returns true if <escapeCancelsEditing> is true and shift, control and meta
     * are not pressed.
     */
    // isCancelEditingKeyEvent(evt: Event): boolean;
    isCancelEditingKeyEvent(evt) {
      return this.escapeCancelsEditing || isShiftDown(evt) || isControlDown(evt) || isMetaDown(evt);
    }
    /**
     * Installs listeners for focus, change and standard key event handling.
     */
    // installListeners(elt: Element): void;
    installListeners(elt) {
      InternalEvent_default.addListener(elt, "dragstart", (evt) => {
        this.graph.stopEditing(false);
        InternalEvent_default.consume(evt);
      });
      InternalEvent_default.addListener(elt, "blur", (evt) => {
        if (this.blurEnabled) {
          this.focusLost();
        }
      });
      InternalEvent_default.addListener(elt, "keydown", (evt) => {
        if (!isConsumed(evt)) {
          if (this.isStopEditingEvent(evt)) {
            this.graph.stopEditing(false);
            InternalEvent_default.consume(evt);
          } else if (evt.keyCode === 27) {
            this.graph.stopEditing(this.isCancelEditingKeyEvent(evt));
            InternalEvent_default.consume(evt);
          }
        }
      });
      const keypressHandler = (evt) => {
        if (this.editingCell != null) {
          if (this.clearOnChange && elt.innerHTML === this.getEmptyLabelText() && (!Client_default.IS_FF || evt.keyCode !== 8 && evt.keyCode !== 46)) {
            this.clearOnChange = false;
            elt.innerHTML = "";
          }
        }
      };
      InternalEvent_default.addListener(elt, "keypress", keypressHandler);
      InternalEvent_default.addListener(elt, "paste", keypressHandler);
      const keyupHandler = (evt) => {
        if (this.editingCell != null) {
          const textarea = this.textarea;
          if (textarea.innerHTML.length === 0 || textarea.innerHTML === "<br>") {
            textarea.innerHTML = this.getEmptyLabelText();
            this.clearOnChange = textarea.innerHTML.length > 0;
          } else {
            this.clearOnChange = false;
          }
        }
      };
      InternalEvent_default.addListener(elt, "input", keyupHandler);
      InternalEvent_default.addListener(elt, "cut", keyupHandler);
      InternalEvent_default.addListener(elt, "paste", keyupHandler);
      const evtName = "input";
      const resizeHandler = (evt) => {
        if (this.editingCell != null && this.autoSize && !isConsumed(evt)) {
          if (this.resizeThread != null) {
            window.clearTimeout(this.resizeThread);
          }
          this.resizeThread = window.setTimeout(() => {
            this.resizeThread = null;
            this.resize();
          }, 0);
        }
      };
      InternalEvent_default.addListener(elt, evtName, resizeHandler);
      InternalEvent_default.addListener(window, "resize", resizeHandler);
      InternalEvent_default.addListener(elt, "cut", resizeHandler);
      InternalEvent_default.addListener(elt, "paste", resizeHandler);
    }
    /**
     * Returns true if the given keydown event should stop cell editing. This
     * returns true if F2 is pressed of if {@link AbstractGraph.enterStopsCellEditing} is true
     * and enter is pressed without control or shift.
     */
    isStopEditingEvent(evt) {
      return evt.keyCode === 113 || this.graph.isEnterStopsCellEditing() && evt.keyCode === 13 && !isControlDown(evt) && !isShiftDown(evt);
    }
    /**
     * Returns true if this editor is the source for the given native event.
     */
    isEventSource(evt) {
      return getSource(evt) === this.textarea;
    }
    /**
     * Returns {@link odified}.
     */
    resize() {
      var _a2, _b, _c, _d, _e, _f, _g, _h, _i, _j, _k;
      const state = this.editingCell ? this.graph.getView().getState(this.editingCell) : null;
      if (!state) {
        this.stopEditing(true);
      } else if (this.textarea != null) {
        const isEdge = state.cell.isEdge();
        const { scale } = this.graph.getView();
        let m = null;
        if (!this.autoSize || state.style.overflow === "fill") {
          this.bounds = this.getEditorBounds(state);
          this.textarea.style.width = `${Math.round(this.bounds.width / scale)}px`;
          this.textarea.style.height = `${Math.round(this.bounds.height / scale)}px`;
          this.textarea.style.left = `${Math.max(0, Math.round(this.bounds.x + 1))}px`;
          this.textarea.style.top = `${Math.max(0, Math.round(this.bounds.y + 1))}px`;
          if (this.graph.isWrapping(state.cell) && (this.bounds.width >= 2 || this.bounds.height >= 2) && this.textarea.innerHTML !== this.getEmptyLabelText()) {
            this.textarea.style.wordWrap = WORD_WRAP;
            this.textarea.style.whiteSpace = "normal";
            if (state.style.overflow !== "fill") {
              this.textarea.style.width = `${Math.round(this.bounds.width / scale) + this.wordWrapPadding}px`;
            }
          } else {
            this.textarea.style.whiteSpace = "nowrap";
            if (state.style.overflow !== "fill") {
              this.textarea.style.width = "";
            }
          }
        } else {
          const lw = (_a2 = state.style.labelWidth) != null ? _a2 : null;
          m = state.text != null && this.align == null ? state.text.margin : null;
          if (m == null) {
            m = getAlignmentAsPoint((_c = (_b = this.align) != null ? _b : state.style.align) != null ? _c : "center", (_d = state.style.verticalAlign) != null ? _d : "middle");
          }
          if (isEdge) {
            this.bounds = new Rectangle_default(state.absoluteOffset.x, state.absoluteOffset.y, 0, 0);
            if (lw != null) {
              const tmp = (lw + 2) * scale;
              this.bounds.width = tmp;
              this.bounds.x += m.x * tmp;
            }
          } else {
            let bounds = Rectangle_default.fromRectangle(state);
            let hpos = (_e = state.style.labelPosition) != null ? _e : "center";
            let vpos = (_f = state.style.verticalLabelPosition) != null ? _f : "middle";
            bounds = state.shape != null && hpos === "center" && vpos === "middle" ? state.shape.getLabelBounds(bounds) : bounds;
            if (lw != null) {
              bounds.width = lw * scale;
            }
            if (!state.view.graph.cellRenderer.legacySpacing || state.style.overflow !== "width") {
              const dummy = new TextShape_default();
              const spacing = ((_g = state.style.spacing) != null ? _g : 2) * scale;
              const spacingTop = (((_h = state.style.spacingTop) != null ? _h : 0) + dummy.baseSpacingTop) * scale + spacing;
              const spacingRight = (((_i = state.style.spacingRight) != null ? _i : 0) + dummy.baseSpacingRight) * scale + spacing;
              const spacingBottom = (((_j = state.style.spacingBottom) != null ? _j : 0) + dummy.baseSpacingBottom) * scale + spacing;
              const spacingLeft = (((_k = state.style.spacingLeft) != null ? _k : 0) + dummy.baseSpacingLeft) * scale + spacing;
              hpos = state.style.labelPosition != null ? state.style.labelPosition : "center";
              vpos = state.style.verticalLabelPosition != null ? state.style.verticalLabelPosition : "middle";
              bounds = new Rectangle_default(bounds.x + spacingLeft, bounds.y + spacingTop, bounds.width - (hpos === "center" && lw == null ? spacingLeft + spacingRight : 0), bounds.height - (vpos === "middle" ? spacingTop + spacingBottom : 0));
            }
            this.bounds = new Rectangle_default(bounds.x + state.absoluteOffset.x, bounds.y + state.absoluteOffset.y, bounds.width, bounds.height);
          }
          if (this.graph.isWrapping(state.cell) && (this.bounds.width >= 2 || this.bounds.height >= 2) && this.textarea.innerHTML !== this.getEmptyLabelText()) {
            this.textarea.style.wordWrap = WORD_WRAP;
            this.textarea.style.whiteSpace = "normal";
            const tmp = Math.round(this.bounds.width / scale) + this.wordWrapPadding;
            if (this.textarea.style.position !== "relative") {
              this.textarea.style.width = `${tmp}px`;
              if (this.textarea.scrollWidth > tmp) {
                this.textarea.style.width = `${this.textarea.scrollWidth}px`;
              }
            } else {
              this.textarea.style.maxWidth = `${tmp}px`;
            }
          } else {
            this.textarea.style.whiteSpace = "nowrap";
            this.textarea.style.width = "";
          }
          const ow = this.textarea.scrollWidth;
          const oh = this.textarea.scrollHeight;
          this.textarea.style.left = `${Math.max(0, Math.round(this.bounds.x - m.x * (this.bounds.width - 2)) + 1)}px`;
          this.textarea.style.top = `${Math.max(0, Math.round(this.bounds.y - m.y * (this.bounds.height - 4) + (m.y === -1 ? 3 : 0)) + 1)}px`;
        }
        setPrefixedStyle(this.textarea.style, "transformOrigin", "0px 0px");
        setPrefixedStyle(this.textarea.style, "transform", `scale(${scale},${scale})${m == null ? "" : ` translate(${m.x * 100}%,${m.y * 100}%)`}`);
      }
    }
    /**
     * Called if the textarea has lost focus.
     */
    focusLost() {
      this.stopEditing(!this.graph.isInvokesStopCellEditing());
    }
    /**
     * Returns the background color for the in-place editor. This implementation
     * always returns NONE.
     */
    getBackgroundColor(state) {
      return NONE;
    }
    /**
     * Starts the editor for the given cell.
     *
     * @param cell <Cell> to start editing.
     * @param trigger Optional mouse event that triggered the editor.
     */
    startEditing(cell, trigger = null) {
      var _a2, _b, _c, _d, _e, _f;
      this.stopEditing(true);
      this.align = null;
      if (this.textarea == null) {
        this.init();
      }
      const tooltipHandler = this.graph.getPlugin("TooltipHandler");
      tooltipHandler == null ? void 0 : tooltipHandler.hideTooltip();
      const state = this.graph.getView().getState(cell);
      if (state) {
        const stateStyle = state.style;
        const size = (_a2 = stateStyle.fontSize) != null ? _a2 : StyleDefaultsConfig.fontSize;
        const family = (_b = stateStyle.fontFamily) != null ? _b : StyleDefaultsConfig.fontFamily;
        const color = (_c = stateStyle.fontColor) != null ? _c : "black";
        const align = (_d = stateStyle.align) != null ? _d : "left";
        const fontStyle = (_e = stateStyle.fontStyle) != null ? _e : 0;
        const bold = matchBinaryMask(fontStyle, FONT_STYLE_MASK.BOLD);
        const italic = matchBinaryMask(fontStyle, FONT_STYLE_MASK.ITALIC);
        const txtDecor = [];
        matchBinaryMask(fontStyle, FONT_STYLE_MASK.UNDERLINE) && txtDecor.push("underline");
        matchBinaryMask(fontStyle, FONT_STYLE_MASK.STRIKETHROUGH) && txtDecor.push("line-through");
        const textarea = this.textarea;
        const textareaStyle = textarea.style;
        textareaStyle.lineHeight = ABSOLUTE_LINE_HEIGHT ? `${Math.round(size * LINE_HEIGHT)}px` : String(LINE_HEIGHT);
        textareaStyle.backgroundColor = this.getBackgroundColor(state) || "transparent";
        textareaStyle.textDecoration = txtDecor.join(" ");
        textareaStyle.fontWeight = bold ? "bold" : "normal";
        textareaStyle.fontStyle = italic ? "italic" : "";
        textareaStyle.fontSize = `${Math.round(size)}px`;
        textareaStyle.zIndex = String(this.zIndex);
        textareaStyle.fontFamily = family;
        textareaStyle.textAlign = align;
        textareaStyle.outline = "none";
        textareaStyle.color = color;
        let dir = this.textDirection = (_f = stateStyle.textDirection) != null ? _f : DEFAULT_TEXT_DIRECTION;
        const stateText = state.text;
        if (dir === "auto") {
          if (stateText !== null && stateText.dialect !== "strictHtml" && !isNode(stateText.value)) {
            dir = stateText.getAutoDirection();
          }
        }
        if (dir === "ltr" || dir === "rtl") {
          textarea.setAttribute("dir", dir);
        } else {
          textarea.removeAttribute("dir");
        }
        textarea.innerHTML = this.getInitialValue(state, trigger) || "";
        this.initialValue = textarea.innerHTML;
        if (textarea.innerHTML.length === 0 || textarea.innerHTML === "<br>") {
          textarea.innerHTML = this.getEmptyLabelText();
          this.clearOnChange = true;
        } else {
          this.clearOnChange = textarea.innerHTML === this.getEmptyLabelText();
        }
        this.graph.container.appendChild(textarea);
        this.editingCell = cell;
        this.trigger = trigger;
        this.textNode = null;
        if (stateText !== null && this.isHideLabel(state)) {
          this.textNode = stateText.node;
          this.textNode.style.visibility = "hidden";
        }
        if (this.autoSize && (state.cell.isEdge() || stateStyle.overflow !== "fill")) {
          window.setTimeout(() => {
            this.resize();
          }, 0);
        }
        this.resize();
        try {
          textarea.focus();
          if (this.isSelectText() && textarea.innerHTML.length > 0 && (textarea.innerHTML !== this.getEmptyLabelText() || !this.clearOnChange)) {
            document.execCommand("selectAll", false);
          }
        } catch (e) {
        }
      }
    }
    /**
     * Returns <selectText>.
     */
    isSelectText() {
      return this.selectText;
    }
    /**
     * Stops the editor and applies the value if cancel is false.
     */
    stopEditing(cancel = false) {
      if (this.editingCell) {
        if (this.textNode) {
          this.textNode.style.visibility = "visible";
          this.textNode = null;
        }
        const state = !cancel ? this.graph.view.getState(this.editingCell) : null;
        const textarea = this.textarea;
        const initial = this.initialValue;
        this.initialValue = null;
        this.editingCell = null;
        this.bounds = null;
        textarea.blur();
        clearSelection();
        if (textarea.parentNode) {
          textarea.parentNode.removeChild(textarea);
        }
        if (this.clearOnChange && textarea.innerHTML === this.getEmptyLabelText()) {
          textarea.innerHTML = "";
          this.clearOnChange = false;
        }
        if (state && (textarea.innerHTML !== initial || this.align !== null)) {
          this.prepareTextarea();
          const value = this.getCurrentValue(state);
          this.graph.batchUpdate(() => {
            if (value !== null) {
              this.applyValue(state, value);
            }
            if (this.align !== null) {
              this.graph.setCellStyles("align", this.align, [state.cell]);
            }
          });
        }
        this.trigger = null;
        if (this.textarea)
          InternalEvent_default.release(this.textarea);
        this.textarea = null;
        this.align = null;
      }
    }
    /**
     * Prepares the textarea for getting its value in <stopEditing>.
     * This implementation removes the extra trailing linefeed in Firefox.
     */
    prepareTextarea() {
      const textarea = this.textarea;
      if (textarea.lastChild && textarea.lastChild.nodeName === "BR") {
        textarea.removeChild(textarea.lastChild);
      }
    }
    /**
     * Returns true if the label should be hidden while the cell is being
     * edited.
     */
    isHideLabel(state = null) {
      return true;
    }
    /**
     * Returns the minimum width and height for editing the given state.
     */
    getMinimumSize(state) {
      const { scale } = this.graph.getView();
      const textarea = this.textarea;
      return new Rectangle_default(0, 0, state.text === null ? 30 : state.text.size * scale + 20, textarea.style.textAlign === "left" ? 120 : 40);
    }
    /**
     * Returns the {@link Rectangle} that defines the bounds of the editor.
     */
    getEditorBounds(state) {
      var _a2, _b, _c, _d, _e, _f;
      const isEdge = state.cell.isEdge();
      const { scale } = this.graph.getView();
      const minSize = this.getMinimumSize(state);
      const minWidth = minSize.width;
      const minHeight = minSize.height;
      let result = null;
      if (!isEdge && state.view.graph.cellRenderer.legacySpacing && state.style.overflow === "fill") {
        result = state.shape.getLabelBounds(Rectangle_default.fromRectangle(state));
      } else {
        const dummy = new TextShape_default();
        const spacing = ((_a2 = state.style.spacing) != null ? _a2 : 0) * scale;
        const spacingTop = (((_b = state.style.spacingTop) != null ? _b : 0) + dummy.baseSpacingTop) * scale + spacing;
        const spacingRight = (((_c = state.style.spacingRight) != null ? _c : 0) + dummy.baseSpacingRight) * scale + spacing;
        const spacingBottom = (((_d = state.style.spacingBottom) != null ? _d : 0) + dummy.baseSpacingBottom) * scale + spacing;
        const spacingLeft = (((_e = state.style.spacingLeft) != null ? _e : 0) + dummy.baseSpacingLeft) * scale + spacing;
        result = new Rectangle_default(state.x, state.y, Math.max(minWidth, state.width - spacingLeft - spacingRight), Math.max(minHeight, state.height - spacingTop - spacingBottom));
        const hpos = state.style.labelPosition != null ? state.style.labelPosition : "center";
        const vpos = state.style.verticalLabelPosition != null ? state.style.verticalLabelPosition : "middle";
        result = state.shape != null && hpos === "center" && vpos === "middle" ? state.shape.getLabelBounds(result) : result;
        if (isEdge) {
          result.x = state.absoluteOffset.x;
          result.y = state.absoluteOffset.y;
          if (state.text != null && state.text.boundingBox != null) {
            if (state.text.boundingBox.x > 0) {
              result.x = state.text.boundingBox.x;
            }
            if (state.text.boundingBox.y > 0) {
              result.y = state.text.boundingBox.y;
            }
          }
        } else if (state.text != null && state.text.boundingBox != null) {
          result.x = Math.min(result.x, state.text.boundingBox.x);
          result.y = Math.min(result.y, state.text.boundingBox.y);
        }
        result.x += spacingLeft;
        result.y += spacingTop;
        if (state.text != null && state.text.boundingBox != null) {
          if (!isEdge) {
            result.width = Math.max(result.width, state.text.boundingBox.width);
            result.height = Math.max(result.height, state.text.boundingBox.height);
          } else {
            result.width = Math.max(minWidth, state.text.boundingBox.width);
            result.height = Math.max(minHeight, state.text.boundingBox.height);
          }
        }
        if (state.cell.isVertex()) {
          const horizontal = (_f = state.style.labelPosition) != null ? _f : "center";
          if (horizontal === "left") {
            result.x -= state.width;
          } else if (horizontal === "right") {
            result.x += state.width;
          }
          const vertical = state.style.verticalLabelPosition != null ? state.style.verticalLabelPosition : "middle";
          if (vertical === "top") {
            result.y -= state.height;
          } else if (vertical === "bottom") {
            result.y += state.height;
          }
        }
      }
      return new Rectangle_default(Math.round(result.x), Math.round(result.y), Math.round(result.width), Math.round(result.height));
    }
    /**
     * Returns the initial label value to be used of the label of the given
     * cell is empty. This label is displayed and cleared on the first keystroke.
     * This implementation returns <emptyLabelText>.
     *
     * @param cell <Cell> for which a text for an empty editing box should be
     * returned.
     */
    getEmptyLabelText(cell = null) {
      var _a2;
      return (_a2 = this.emptyLabelText) != null ? _a2 : "";
    }
    /**
     * Returns the cell that is currently being edited or null if no cell is
     * being edited.
     */
    getEditingCell() {
      return this.editingCell;
    }
    /**
     * Destroys the editor and removes all associated resources.
     */
    onDestroy() {
      if (this.textarea) {
        InternalEvent_default.release(this.textarea);
        if (this.textarea.parentNode) {
          this.textarea.parentNode.removeChild(this.textarea);
        }
        this.textarea = null;
      }
      this.graph.getDataModel().removeListener(this.changeHandler);
      this.graph.getView().removeListener(this.zoomHandler);
    }
  };
  CellEditorHandler.pluginId = "CellEditorHandler";
  var CellEditorHandler_default = CellEditorHandler;

  // node_modules/@maxgraph/core/lib/esm/view/plugin/TooltipHandler.js
  var TooltipHandler = class {
    /**
     * Creates the tooltip element and appends it to the document body.
     *
     */
    init() {
      if (document.body) {
        this.div = document.createElement("div");
        this.div.className = "mxTooltip";
        this.div.style.visibility = "hidden";
        document.body.appendChild(this.div);
        InternalEvent_default.addGestureListeners(this.div, (evt) => {
          const source = getSource(evt);
          if (source && source.nodeName !== "A") {
            this.hideTooltip();
          }
        });
        InternalEvent_default.addListener(this.graph.getContainer(), "mouseleave", (evt) => {
          if (this.div !== evt.relatedTarget) {
            this.hide();
          }
        });
      }
    }
    /**
     * Constructs an event handler that displays tooltips.
     *
     * @param graph Reference to the enclosing {@link AbstractGraph}.
     */
    constructor(graph) {
      this.zIndex = 10005;
      this.delay = 500;
      this.ignoreTouchEvents = true;
      this.hideOnHover = false;
      this.destroyed = false;
      this.lastX = 0;
      this.lastY = 0;
      this.state = null;
      this.stateSource = false;
      this.thread = null;
      this.enabled = false;
      this.graph = graph;
      this.graph.addMouseListener(this);
    }
    /**
     * Returns `true` if events are handled.
     *
     * This implementation returns {@link enabled}.
     */
    isEnabled() {
      return this.enabled;
    }
    /**
     * Enables or disables event handling.
     *
     * This implementation updates {@link enabled}.
     */
    setEnabled(enabled) {
      this.enabled = enabled;
    }
    /**
     * Returns {@link hideOnHover}.
     */
    isHideOnHover() {
      return this.hideOnHover;
    }
    /**
     * Sets {@link hideOnHover}.
     */
    setHideOnHover(value) {
      this.hideOnHover = value;
    }
    /**
     * Returns the {@link CellState}. to be used for showing a tooltip for this event.
     */
    getStateForEvent(me) {
      return me.getState();
    }
    mouseDown(_sender, me) {
      this.reset(me, false);
      this.hideTooltip();
    }
    mouseMove(_sender, me) {
      if (me.getX() !== this.lastX || me.getY() !== this.lastY) {
        this.reset(me, true);
        const state = this.getStateForEvent(me);
        if (this.isHideOnHover() || state !== this.state || me.getSource() !== this.node && (!this.stateSource || state != null && this.stateSource === (me.isSource(state.shape) || !me.isSource(state.text)))) {
          this.hideTooltip();
        }
      }
      this.lastX = me.getX();
      this.lastY = me.getY();
    }
    /**
     * Handles the event by resetting the tooltip timer or hiding the existing tooltip.
     */
    mouseUp(_sender, me) {
      this.reset(me, true);
      this.hideTooltip();
    }
    /**
     * Resets the timer.
     */
    resetTimer() {
      if (this.thread) {
        window.clearTimeout(this.thread);
        this.thread = null;
      }
    }
    /**
     * Resets and/or restarts the timer to trigger the display of the tooltip.
     */
    reset(me, restart, state = null) {
      if (!this.ignoreTouchEvents || isMouseEvent(me.getEvent())) {
        this.resetTimer();
        state = state != null ? state : this.getStateForEvent(me);
        if (restart && this.isEnabled() && state && (!this.div || this.div.style.visibility == "hidden")) {
          const node = me.getSource();
          const x = me.getX();
          const y = me.getY();
          const stateSource = me.isSource(state.shape) || me.isSource(state.text);
          const popupMenuHandler = this.graph.getPlugin("PopupMenuHandler");
          this.thread = window.setTimeout(() => {
            if (state && node && !this.graph.isEditing() && !(popupMenuHandler == null ? void 0 : popupMenuHandler.isMenuShowing()) && !this.graph.isMouseDown) {
              const tip = this.getTooltip(state, node, x, y);
              this.show(tip, x, y);
              this.state = state;
              this.node = node;
              this.stateSource = stateSource;
            }
          }, this.delay);
        }
      }
    }
    /**
     * Hides the tooltip and resets the timer.
     */
    hide() {
      this.resetTimer();
      this.hideTooltip();
    }
    /**
     * Hides the tooltip.
     */
    hideTooltip() {
      if (this.div) {
        this.div.style.visibility = "hidden";
        this.div.innerHTML = "";
      }
    }
    /**
     * Shows the tooltip for the specified cell and optional index at the
     * specified location (with a vertical offset of 10 pixels).
     */
    show(tip, x, y) {
      if (!this.destroyed && tip && tip !== "") {
        const origin = getScrollOrigin();
        if (!this.div) {
          this.init();
        }
        this.div.style.zIndex = String(this.zIndex);
        this.div.style.left = `${x + origin.x}px`;
        this.div.style.top = `${y + TOOLTIP_VERTICAL_OFFSET + origin.y}px`;
        if (!isNode(tip)) {
          this.div.innerHTML = tip.replace(/\n/g, "<br>");
        } else {
          this.div.innerHTML = "";
          this.div.appendChild(tip);
        }
        this.div.style.visibility = "";
        fit(this.div);
      }
    }
    /**
     * Destroys the handler and all its resources and DOM nodes.
     */
    onDestroy() {
      var _a2;
      if (!this.destroyed) {
        this.resetTimer();
        this.graph.removeMouseListener(this);
        if (this.div) {
          InternalEvent_default.release(this.div);
        }
        if ((_a2 = this.div) == null ? void 0 : _a2.parentNode) {
          this.div.parentNode.removeChild(this.div);
        }
        this.destroyed = true;
        this.div = null;
      }
    }
    /**
     * Returns the string or DOM node that represents the tooltip for the given state, node and coordinate pair.
     *
     * This implementation checks if the given node is a folding icon or overlay and returns the respective tooltip.
     * - If this does not result in a tooltip, the handler for the cell is retrieved from {@link SelectionCellsHandler} and the optional `getTooltipForNode` method is called.
     * - If no special tooltip exists here then {@link getTooltipForCell} is used with the cell in the given state as the argument to return a tooltip for the given state.
     *
     * @param state {@link CellState} whose tooltip should be returned.
     * @param node DOM node that is currently under the mouse.
     * @param x X-coordinate of the mouse.
     * @param y Y-coordinate of the mouse.
     *
     * @since 0.23.0
     */
    getTooltip(state, node, x, y) {
      let tip = null;
      if (state.control && (node === state.control.node || node.parentNode === state.control.node)) {
        tip = this.graph.getCollapseExpandResource();
        tip = htmlEntities(translate(tip) || tip, true).replace(/\\n/g, "<br>");
      }
      if (!tip && state.overlays) {
        state.overlays.forEach((shape) => {
          var _a2;
          if (!tip && (node === shape.node || node.parentNode === shape.node)) {
            tip = shape.overlay ? (_a2 = shape.overlay.toString()) != null ? _a2 : null : null;
          }
        });
      }
      if (!tip) {
        const selectionCellsHandler = this.graph.getPlugin("SelectionCellsHandler");
        const handler = selectionCellsHandler == null ? void 0 : selectionCellsHandler.getHandler(state.cell);
        if (handler && // this method exists at least in EdgeSegmentHandler and ElbowEdgeHandler
        "getTooltipForNode" in handler && typeof handler.getTooltipForNode === "function") {
          tip = handler.getTooltipForNode(node);
        }
      }
      if (!tip) {
        tip = this.getTooltipForCell(state.cell);
      }
      return tip;
    }
    /**
     * Returns the string or DOM node to be used as the tooltip for the given cell.
     * This implementation uses the {@link Cell.getTooltip} function if it exists, or else it returns {@link convertValueToString} for the cell.
     *
     * To replace all tooltips with the string "Hello, World!", use the following code:
     *
     * ```typescript
     * const tooltipHandler = graph.getPlugin<TooltipHandler>('TooltipHandler')!;
     * tooltipHandler.getTooltipForCell = function(cell) {
     *   return 'Hello, World!';
     * }
     * ```
     *
     * @param cell {@link Cell} whose tooltip should be returned.
     *
     * @since 0.23.0
     */
    getTooltipForCell(cell) {
      if (cell && "getTooltip" in cell && typeof cell.getTooltip === "function") {
        return cell.getTooltip();
      }
      return this.graph.convertValueToString(cell);
    }
  };
  TooltipHandler.pluginId = "TooltipHandler";
  var TooltipHandler_default = TooltipHandler;

  // node_modules/@maxgraph/core/lib/esm/view/plugin/SelectionCellsHandler.js
  var SelectionCellsHandler = class extends EventSource_default {
    constructor(graph) {
      super();
      this.enabled = true;
      this.maxHandlers = 100;
      this.graph = graph;
      this.handlers = /* @__PURE__ */ new Map();
      this.graph.addMouseListener(this);
      this.refreshHandler = () => {
        if (this.isEnabled()) {
          this.refresh();
        }
      };
      this.graph.getSelectionModel().addListener(InternalEvent_default.CHANGE, this.refreshHandler);
      this.graph.getDataModel().addListener(InternalEvent_default.CHANGE, this.refreshHandler);
      this.graph.getView().addListener(InternalEvent_default.SCALE, this.refreshHandler);
      this.graph.getView().addListener(InternalEvent_default.TRANSLATE, this.refreshHandler);
      this.graph.getView().addListener(InternalEvent_default.SCALE_AND_TRANSLATE, this.refreshHandler);
      this.graph.getView().addListener(InternalEvent_default.DOWN, this.refreshHandler);
      this.graph.getView().addListener(InternalEvent_default.UP, this.refreshHandler);
    }
    /**
     * Returns {@link enabled}.
     */
    isEnabled() {
      return this.enabled;
    }
    /**
     * Sets {@link enabled}.
     */
    setEnabled(value) {
      this.enabled = value;
    }
    /**
     * Returns the handler for the given cell.
     */
    getHandler(cell) {
      return this.handlers.get(cell);
    }
    /**
     * Returns true if the given cell has a handler.
     */
    isHandled(cell) {
      return !!this.getHandler(cell);
    }
    /**
     * Resets all handlers.
     */
    reset() {
      this.handlers.forEach((handler) => {
        handler.reset.apply(handler);
      });
    }
    /**
     * Reloads or updates all handlers.
     */
    getHandledSelectionCells() {
      return this.graph.getSelectionCells();
    }
    /**
     * Reloads or updates all handlers.
     */
    refresh() {
      var _a2;
      const oldHandlers = this.handlers;
      this.handlers = /* @__PURE__ */ new Map();
      const tmp = sortCells(this.getHandledSelectionCells(), false);
      for (let i = 0; i < tmp.length; i += 1) {
        const state = this.graph.view.getState(tmp[i]);
        if (state) {
          let handler = (_a2 = oldHandlers.get(tmp[i])) != null ? _a2 : null;
          oldHandlers.delete(tmp[i]);
          if (handler) {
            if (handler.state !== state) {
              handler.onDestroy();
              handler = null;
            } else if (!this.isHandlerActive(handler)) {
              if (handler.refresh)
                handler.refresh();
              handler.redraw();
            }
          }
          if (handler) {
            this.handlers.set(tmp[i], handler);
          }
        }
      }
      oldHandlers.forEach((handler) => {
        this.fireEvent(new EventObject_default(InternalEvent_default.REMOVE, { state: handler.state }));
        handler.onDestroy();
      });
      for (let i = 0; i < tmp.length; i += 1) {
        const state = this.graph.view.getState(tmp[i]);
        if (state) {
          let handler = this.handlers.get(tmp[i]);
          if (!handler) {
            handler = this.graph.createHandler(state);
            this.fireEvent(new EventObject_default(InternalEvent_default.ADD, { state }));
            this.handlers.set(tmp[i], handler);
          } else {
            handler.updateParentHighlight();
          }
        }
      }
    }
    /**
     * Returns true if the given handler is active and should not be redrawn.
     */
    isHandlerActive(handler) {
      return handler.index !== null;
    }
    /**
     * Updates the handler for the given shape if one exists.
     */
    updateHandler(state) {
      let handler = this.handlers.get(state.cell);
      this.handlers.delete(state.cell);
      if (handler) {
        const { index } = handler;
        const x = handler.startX;
        const y = handler.startY;
        handler.onDestroy();
        handler = this.graph.createHandler(state);
        if (handler) {
          this.handlers.set(state.cell, handler);
          if (index !== null) {
            handler.start(x, y, index);
          }
        }
      }
    }
    /**
     * Redirects the given event to the handlers.
     */
    mouseDown(sender, me) {
      if (this.graph.isEnabled() && this.isEnabled()) {
        this.handlers.forEach((handler) => {
          handler.mouseDown(sender, me);
        });
      }
    }
    /**
     * Redirects the given event to the handlers.
     */
    mouseMove(sender, me) {
      if (this.graph.isEnabled() && this.isEnabled()) {
        this.handlers.forEach((handler) => {
          handler.mouseMove(sender, me);
        });
      }
    }
    /**
     * Redirects the given event to the handlers.
     */
    mouseUp(sender, me) {
      if (this.graph.isEnabled() && this.isEnabled()) {
        this.handlers.forEach((handler) => {
          handler.mouseUp(sender, me);
        });
      }
    }
    /**
     * Destroys the handler and all its resources and DOM nodes.
     */
    onDestroy() {
      this.graph.removeMouseListener(this);
      this.graph.getSelectionModel().removeListener(this.refreshHandler);
      this.graph.getDataModel().removeListener(this.refreshHandler);
      this.graph.getView().removeListener(this.refreshHandler);
      super.destroy();
    }
  };
  SelectionCellsHandler.pluginId = "SelectionCellsHandler";
  var SelectionCellsHandler_default = SelectionCellsHandler;

  // node_modules/@maxgraph/core/lib/esm/gui/MaxPopupMenu.js
  var MaxPopupMenu = class extends EventSource_default {
    constructor(factoryMethod) {
      super();
      this.activeRow = null;
      this.eventReceiver = null;
      this.submenuImage = `${Client_default.imageBasePath}/submenu.gif`;
      this.zIndex = 10006;
      this.useLeftButtonForPopup = false;
      this.enabled = true;
      this.itemCount = 0;
      this.autoExpand = false;
      this.smartSeparators = false;
      this.labels = true;
      this.willAddSeparator = false;
      this.containsItems = false;
      if (factoryMethod) {
        this.factoryMethod = factoryMethod;
      }
      this.table = document.createElement("table");
      this.table.className = "mxPopupMenu";
      this.tbody = document.createElement("tbody");
      this.table.appendChild(this.tbody);
      this.div = document.createElement("div");
      this.div.className = "mxPopupMenu";
      this.div.style.display = "inline";
      this.div.style.zIndex = String(this.zIndex);
      this.div.appendChild(this.table);
      InternalEvent_default.disableContextMenu(this.div);
    }
    /**
     * Returns true if events are handled. This implementation
     * returns <enabled>.
     */
    isEnabled() {
      return this.enabled;
    }
    /**
     * Enables or disables event handling. This implementation
     * updates <enabled>.
     */
    setEnabled(enabled) {
      this.enabled = enabled;
    }
    /**
     * Returns true if the given event is a popupmenu trigger for the optional
     * given cell.
     *
     * @param me {@link MouseEvent} that represents the mouse event.
     */
    isPopupTrigger(me) {
      return me.isPopupTrigger() || this.useLeftButtonForPopup && isLeftMouseButton(me.getEvent());
    }
    /**
     * Adds the given item to the given parent item. If no parent item is specified then the item is added to the top-level menu.
     *
     * The return value may be used as the parent argument, i.e. as a submenu item.
     * The return value is the table row that represents the item.
     *
     * @param title String that represents the title of the menu item.
     * @param image Optional URL for the image icon.
     * @param funct Function associated that takes a `mouseup` or `touchend` event.
     * @param parent Optional item returned by {@link addItem}.
     * @param iconCls Optional string that represents the CSS class for the image icon. It is ignored if image is given.
     * @param enabled Optional boolean indicating if the item is enabled. Default is `true`.
     * @param active Optional boolean indicating if the menu should implement any event handling. Default is `true`.
     * @param noHover Optional boolean to disable hover state. Default is `false`.
     */
    addItem(title, image, funct, parent = null, iconCls = null, enabled = true, active = true, noHover = false) {
      var _a2;
      parent = parent != null ? parent : this;
      this.itemCount++;
      if (parent.willAddSeparator) {
        if (parent.containsItems) {
          this.addSeparator(parent, true);
        }
        parent.willAddSeparator = false;
      }
      parent.containsItems = true;
      const tr = document.createElement("tr");
      tr.className = "mxPopupMenuItem";
      const col1 = document.createElement("td");
      col1.className = "mxPopupMenuIcon";
      if (image) {
        const img = document.createElement("img");
        img.src = image;
        col1.appendChild(img);
      } else if (iconCls) {
        const div = document.createElement("div");
        div.className = iconCls;
        col1.appendChild(div);
      }
      tr.appendChild(col1);
      if (this.labels) {
        const col2 = document.createElement("td");
        col2.className = `mxPopupMenuItem${!enabled ? " mxDisabled" : ""}`;
        write(col2, title);
        col2.align = "left";
        tr.appendChild(col2);
        const col3 = document.createElement("td");
        col3.className = `mxPopupMenuItem${!enabled ? " mxDisabled" : ""}`;
        col3.style.paddingRight = "6px";
        col3.style.textAlign = "right";
        tr.appendChild(col3);
        if (parent.div == null) {
          this.createSubmenu(parent);
        }
      }
      (_a2 = parent.tbody) == null ? void 0 : _a2.appendChild(tr);
      if (active && enabled) {
        InternalEvent_default.addGestureListeners(tr, (evt) => {
          this.eventReceiver = tr;
          if (parent && parent.activeRow != tr && parent.activeRow != parent) {
            if (parent.activeRow && parent.activeRow.div.parentNode) {
              this.hideSubmenu(parent);
            }
            if (tr.div) {
              this.showSubmenu(parent, tr);
              parent.activeRow = tr;
            }
          }
          InternalEvent_default.consume(evt);
        }, (_evt) => {
          if (parent && parent.activeRow != tr && parent.activeRow != parent) {
            if (parent.activeRow && parent.activeRow.div.parentNode) {
              this.hideSubmenu(parent);
            }
            if (this.autoExpand && tr.div) {
              this.showSubmenu(parent, tr);
              parent.activeRow = tr;
            }
          }
          if (!noHover) {
            tr.className = "mxPopupMenuItemHover";
          }
        }, (evt) => {
          if (this.eventReceiver == tr) {
            if (parent && parent.activeRow != tr) {
              this.hideMenu();
            }
            funct == null ? void 0 : funct(evt);
          }
          this.eventReceiver = null;
          InternalEvent_default.consume(evt);
        });
        if (!noHover) {
          InternalEvent_default.addListener(tr, "mouseout", (_evt) => {
            tr.className = "mxPopupMenuItem";
          });
        }
      }
      return tr;
    }
    /**
     * Adds a checkmark to the given menuitem.
     */
    addCheckmark(item, img) {
      if (item.firstChild) {
        const td = item.firstChild.nextSibling;
        td.style.backgroundImage = `url('${img}')`;
        td.style.backgroundRepeat = "no-repeat";
        td.style.backgroundPosition = "2px 50%";
      }
    }
    /**
     * Creates the nodes required to add submenu items inside the given parent
     * item. This is called in <addItem> if a parent item is used for the first
     * time. This adds various DOM nodes and a <submenuImage> to the parent.
     *
     * @param parent An item returned by <addItem>.
     */
    createSubmenu(parent) {
      var _a2, _b;
      parent.table = document.createElement("table");
      parent.table.className = "mxPopupMenu";
      parent.tbody = document.createElement("tbody");
      parent.table.appendChild(parent.tbody);
      parent.div = document.createElement("div");
      parent.div.className = "mxPopupMenu";
      parent.div.style.position = "absolute";
      parent.div.style.display = "inline";
      parent.div.style.zIndex = String(this.zIndex);
      parent.div.appendChild(parent.table);
      const img = document.createElement("img");
      img.setAttribute("src", this.submenuImage);
      if ((_b = (_a2 = parent.firstChild) == null ? void 0 : _a2.nextSibling) == null ? void 0 : _b.nextSibling) {
        const td = parent.firstChild.nextSibling.nextSibling;
        td.appendChild(img);
      }
    }
    /**
     * Shows the submenu inside the given parent row.
     */
    showSubmenu(parent, row) {
      if (row.div) {
        row.div.style.left = `${parent.div.offsetLeft + row.offsetLeft + row.offsetWidth - 1}px`;
        row.div.style.top = `${parent.div.offsetTop + row.offsetTop}px`;
        document.body.appendChild(row.div);
        const left = row.div.offsetLeft;
        const width = row.div.offsetWidth;
        const offset = getDocumentScrollOrigin(document);
        const b = document.body;
        const d = document.documentElement;
        const right = offset.x + (b.clientWidth || d.clientWidth);
        if (left + width > right) {
          row.div.style.left = `${Math.max(0, parent.div.offsetLeft - width - 6)}px`;
        }
        fit(row.div);
      }
    }
    /**
     * Adds a horizontal separator in the given parent item or the top-level menu
     * if no parent is specified.
     *
     * @param parent Optional item returned by <addItem>.
     * @param force Optional boolean to ignore <smartSeparators>. Default is false.
     */
    addSeparator(parent = null, force = false) {
      parent = parent || this;
      if (this.smartSeparators && !force) {
        parent.willAddSeparator = true;
      } else if (parent.tbody) {
        parent.willAddSeparator = false;
        const tr = document.createElement("tr");
        const col1 = document.createElement("td");
        col1.className = "mxPopupMenuIcon";
        col1.style.padding = "0 0 0 0px";
        tr.appendChild(col1);
        const col2 = document.createElement("td");
        col2.style.padding = "0 0 0 0px";
        col2.setAttribute("colSpan", "2");
        const hr = document.createElement("hr");
        hr.setAttribute("size", "1");
        col2.appendChild(hr);
        tr.appendChild(col2);
        parent.tbody.appendChild(tr);
      }
    }
    /**
     * Shows the popup menu for the given event and cell.
     *
     * Example:
     *
     * ```javascript
     * graph.getPlugin('PanningHandler').popup(x, y, cell, evt)
     * {
     *   mxUtils.alert('Hello, World!');
     * }
     * ```
     */
    popup(x, y, cell, evt) {
      if (this.div && this.tbody && this.factoryMethod) {
        this.div.style.left = `${x}px`;
        this.div.style.top = `${y}px`;
        while (this.tbody.firstChild) {
          InternalEvent_default.release(this.tbody.firstChild);
          this.tbody.removeChild(this.tbody.firstChild);
        }
        this.itemCount = 0;
        this.factoryMethod(this, cell, evt);
        if (this.itemCount > 0) {
          this.showMenu();
          this.fireEvent(new EventObject_default(InternalEvent_default.SHOW));
        }
      }
    }
    /**
     * Returns true if the menu is showing.
     */
    isMenuShowing() {
      return this.div && this.div.parentNode == document.body;
    }
    /**
     * Shows the menu.
     */
    showMenu() {
      document.body.appendChild(this.div);
      fit(this.div);
    }
    /**
     * Removes the menu and all submenus.
     */
    hideMenu() {
      var _a2;
      if (this.div) {
        (_a2 = this.div.parentNode) == null ? void 0 : _a2.removeChild(this.div);
        this.hideSubmenu(this);
        this.containsItems = false;
        this.fireEvent(new EventObject_default(InternalEvent_default.HIDE));
      }
    }
    /**
     * Removes all submenus inside the given parent.
     *
     * @param parent An item returned by <addItem>.
     */
    hideSubmenu(parent) {
      var _a2;
      if (parent.activeRow) {
        this.hideSubmenu(parent.activeRow);
        (_a2 = parent.activeRow.div.parentNode) == null ? void 0 : _a2.removeChild(parent.activeRow.div);
        parent.activeRow = null;
      }
    }
    /**
     * Destroys the handler and all its resources and DOM nodes.
     */
    destroy() {
      var _a2;
      if (this.div) {
        InternalEvent_default.release(this.div);
        (_a2 = this.div.parentNode) == null ? void 0 : _a2.removeChild(this.div);
      }
      super.destroy();
    }
  };
  var MaxPopupMenu_default = MaxPopupMenu;

  // node_modules/@maxgraph/core/lib/esm/view/plugin/PopupMenuHandler.js
  var PopupMenuHandler = class extends MaxPopupMenu_default {
    constructor(graph) {
      super();
      this.inTolerance = false;
      this.popupTrigger = false;
      this.selectOnPopup = true;
      this.clearSelectionOnBackground = true;
      this.triggerX = null;
      this.triggerY = null;
      this.screenX = null;
      this.screenY = null;
      this.graph = graph;
      this.graph.addMouseListener(this);
      this.gestureHandler = (sender, eo) => {
        this.inTolerance = false;
      };
      this.graph.addListener(InternalEvent_default.GESTURE, this.gestureHandler);
      this.init();
    }
    /**
     * Initializes the shapes required for this vertex handler.
     */
    init() {
      InternalEvent_default.addGestureListeners(this.div, (evt) => {
        const tooltipHandler = this.graph.getPlugin("TooltipHandler");
        tooltipHandler == null ? void 0 : tooltipHandler.hide();
      });
    }
    /**
     * Hook for returning if a cell should be selected for a given {@link MouseEvent}.
     * This implementation returns <selectOnPopup>.
     */
    isSelectOnPopup(me) {
      return this.selectOnPopup;
    }
    /**
     * Handles the event by initiating the panning. By consuming the event all
     * subsequent events of the gesture are redirected to this handler.
     */
    mouseDown(_sender, me) {
      if (this.isEnabled() && !isMultiTouchEvent(me.getEvent())) {
        this.hideMenu();
        this.triggerX = me.getGraphX();
        this.triggerY = me.getGraphY();
        this.screenX = getMainEvent(me.getEvent()).screenX;
        this.screenY = getMainEvent(me.getEvent()).screenY;
        this.popupTrigger = this.isPopupTrigger(me);
        this.inTolerance = true;
      }
    }
    /**
     * Handles the event by updating the panning on the graph.
     */
    mouseMove(_sender, me) {
      if (this.inTolerance && this.screenX != null && this.screenY != null) {
        if (Math.abs(getMainEvent(me.getEvent()).screenX - this.screenX) > this.graph.getEventTolerance() || Math.abs(getMainEvent(me.getEvent()).screenY - this.screenY) > this.graph.getEventTolerance()) {
          this.inTolerance = false;
        }
      }
    }
    /**
     * Handles the event by setting the translation on the view or showing the popupmenu.
     */
    mouseUp(_sender, me) {
      if (this.popupTrigger && this.inTolerance && this.triggerX != null && this.triggerY != null) {
        const cell = this.getCellForPopupEvent(me);
        if (this.graph.isEnabled() && this.isSelectOnPopup(me) && cell != null && !this.graph.isCellSelected(cell)) {
          this.graph.setSelectionCell(cell);
        } else if (this.clearSelectionOnBackground && cell == null) {
          this.graph.clearSelection();
        }
        const tooltipHandler = this.graph.getPlugin("TooltipHandler");
        tooltipHandler == null ? void 0 : tooltipHandler.hide();
        const origin = getScrollOrigin();
        this.popup(me.getX() + origin.x + 1, me.getY() + origin.y + 1, cell, me.getEvent());
        me.consume();
      }
      this.popupTrigger = false;
      this.inTolerance = false;
    }
    /**
     * Hook to return the cell for the mouse up popup trigger handling.
     */
    getCellForPopupEvent(me) {
      return me.getCell();
    }
    /**
     * Destroys the handler and all its resources and DOM nodes.
     */
    onDestroy() {
      this.graph.removeMouseListener(this);
      this.graph.removeListener(this.gestureHandler);
      super.destroy();
    }
  };
  PopupMenuHandler.pluginId = "PopupMenuHandler";
  var PopupMenuHandler_default = PopupMenuHandler;

  // node_modules/@maxgraph/core/lib/esm/view/plugin/ConnectionHandler.js
  var ConnectionHandler = class extends EventSource_default {
    /**
     * Constructs an event handler that connects vertices using the specified
     * factory method to create the new edges.
     *
     * @param graph Reference to the enclosing {@link AbstractGraph}.
     * @param factoryMethod Optional function to create the edge. The function takes
     * the source and target {@link Cell} as the first and second argument and an
     * optional cell style from the preview as the third argument. It returns
     * the {@link Cell} that represents the new edge.
     */
    constructor(graph, factoryMethod = null) {
      super();
      this.previous = null;
      this.iconState = null;
      this.icons = [];
      this.cell = null;
      this.currentPoint = null;
      this.sourceConstraint = null;
      this.shape = null;
      this.icon = null;
      this.originalPoint = null;
      this.currentState = null;
      this.selectedIcon = null;
      this.waypoints = [];
      this.factoryMethod = null;
      this.moveIconFront = false;
      this.moveIconBack = false;
      this.connectImage = null;
      this.targetConnectImage = false;
      this.enabled = false;
      this.select = true;
      this.createTarget = false;
      this.error = null;
      this.waypointsEnabled = false;
      this.ignoreMouseDown = false;
      this.first = null;
      this.connectIconOffset = new Point_default(0, TOOLTIP_VERTICAL_OFFSET);
      this.edgeState = null;
      this.mouseDownCounter = 0;
      this.movePreviewAway = false;
      this.outlineConnect = false;
      this.livePreview = false;
      this.cursor = null;
      this.cursorConnect = "pointer";
      this.insertBeforeSource = false;
      this.graph = graph;
      this.factoryMethod = factoryMethod;
      this.graph.addMouseListener(this);
      this.marker = this.createMarker();
      this.constraintHandler = this.createConstraintHandler();
      this.changeHandler = (sender) => {
        if (this.iconState) {
          this.iconState = this.graph.getView().getState(this.iconState.cell);
        }
        if (this.iconState) {
          this.redrawIcons(this.icons, this.iconState);
          this.constraintHandler.reset();
        } else if (this.previous && !this.graph.view.getState(this.previous.cell)) {
          this.reset();
        }
      };
      this.graph.getDataModel().addListener(InternalEvent_default.CHANGE, this.changeHandler);
      this.graph.getView().addListener(InternalEvent_default.SCALE, this.changeHandler);
      this.graph.getView().addListener(InternalEvent_default.TRANSLATE, this.changeHandler);
      this.graph.getView().addListener(InternalEvent_default.SCALE_AND_TRANSLATE, this.changeHandler);
      this.drillHandler = (sender) => {
        this.reset();
      };
      this.graph.addListener(InternalEvent_default.START_EDITING, this.drillHandler);
      this.graph.getView().addListener(InternalEvent_default.DOWN, this.drillHandler);
      this.graph.getView().addListener(InternalEvent_default.UP, this.drillHandler);
      this.escapeHandler = () => {
        this.reset();
      };
      this.graph.addListener(InternalEvent_default.ESCAPE, this.escapeHandler);
    }
    /**
     * Hook for subclasses to change the implementation of {@link ConstraintHandler} used here.
     * @since 0.21.0
     */
    createConstraintHandler() {
      return new ConstraintHandler_default(this.graph);
    }
    /**
     * Returns true if events are handled. This implementation
     * returns <enabled>.
     */
    isEnabled() {
      return this.enabled;
    }
    /**
     * Enables or disables event handling. This implementation
     * updates <enabled>.
     *
     * @param enabled Boolean that specifies the new enabled state.
     */
    setEnabled(enabled) {
      this.enabled = enabled;
    }
    /**
     * Returns <insertBeforeSource> for non-loops and false for loops.
     *
     * @param edge {@link Cell} that represents the edge to be inserted.
     * @param source {@link Cell} that represents the source terminal.
     * @param target {@link Cell} that represents the target terminal.
     * @param evt Mousedown event of the connect gesture.
     * @param dropTarget {@link Cell} that represents the cell under the mouse when it was
     * released.
     */
    isInsertBefore(edge, source, target, evt, dropTarget) {
      return this.insertBeforeSource && source !== target;
    }
    /**
     * Returns <createTarget>.
     *
     * @param evt Current active native pointer event.
     */
    isCreateTarget(evt) {
      return this.createTarget;
    }
    /**
     * Sets <createTarget>.
     */
    setCreateTarget(value) {
      this.createTarget = value;
    }
    /**
     * Creates the preview shape for new connections.
     */
    createShape() {
      const shape = this.livePreview && this.edgeState ? this.graph.cellRenderer.createShape(this.edgeState) : new PolylineShape_default([], INVALID_COLOR);
      if (shape && shape.node) {
        shape.dialect = "svg";
        shape.scale = this.graph.view.scale;
        shape.pointerEvents = false;
        shape.isDashed = true;
        shape.init(this.graph.getView().getOverlayPane());
        InternalEvent_default.redirectMouseEvents(shape.node, this.graph, null);
      }
      return shape;
    }
    /**
     * Returns true if the given cell is connectable. This is a hook to
     * disable floating connections. This implementation returns true.
     */
    isConnectableCell(cell) {
      return true;
    }
    /**
     * Creates and returns the {@link CellMarker} used in {@link marker}.
     */
    createMarker() {
      return new ConnectionHandlerCellMarker(this.graph, this);
    }
    /**
     * Starts a new connection for the given state and coordinates.
     */
    start(state, x, y, edgeState) {
      this.previous = state;
      this.first = new Point_default(x, y);
      this.edgeState = edgeState != null ? edgeState : this.createEdgeState();
      this.marker.currentColor = this.marker.validColor;
      this.marker.markedState = state;
      this.marker.mark();
      this.fireEvent(new EventObject_default(InternalEvent_default.START, { state: this.previous }));
    }
    /**
     * Returns true if the source terminal has been clicked and a new
     * connection is currently being previewed.
     */
    isConnecting() {
      return !!this.first && !!this.shape;
    }
    /**
     * Returns {@link AbstractGraph.isValidSource} for the given source terminal.
     *
     * @param cell {@link Cell} that represents the source terminal.
     * @param me {@link MouseEvent} that is associated with this call.
     */
    isValidSource(cell, me) {
      return this.graph.isValidSource(cell);
    }
    /**
     * Returns true. The call to {@link AbstractGraph.isValidTarget} is implicit by calling
     * {@link AbstractGraph.getEdgeValidationError} in <validateConnection>. This is an
     * additional hook for disabling certain targets in this specific handler.
     *
     * @param cell {@link Cell} that represents the target terminal.
     */
    isValidTarget(cell) {
      return true;
    }
    /**
     * Returns the error message or an empty string if the connection for the
     * given source target pair is not valid. Otherwise it returns null. This
     * implementation uses {@link AbstractGraph.getEdgeValidationError}.
     *
     * @param source {@link Cell} that represents the source terminal.
     * @param target {@link Cell} that represents the target terminal.
     */
    validateConnection(source, target) {
      if (!this.isValidTarget(target)) {
        return "";
      }
      return this.graph.getEdgeValidationError(null, source, target);
    }
    /**
     * Hook to return the {@link Image} used for the connection icon of the given
     * {@link CellState}. This implementation returns {@link connectImage}.
     *
     * @param state {@link CellState} whose connect image should be returned.
     */
    getConnectImage(state) {
      return this.connectImage;
    }
    /**
     * Returns true if the state has a HTML label in the graph's container, otherwise
     * it returns {@link oveIconFront}.
     *
     * @param state <CellState> whose connect icons should be returned.
     */
    isMoveIconToFrontForState(state) {
      if (state.text && state.text.node.parentNode === this.graph.container) {
        return true;
      }
      return this.moveIconFront;
    }
    /**
     * Creates the array {@link ImageShape}s that represent the connect icons for
     * the given {@link CellState}.
     *
     * @param state {@link CellState} whose connect icons should be returned.
     */
    createIcons(state) {
      const image = this.getConnectImage(state);
      if (image) {
        this.iconState = state;
        const icons = [];
        const bounds = new Rectangle_default(0, 0, image.width, image.height);
        const icon = new ImageShape_default(bounds, image.src, void 0, void 0, 0);
        icon.preserveImageAspect = false;
        if (this.isMoveIconToFrontForState(state)) {
          icon.dialect = "strictHtml";
          icon.init(this.graph.container);
        } else {
          icon.dialect = "svg";
          icon.init(this.graph.getView().getOverlayPane());
          if (this.moveIconBack && icon.node.parentNode && icon.node.previousSibling) {
            icon.node.parentNode.insertBefore(icon.node, icon.node.parentNode.firstChild);
          }
        }
        icon.node.style.cursor = this.cursorConnect;
        const getState = () => {
          var _a2;
          return (_a2 = this.currentState) != null ? _a2 : state;
        };
        const mouseDown = (evt) => {
          if (!isConsumed(evt)) {
            this.icon = icon;
            this.graph.fireMouseEvent(InternalEvent_default.MOUSE_DOWN, new InternalMouseEvent_default(evt, getState()));
          }
        };
        InternalEvent_default.redirectMouseEvents(icon.node, this.graph, getState, mouseDown);
        icons.push(icon);
        this.redrawIcons(icons, this.iconState);
        return icons;
      }
      return [];
    }
    /**
     * Redraws the given array of {@link ImageShape}s.
     *
     * @param icons Array of {@link ImageShape}s to be redrawn.
     * @param state {@link CellState} under the mouse.
     */
    redrawIcons(icons, state) {
      if (icons[0] && icons[0].bounds) {
        const pos = this.getIconPosition(icons[0], state);
        icons[0].bounds.x = pos.x;
        icons[0].bounds.y = pos.y;
        icons[0].redraw();
      }
    }
    /**
     * Returns the center position of the given icon.
     *
     * @param icon The connect icon of {@link ImageShape} with the mouse.
     * @param state {@link CellState} under the mouse.
     */
    getIconPosition(icon, state) {
      var _a2;
      const { scale } = this.graph.getView();
      let cx = state.getCenterX();
      let cy = state.getCenterY();
      if (this.graph.isSwimlane(state.cell)) {
        const size = this.graph.getStartSize(state.cell);
        cx = size.width !== 0 ? state.x + size.width * scale / 2 : cx;
        cy = size.height !== 0 ? state.y + size.height * scale / 2 : cy;
        const alpha = toRadians((_a2 = state.style.rotation) != null ? _a2 : 0);
        if (alpha !== 0) {
          const cos = Math.cos(alpha);
          const sin = Math.sin(alpha);
          const ct = new Point_default(state.getCenterX(), state.getCenterY());
          const pt = getRotatedPoint(new Point_default(cx, cy), cos, sin, ct);
          cx = pt.x;
          cy = pt.y;
        }
      }
      return new Point_default(cx - icon.bounds.width / 2, cy - icon.bounds.height / 2);
    }
    /**
     * Destroys the connect icons and resets the respective state.
     */
    destroyIcons() {
      for (let i = 0; i < this.icons.length; i += 1) {
        this.icons[i].destroy();
      }
      this.icons = [];
      this.icon = null;
      this.selectedIcon = null;
      this.iconState = null;
    }
    /**
     * Returns true if the given mouse down event should start this handler.
     * This implementation returns true if the event does not force marquee
     * selection, and the currentConstraint and currentFocus of the
     * {@link constraintHandler} are not null, or {@link previous} and {@link error} are not null and
     * {@link icons} is null or {@link icons} and {@link icon} are not `null`.
     */
    isStartEvent(me) {
      return this.constraintHandler.currentFocus !== null && this.constraintHandler.currentConstraint !== null || this.previous !== null && this.error === null && (this.icons.length === 0 || this.icon !== null);
    }
    /**
     * Handles the event by initiating a new connection.
     */
    mouseDown(_sender, me) {
      this.mouseDownCounter += 1;
      if (this.isEnabled() && this.graph.isEnabled() && !me.isConsumed() && !this.isConnecting() && this.isStartEvent(me)) {
        if (this.constraintHandler.currentConstraint && this.constraintHandler.currentFocus && this.constraintHandler.currentPoint) {
          this.sourceConstraint = this.constraintHandler.currentConstraint;
          this.previous = this.constraintHandler.currentFocus;
          this.first = this.constraintHandler.currentPoint.clone();
        } else {
          this.first = new Point_default(me.getGraphX(), me.getGraphY());
        }
        this.edgeState = this.createEdgeState(me);
        this.mouseDownCounter = 1;
        if (this.waypointsEnabled && !this.shape) {
          this.waypoints = [];
          this.shape = this.createShape();
          if (this.edgeState) {
            this.shape.apply(this.edgeState);
          }
        }
        if (!this.previous && this.edgeState && this.edgeState.cell.geometry) {
          const pt = this.graph.getPointForEvent(me.getEvent());
          this.edgeState.cell.geometry.setTerminalPoint(pt, true);
        }
        this.fireEvent(new EventObject_default(InternalEvent_default.START, { state: this.previous }));
        me.consume();
      }
      this.selectedIcon = this.icon;
      this.icon = null;
    }
    /**
     * Returns true if a tap on the given source state should immediately start
     * connecting. This implementation returns true if the state is not movable
     * in the graph.
     */
    isImmediateConnectSource(state) {
      return !this.graph.isCellMovable(state.cell);
    }
    /**
     * Hook to return an <CellState> which may be used during the preview.
     * This implementation returns null.
     *
     * Use the following code to create a preview for an existing edge style:
     *
     * ```javascript
     * graph.getPlugin('ConnectionHandler').createEdgeState(me)
     * {
     *   var edge = graph.createEdge(null, null, null, null, null, 'edgeStyle=elbowEdgeStyle');
     *
     *   return new CellState(this.graph.view, edge, this.graph.getCellStyle(edge));
     * };
     * ```
     */
    createEdgeState(me) {
      return null;
    }
    /**
     * Returns true if <outlineConnect> is true and the source of the event is the outline shape
     * or shift is pressed.
     */
    isOutlineConnectEvent(me) {
      if (!this.currentPoint)
        return false;
      const offset = getOffset(this.graph.container);
      const evt = me.getEvent();
      const clientX = getClientX(evt);
      const clientY = getClientY(evt);
      const doc = document.documentElement;
      const left = (window.pageXOffset || doc.scrollLeft) - (doc.clientLeft || 0);
      const top = (window.pageYOffset || doc.scrollTop) - (doc.clientTop || 0);
      const gridX = this.currentPoint.x - this.graph.container.scrollLeft + offset.x - left;
      const gridY = this.currentPoint.y - this.graph.container.scrollTop + offset.y - top;
      return this.outlineConnect && !isShiftDown(me.getEvent()) && (me.isSource(this.marker.highlight.shape) || isAltDown(me.getEvent()) && me.getState() != null || this.marker.highlight.isHighlightAt(clientX, clientY) || (gridX !== clientX || gridY !== clientY) && me.getState() == null && this.marker.highlight.isHighlightAt(gridX, gridY));
    }
    /**
     * Updates the current state for a given mouse move event by using the {@link marker}.
     */
    updateCurrentState(me, point) {
      this.constraintHandler.update(me, !this.first, false, !this.first || me.isSource(this.marker.highlight.shape) ? null : point);
      if (this.constraintHandler.currentFocus != null && this.constraintHandler.currentConstraint != null) {
        if (this.marker.highlight && this.marker.highlight.state && this.marker.highlight.state.cell === this.constraintHandler.currentFocus.cell && this.marker.highlight.shape) {
          if (this.marker.highlight.shape.stroke !== "transparent") {
            this.marker.highlight.shape.stroke = "transparent";
            this.marker.highlight.repaint();
          }
        } else {
          this.marker.markCell(this.constraintHandler.currentFocus.cell, "transparent");
        }
        if (this.previous) {
          this.error = this.validateConnection(this.previous.cell, this.constraintHandler.currentFocus.cell);
          if (!this.error) {
            this.currentState = this.constraintHandler.currentFocus;
          }
          if (this.error || this.currentState && !this.isCellEnabled(this.currentState.cell)) {
            this.constraintHandler.reset();
          }
        }
      } else {
        if (this.graph.isIgnoreTerminalEvent(me.getEvent())) {
          this.marker.reset();
          this.currentState = null;
        } else {
          this.marker.process(me);
          this.currentState = this.marker.getValidState();
        }
        if (this.currentState != null && !this.isCellEnabled(this.currentState.cell)) {
          this.constraintHandler.reset();
          this.marker.reset();
          this.currentState = null;
        }
        const outline = this.isOutlineConnectEvent(me);
        if (this.currentState != null && outline) {
          if (me.isSource(this.marker.highlight.shape)) {
            point = new Point_default(me.getGraphX(), me.getGraphY());
          }
          const constraint = this.graph.getOutlineConstraint(point, this.currentState, me);
          this.constraintHandler.setFocus(me, this.currentState, false);
          this.constraintHandler.currentConstraint = constraint;
          this.constraintHandler.currentPoint = point;
        }
        if (this.outlineConnect) {
          if (this.marker.highlight != null && this.marker.highlight.shape != null) {
            const s = this.graph.view.scale;
            if (this.constraintHandler.currentConstraint != null && this.constraintHandler.currentFocus != null) {
              this.marker.highlight.shape.stroke = OUTLINE_HIGHLIGHT_COLOR;
              this.marker.highlight.shape.strokeWidth = OUTLINE_HIGHLIGHT_STROKEWIDTH / s / s;
              this.marker.highlight.repaint();
            } else if (this.marker.hasValidState()) {
              const cell = me.getCell();
              if (cell && cell.isConnectable() && this.marker.getValidState() !== me.getState()) {
                this.marker.highlight.shape.stroke = "transparent";
                this.currentState = null;
              } else {
                this.marker.highlight.shape.stroke = VALID_COLOR;
              }
              this.marker.highlight.shape.strokeWidth = HIGHLIGHT_STROKEWIDTH / s / s;
              this.marker.highlight.repaint();
            }
          }
        }
      }
    }
    /**
     * Returns true if the given cell does not allow new connections to be created.
     */
    isCellEnabled(cell) {
      return true;
    }
    /**
     * Converts the given point from screen coordinates to model coordinates.
     */
    convertWaypoint(point) {
      const scale = this.graph.getView().getScale();
      const tr = this.graph.getView().getTranslate();
      point.x = point.x / scale - tr.x;
      point.y = point.y / scale - tr.y;
    }
    /**
     * Called to snap the given point to the current preview. This snaps to the
     * first point of the preview if alt is not pressed.
     */
    snapToPreview(me, point) {
      if (!isAltDown(me.getEvent()) && this.previous) {
        const tol = this.graph.getGridSize() * this.graph.view.scale / 2;
        const tmp = this.sourceConstraint && this.first ? this.first : new Point_default(this.previous.getCenterX(), this.previous.getCenterY());
        if (Math.abs(tmp.x - me.getGraphX()) < tol) {
          point.x = tmp.x;
        }
        if (Math.abs(tmp.y - me.getGraphY()) < tol) {
          point.y = tmp.y;
        }
      }
    }
    /**
     * Handles the event by updating the preview edge or by highlighting
     * a possible source or target terminal.
     */
    mouseMove(_sender, me) {
      if (!me.isConsumed() && (this.ignoreMouseDown || this.first || !this.graph.isMouseDown)) {
        if (!this.isEnabled() && this.currentState) {
          this.destroyIcons();
          this.currentState = null;
        }
        const view = this.graph.getView();
        const { scale } = view;
        const tr = view.translate;
        let point = new Point_default(me.getGraphX(), me.getGraphY());
        this.error = null;
        if (this.graph.isGridEnabledEvent(me.getEvent())) {
          point = new Point_default((this.graph.snap(point.x / scale - tr.x) + tr.x) * scale, (this.graph.snap(point.y / scale - tr.y) + tr.y) * scale);
        }
        this.snapToPreview(me, point);
        this.currentPoint = point;
        if ((this.first || this.isEnabled() && this.graph.isEnabled()) && (this.shape || !this.first || Math.abs(me.getGraphX() - this.first.x) > this.graph.getEventTolerance() || Math.abs(me.getGraphY() - this.first.y) > this.graph.getEventTolerance())) {
          this.updateCurrentState(me, point);
        }
        if (this.first) {
          let constraint = null;
          let current = point;
          if (this.constraintHandler.currentConstraint && this.constraintHandler.currentFocus && this.constraintHandler.currentPoint) {
            constraint = this.constraintHandler.currentConstraint;
            current = this.constraintHandler.currentPoint.clone();
          } else if (this.previous && !this.graph.isIgnoreTerminalEvent(me.getEvent()) && isShiftDown(me.getEvent())) {
            if (Math.abs(this.previous.getCenterX() - point.x) < Math.abs(this.previous.getCenterY() - point.y)) {
              point.x = this.previous.getCenterX();
            } else {
              point.y = this.previous.getCenterY();
            }
          }
          let pt2 = this.first;
          if (this.selectedIcon && this.selectedIcon.bounds) {
            const w = this.selectedIcon.bounds.width;
            const h = this.selectedIcon.bounds.height;
            if (this.currentState && this.targetConnectImage) {
              const pos = this.getIconPosition(this.selectedIcon, this.currentState);
              this.selectedIcon.bounds.x = pos.x;
              this.selectedIcon.bounds.y = pos.y;
            } else {
              const bounds = new Rectangle_default(me.getGraphX() + this.connectIconOffset.x, me.getGraphY() + this.connectIconOffset.y, w, h);
              this.selectedIcon.bounds = bounds;
            }
            this.selectedIcon.redraw();
          }
          if (this.edgeState) {
            this.updateEdgeState(current, constraint);
            current = this.edgeState.absolutePoints[this.edgeState.absolutePoints.length - 1];
            pt2 = this.edgeState.absolutePoints[0];
          } else {
            if (this.currentState) {
              if (!this.constraintHandler.currentConstraint) {
                const tmp = this.getTargetPerimeterPoint(this.currentState, me);
                if (tmp != null) {
                  current = tmp;
                }
              }
            }
            if (!this.sourceConstraint && this.previous) {
              const next = this.waypoints.length > 0 ? this.waypoints[0] : current;
              const tmp = this.getSourcePerimeterPoint(this.previous, next, me);
              if (tmp) {
                pt2 = tmp;
              }
            }
          }
          if (!this.currentState && this.movePreviewAway && current) {
            let tmp = pt2;
            if (this.edgeState && this.edgeState.absolutePoints.length >= 2) {
              const tmp2 = this.edgeState.absolutePoints[this.edgeState.absolutePoints.length - 2];
              if (tmp2) {
                tmp = tmp2;
              }
            }
            if (tmp) {
              const dx = current.x - tmp.x;
              const dy = current.y - tmp.y;
              const len = Math.sqrt(dx * dx + dy * dy);
              if (len === 0) {
                return;
              }
              this.originalPoint = current.clone();
              current.x -= dx * 4 / len;
              current.y -= dy * 4 / len;
            }
          } else {
            this.originalPoint = null;
          }
          if (!this.shape) {
            const dx = Math.abs(me.getGraphX() - this.first.x);
            const dy = Math.abs(me.getGraphY() - this.first.y);
            if (dx > this.graph.getEventTolerance() || dy > this.graph.getEventTolerance()) {
              this.shape = this.createShape();
              if (this.edgeState) {
                this.shape.apply(this.edgeState);
              }
              this.updateCurrentState(me, point);
            }
          }
          if (this.shape) {
            if (this.edgeState) {
              this.shape.points = this.edgeState.absolutePoints;
            } else {
              let pts = [pt2];
              if (this.waypoints.length > 0) {
                pts = pts.concat(this.waypoints);
              }
              pts.push(current);
              this.shape.points = pts;
            }
            this.drawPreview();
          }
          if (this.cursor) {
            this.graph.container.style.cursor = this.cursor;
          }
          InternalEvent_default.consume(me.getEvent());
          me.consume();
        } else if (!this.isEnabled() || !this.graph.isEnabled()) {
          this.constraintHandler.reset();
        } else if (this.previous !== this.currentState && !this.edgeState) {
          this.destroyIcons();
          if (this.currentState && !this.error && !this.constraintHandler.currentConstraint) {
            this.icons = this.createIcons(this.currentState);
            if (this.icons.length === 0) {
              this.currentState.setCursor(this.cursorConnect);
              me.consume();
            }
          }
          this.previous = this.currentState;
        } else if (this.previous === this.currentState && this.currentState != null && this.icons.length === 0 && !this.graph.isMouseDown) {
          me.consume();
        }
        if (!this.graph.isMouseDown && this.currentState != null && this.icons != null) {
          let hitsIcon = false;
          const target = me.getSource();
          for (let i = 0; i < this.icons.length && !hitsIcon; i += 1) {
            hitsIcon = target === this.icons[i].node || // @ts-ignore parentNode should exist.
            !!target && target.parentNode === this.icons[i].node;
          }
          if (!hitsIcon) {
            this.updateIcons(this.currentState, this.icons, me);
          }
        }
      } else {
        this.constraintHandler.reset();
      }
    }
    /**
     * Updates {@link edgeState}.
     */
    updateEdgeState(current, constraint) {
      var _a2;
      if (!this.edgeState)
        return;
      if ((_a2 = this.sourceConstraint) == null ? void 0 : _a2.point) {
        this.edgeState.style.exitX = this.sourceConstraint.point.x;
        this.edgeState.style.exitY = this.sourceConstraint.point.y;
      }
      if (constraint == null ? void 0 : constraint.point) {
        this.edgeState.style.entryX = constraint.point.x;
        this.edgeState.style.entryY = constraint.point.y;
      } else {
        delete this.edgeState.style.entryX;
        delete this.edgeState.style.entryY;
      }
      this.edgeState.absolutePoints = [null, this.currentState != null ? null : current];
      if (this.sourceConstraint) {
        this.graph.view.updateFixedTerminalPoint(this.edgeState, this.previous, true, this.sourceConstraint);
      }
      if (this.currentState != null) {
        if (constraint == null) {
          constraint = this.graph.getConnectionConstraint(this.edgeState, this.previous, false);
        }
        this.edgeState.setAbsoluteTerminalPoint(null, false);
        this.graph.view.updateFixedTerminalPoint(this.edgeState, this.currentState, false, constraint);
      }
      const realPoints = [];
      for (let i = 0; i < this.waypoints.length; i += 1) {
        const pt = this.waypoints[i].clone();
        this.convertWaypoint(pt);
        realPoints[i] = pt;
      }
      this.graph.view.updatePoints(this.edgeState, realPoints, this.previous, this.currentState);
      this.graph.view.updateFloatingTerminalPoints(this.edgeState, this.previous, this.currentState);
    }
    /**
     * Returns the perimeter point for the given target state.
     *
     * @param state <CellState> that represents the target cell state.
     * @param _me {@link MouseEvent} that represents the mouse move.
     */
    getTargetPerimeterPoint(state, _me) {
      let result = null;
      const { view } = state;
      const targetPerimeter = view.getPerimeterFunction(state);
      if (targetPerimeter && this.previous && this.edgeState) {
        const next = this.waypoints.length > 0 ? this.waypoints[this.waypoints.length - 1] : new Point_default(this.previous.getCenterX(), this.previous.getCenterY());
        const tmp = targetPerimeter(view.getPerimeterBounds(state), this.edgeState, next, false);
        if (tmp) {
          result = tmp;
        }
      } else {
        result = new Point_default(state.getCenterX(), state.getCenterY());
      }
      return result;
    }
    /**
     * Hook to update the icon position(s) based on a mouseOver event. This is
     * an empty implementation.
     *
     * @param state <CellState> that represents the target cell state.
     * @param next {@link Point} that represents the next point along the previewed edge.
     * @param me {@link MouseEvent} that represents the mouse move.
     */
    getSourcePerimeterPoint(state, next, me) {
      var _a2;
      let result = null;
      const { view } = state;
      const sourcePerimeter = view.getPerimeterFunction(state);
      const c = new Point_default(state.getCenterX(), state.getCenterY());
      if (sourcePerimeter) {
        const theta = (_a2 = state.style.rotation) != null ? _a2 : 0;
        const rad = -theta * (Math.PI / 180);
        if (theta !== 0) {
          next = getRotatedPoint(new Point_default(next.x, next.y), Math.cos(rad), Math.sin(rad), c);
        }
        let tmp = sourcePerimeter(view.getPerimeterBounds(state), state, next, false);
        if (tmp) {
          if (theta !== 0) {
            tmp = getRotatedPoint(new Point_default(tmp.x, tmp.y), Math.cos(-rad), Math.sin(-rad), c);
          }
          result = tmp;
        }
      } else {
        result = c;
      }
      return result;
    }
    /**
     * Hook to update the icon position(s) based on a mouseOver event.
     *
     * This is an empty implementation.
     *
     * @param state {@link CellState} under the mouse.
     * @param icons Array of currently displayed icons.
     * @param me {@link MouseEvent} that contains the mouse event.
     */
    updateIcons(state, icons, me) {
    }
    /**
     * Returns `true` if the given mouse up event should stop this handler.
     *
     * The connection will be created if {@link error} is `null`.
     * Note that this is only called if {@link waypointsEnabled} is `true`.
     *
     * This implementation returns `true` if there is a cell state in the given event.
     */
    isStopEvent(me) {
      return !!me.getState();
    }
    /**
     * Adds the waypoint for the given event to {@link waypoints}.
     */
    addWaypointForEvent(me) {
      if (!this.first)
        return;
      let point = convertPoint(this.graph.container, me.getX(), me.getY());
      const dx = Math.abs(point.x - this.first.x);
      const dy = Math.abs(point.y - this.first.y);
      const addPoint = this.waypoints.length > 0 || this.mouseDownCounter > 1 && (dx > this.graph.getEventTolerance() || dy > this.graph.getEventTolerance());
      if (addPoint) {
        const { scale } = this.graph.view;
        point = new Point_default(this.graph.snap(me.getGraphX() / scale) * scale, this.graph.snap(me.getGraphY() / scale) * scale);
        this.waypoints.push(point);
      }
    }
    /**
     * Returns `true` if the connection for the given constraints is valid.
     *
     * This implementation returns `true` if the constraints are not pointing to the same fixed connection point.
     */
    checkConstraints(c1, c2) {
      return !c1 || !c2 || !c1.point || !c2.point || !c1.point.equals(c2.point) || c1.dx !== c2.dx || c1.dy !== c2.dy || c1.perimeter !== c2.perimeter;
    }
    /**
     * Handles the event by inserting the new connection.
     */
    mouseUp(_sender, me) {
      if (!me.isConsumed() && this.isConnecting()) {
        if (this.waypointsEnabled && !this.isStopEvent(me)) {
          this.addWaypointForEvent(me);
          me.consume();
          return;
        }
        const c1 = this.sourceConstraint;
        const c2 = this.constraintHandler.currentConstraint;
        const source = this.previous ? this.previous.cell : null;
        let target = null;
        if (this.constraintHandler.currentConstraint && this.constraintHandler.currentFocus) {
          target = this.constraintHandler.currentFocus.cell;
        }
        if (!target && this.currentState) {
          target = this.currentState.cell;
        }
        if (!this.error && (!source || !target || source !== target || this.checkConstraints(c1, c2))) {
          this.connect(source, target, me.getEvent(), me.getCell());
        } else {
          if (this.previous != null && this.marker.validState != null && this.previous.cell === this.marker.validState.cell) {
            this.graph.selectCellForEvent(this.marker.validState.cell, me.getEvent());
          }
          if (this.error != null && this.error.length > 0) {
            this.graph.validationAlert(this.error);
          }
        }
        this.destroyIcons();
        me.consume();
      }
      if (this.first != null) {
        this.reset();
      }
    }
    /**
     * Resets the state of this handler.
     */
    reset() {
      if (this.shape != null) {
        this.shape.destroy();
        this.shape = null;
      }
      if (this.cursor != null && this.graph.container != null) {
        this.graph.container.style.cursor = "";
      }
      this.destroyIcons();
      this.marker.reset();
      this.constraintHandler.reset();
      this.originalPoint = null;
      this.currentPoint = null;
      this.edgeState = null;
      this.previous = null;
      this.error = null;
      this.sourceConstraint = null;
      this.mouseDownCounter = 0;
      this.first = null;
      this.fireEvent(new EventObject_default(InternalEvent_default.RESET));
    }
    /**
     * Redraws the preview edge using the color and width returned by {@link getEdgeColor} and {@link getEdgeWidth}.
     */
    drawPreview() {
      this.updatePreview(this.error === null);
      if (this.shape)
        this.shape.redraw();
    }
    /**
     * Returns the color used to draw the preview edge.
     * This returns green if there is no edge validation error and red otherwise.
     *
     * @param valid Boolean indicating if the color for a valid edge should be returned.
     */
    updatePreview(valid) {
      if (this.shape) {
        this.shape.strokeWidth = this.getEdgeWidth(valid);
        this.shape.stroke = this.getEdgeColor(valid);
      }
    }
    /**
     * Returns the color used to draw the preview edge.
     *
     * This returns green if there is no edge validation error and red otherwise.
     *
     * @param valid Boolean indicating if the color for a valid edge should be returned.
     */
    getEdgeColor(valid) {
      return valid ? VALID_COLOR : INVALID_COLOR;
    }
    /**
     * Returns the width used to draw the preview edge.
     *
     * This returns `3` if there is no edge validation error and `1` otherwise.
     *
     * @param valid Boolean indicating if the width for a valid edge should be returned.
     */
    getEdgeWidth(valid) {
      return valid ? 3 : 1;
    }
    /**
     * Connects the given source and target using a new edge.
     *
     * This implementation uses {@link createEdge} to create the edge.
     *
     * @param source {@link Cell} that represents the source terminal.
     * @param target {@link Cell} that represents the target terminal.
     * @param evt {@link MouseEvent} event of the connect gesture.
     * @param dropTarget {@link Cell} that represents the cell under the mouse when it was
     * released.
     */
    connect(source, target, evt, dropTarget = null) {
      var _a2, _b, _c, _d, _e;
      if (target || this.isCreateTarget(evt) || this.graph.isAllowDanglingEdges()) {
        const model = this.graph.getDataModel();
        let terminalInserted = false;
        let edge = null;
        model.beginUpdate();
        try {
          if (source && !target && !this.graph.isIgnoreTerminalEvent(evt) && this.isCreateTarget(evt)) {
            target = this.createTargetVertex(evt, source);
            if (target) {
              dropTarget = this.graph.getDropTarget([target], evt, dropTarget);
              terminalInserted = true;
              if (dropTarget == null || !dropTarget.isEdge()) {
                const pstate = dropTarget ? this.graph.getView().getState(dropTarget) : null;
                if (pstate) {
                  const tmp = target.getGeometry();
                  if (tmp) {
                    tmp.x -= pstate.origin.x;
                    tmp.y -= pstate.origin.y;
                  }
                }
              } else {
                dropTarget = this.graph.getDefaultParent();
              }
              this.graph.addCell(target, dropTarget);
            }
          }
          let parent = this.graph.getDefaultParent();
          if (source && target && source.getParent() === target.getParent() && ((_a2 = source.getParent()) == null ? void 0 : _a2.getParent()) !== model.getRoot()) {
            parent = source.getParent();
            if (source.geometry && source.geometry.relative && target.geometry && target.geometry.relative) {
              parent = parent.getParent();
            }
          }
          let value = null;
          let style = {};
          if ((_b = this.edgeState) == null ? void 0 : _b.cell) {
            value = this.edgeState.cell.value;
            style = (_c = this.edgeState.cell.style) != null ? _c : {};
          }
          edge = this.insertEdge(parent, "", value, source, target, style);
          if (edge && source) {
            this.graph.setConnectionConstraint(edge, source, true, this.sourceConstraint);
            this.graph.setConnectionConstraint(edge, target, false, this.constraintHandler.currentConstraint);
            if ((_e = (_d = this.edgeState) == null ? void 0 : _d.cell) == null ? void 0 : _e.geometry) {
              model.setGeometry(edge, this.edgeState.cell.geometry);
            }
            parent = source.getParent();
            if (this.isInsertBefore(edge, source, target, evt, dropTarget)) {
              const index = null;
              let tmp = source;
              while (tmp && tmp.parent != null && tmp.geometry != null && tmp.geometry.relative && tmp.parent !== edge.parent) {
                tmp = tmp.getParent();
              }
              if (tmp != null && tmp.parent != null && tmp.parent === edge.parent) {
                model.add(parent, edge, tmp.parent.getIndex(tmp));
              }
            }
            let geo = edge.getGeometry();
            if (geo == null) {
              geo = new Geometry_default();
              geo.relative = true;
              model.setGeometry(edge, geo);
            }
            if (this.waypoints.length > 0) {
              const s = this.graph.view.scale;
              const tr = this.graph.view.translate;
              geo.points = [];
              for (let i = 0; i < this.waypoints.length; i += 1) {
                const pt = this.waypoints[i];
                geo.points.push(new Point_default(pt.x / s - tr.x, pt.y / s - tr.y));
              }
            }
            if (!target && this.currentPoint) {
              const t = this.graph.view.translate;
              const s = this.graph.view.scale;
              const pt = this.originalPoint != null ? new Point_default(this.originalPoint.x / s - t.x, this.originalPoint.y / s - t.y) : new Point_default(this.currentPoint.x / s - t.x, this.currentPoint.y / s - t.y);
              pt.x -= this.graph.getPanDx() / this.graph.view.scale;
              pt.y -= this.graph.getPanDy() / this.graph.view.scale;
              geo.setTerminalPoint(pt, false);
            }
            this.fireEvent(new EventObject_default(InternalEvent_default.CONNECT, "cell", edge, "terminal", target, "event", evt, "target", dropTarget, "terminalInserted", terminalInserted));
          }
        } catch (e) {
          log().show();
          const errorMessage = `Error in ConnectionHandler: ${e instanceof Error ? e.message + "\n" + e.stack : "unknown cause"}`;
          log().debug(errorMessage);
        } finally {
          model.endUpdate();
        }
        if (this.select) {
          this.selectCells(edge, terminalInserted ? target : null);
        }
      }
    }
    /**
     * Selects the given edge after adding a new connection. The target argument
     * contains the target vertex if one has been inserted.
     */
    selectCells(edge, target) {
      this.graph.setSelectionCell(edge);
    }
    /**
     * Creates, inserts and returns the new edge for the given parameters. This
     * implementation does only use {@link createEdge} if {@link factoryMethod} is defined,
     * otherwise {@link AbstractGraph.insertEdge} will be used.
     */
    insertEdge(parent, id, value, source, target, style) {
      if (!this.factoryMethod) {
        return this.graph.insertEdge({ parent, id, value, source, target, style });
      }
      let edge = this.createEdge(value, source, target, style);
      edge = this.graph.addEdge(edge, parent, source, target);
      return edge;
    }
    /**
     * Hook method for creating new vertices on the fly if no target was
     * under the mouse. This is only called if <createTarget> is true and
     * returns null.
     *
     * @param evt Mousedown event of the connect gesture.
     * @param source {@link Cell} that represents the source terminal.
     */
    createTargetVertex(evt, source) {
      let geo = source.getGeometry();
      while (geo && geo.relative) {
        source = source.getParent();
        geo = source.getGeometry();
      }
      const clone2 = this.graph.cloneCell(source);
      geo = clone2.getGeometry();
      if (geo && this.currentPoint) {
        const t = this.graph.view.translate;
        const s = this.graph.view.scale;
        const point = new Point_default(this.currentPoint.x / s - t.x, this.currentPoint.y / s - t.y);
        geo.x = Math.round(point.x - geo.width / 2 - this.graph.getPanDx() / s);
        geo.y = Math.round(point.y - geo.height / 2 - this.graph.getPanDy() / s);
        const tol = this.getAlignmentTolerance();
        if (tol > 0) {
          const sourceState = this.graph.view.getState(source);
          if (sourceState != null) {
            const x = sourceState.x / s - t.x;
            const y = sourceState.y / s - t.y;
            if (Math.abs(x - geo.x) <= tol) {
              geo.x = Math.round(x);
            }
            if (Math.abs(y - geo.y) <= tol) {
              geo.y = Math.round(y);
            }
          }
        }
      }
      return clone2;
    }
    /**
     * Returns the tolerance for aligning new targets to sources. This returns the grid size / 2.
     */
    getAlignmentTolerance(evt) {
      return this.graph.isGridEnabled() ? this.graph.getGridSize() / 2 : this.graph.getSnapTolerance();
    }
    /**
     * Creates and returns a new edge using {@link factoryMethod} if one exists. If
     * no factory method is defined, then a new default edge is returned. The
     * source and target arguments are informal, the actual connection is
     * set up later by the caller of this function.
     *
     * @param value Value to be used for creating the edge.
     * @param source {@link Cell} that represents the source terminal.
     * @param target {@link Cell} that represents the target terminal.
     * @param style Optional style from the preview edge.
     */
    createEdge(value, source, target, style = {}) {
      let edge = null;
      if (this.factoryMethod != null) {
        edge = this.factoryMethod(source, target, style);
      }
      if (edge == null) {
        edge = new Cell_default(value || "");
        edge.setEdge(true);
        edge.setStyle(style);
        const geo = new Geometry_default();
        geo.relative = true;
        edge.setGeometry(geo);
      }
      return edge;
    }
    /**
     * Destroys the handler and all its resources and DOM nodes. This should be
     * called on all instances. It is called automatically for the built-in
     * instance created for each {@link AbstractGraph}.
     */
    onDestroy() {
      this.graph.removeMouseListener(this);
      if (this.shape) {
        this.shape.destroy();
        this.shape = null;
      }
      if (this.marker) {
        this.marker.destroy();
        this.marker = null;
      }
      if (this.constraintHandler) {
        this.constraintHandler.onDestroy();
      }
      if (this.changeHandler) {
        this.graph.getDataModel().removeListener(this.changeHandler);
        this.graph.getView().removeListener(this.changeHandler);
      }
      if (this.drillHandler) {
        this.graph.removeListener(this.drillHandler);
        this.graph.getView().removeListener(this.drillHandler);
      }
      if (this.escapeHandler) {
        this.graph.removeListener(this.escapeHandler);
      }
    }
  };
  ConnectionHandler.pluginId = "ConnectionHandler";
  var ConnectionHandler_default = ConnectionHandler;
  var ConnectionHandlerCellMarker = class extends CellMarker_default {
    constructor(graph, connectionHandler, validColor = VALID_COLOR, invalidColor = DEFAULT_INVALID_COLOR, hotspot = DEFAULT_HOTSPOT) {
      super(graph, validColor, invalidColor, hotspot);
      this.hotspotEnabled = true;
      this.connectionHandler = connectionHandler;
    }
    // Overrides to return cell at location only if valid (so that
    // there is no highlight for invalid cells)
    getCell(me) {
      let cell = super.getCell(me);
      this.connectionHandler.error = null;
      if (!cell && this.connectionHandler.currentPoint) {
        cell = this.connectionHandler.graph.getCellAt(this.connectionHandler.currentPoint.x, this.connectionHandler.currentPoint.y);
      }
      if (cell && !cell.isConnectable() && this.connectionHandler.cell) {
        const parent = this.connectionHandler.cell.getParent();
        if (parent && parent.isVertex() && parent.isConnectable()) {
          cell = parent;
        }
      }
      if (cell) {
        if (this.connectionHandler.graph.isSwimlane(cell) && this.connectionHandler.currentPoint != null && this.connectionHandler.graph.hitsSwimlaneContent(cell, this.connectionHandler.currentPoint.x, this.connectionHandler.currentPoint.y) || !this.connectionHandler.isConnectableCell(cell)) {
          cell = null;
        }
      }
      if (cell) {
        if (this.connectionHandler.isConnecting()) {
          if (this.connectionHandler.previous) {
            this.connectionHandler.error = this.connectionHandler.validateConnection(this.connectionHandler.previous.cell, cell);
            if (this.connectionHandler.error !== null && this.connectionHandler.error.length === 0) {
              cell = null;
              if (this.connectionHandler.isCreateTarget(me.getEvent())) {
                this.connectionHandler.error = null;
              }
            }
          }
        } else if (!this.connectionHandler.isValidSource(cell, me)) {
          cell = null;
        }
      } else if (this.connectionHandler.isConnecting() && !this.connectionHandler.isCreateTarget(me.getEvent()) && !this.connectionHandler.graph.isAllowDanglingEdges()) {
        this.connectionHandler.error = "";
      }
      return cell;
    }
    // Sets the highlight color according to validateConnection
    isValidState(state) {
      if (this.connectionHandler.isConnecting()) {
        return !this.connectionHandler.error;
      }
      return super.isValidState(state);
    }
    // Overrides to use marker color only in highlight mode or for
    // target selection
    getMarkerColor(evt, state, isValid) {
      return !this.connectionHandler.connectImage || this.connectionHandler.isConnecting() ? super.getMarkerColor(evt, state, isValid) : NONE;
    }
    // Overrides to use hotspot only for source selection otherwise
    // intersects always returns true when over a cell
    intersects(state, evt) {
      if (this.connectionHandler.connectImage || this.connectionHandler.isConnecting()) {
        return true;
      }
      return super.intersects(state, evt);
    }
  };

  // node_modules/@maxgraph/core/lib/esm/view/other/Guide.js
  var Guide = class {
    constructor(graph, states) {
      this.states = [];
      this.horizontal = true;
      this.vertical = true;
      this.guideX = null;
      this.guideY = null;
      this.rounded = false;
      this.tolerance = 2;
      this.graph = graph;
      this.setStates(states);
    }
    /**
     * Sets the {@link CellState}s that should be used for alignment.
     */
    setStates(states) {
      this.states = states;
    }
    /**
     * Returns true if the guide should be enabled for the given native event. This
     * implementation always returns true.
     */
    isEnabledForEvent(evt) {
      return true;
    }
    /**
     * Returns the tolerance for the guides. Default value is gridSize / 2.
     */
    getGuideTolerance(gridEnabled = false) {
      return gridEnabled && this.graph.isGridEnabled() ? this.graph.getGridSize() / 2 : this.tolerance;
    }
    /**
     * Returns the mxShape to be used for painting the respective guide. This
     * implementation returns a new, dashed and crisp {@link PolylineShape} using
     * {@link GUIDE_COLOR} and {@link GUIDE_STROKEWIDTH} as the format.
     *
     * @param horizontal Boolean that specifies which guide should be created.
     */
    createGuideShape(horizontal = false) {
      const guide = new PolylineShape_default([], GUIDE_COLOR, GUIDE_STROKEWIDTH);
      guide.isDashed = true;
      return guide;
    }
    /**
     * Returns true if the given state should be ignored.
     * @param state
     */
    isStateIgnored(state) {
      return false;
    }
    /**
     * Moves the <bounds> by the given {@link Point} and returnt the snapped point.
     */
    move(bounds = null, delta, gridEnabled = false, clone2 = false) {
      if ((this.horizontal || this.vertical) && bounds) {
        const { scale } = this.graph.getView();
        const tt = this.getGuideTolerance(gridEnabled) * scale;
        const b = bounds.clone();
        b.x += delta.x;
        b.y += delta.y;
        let overrideX = false;
        let stateX = null;
        let valueX = null;
        let overrideY = false;
        let stateY = null;
        let valueY = null;
        let ttX = tt;
        let ttY = tt;
        const left = b.x;
        const right = b.x + b.width;
        const center = b.getCenterX();
        const top = b.y;
        const bottom = b.y + b.height;
        const middle = b.getCenterY();
        const snapX = (x, state, centerAlign) => {
          let override = false;
          if (centerAlign && Math.abs(x - center) < ttX) {
            delta.x = x - bounds.getCenterX();
            ttX = Math.abs(x - center);
            override = true;
          } else if (!centerAlign) {
            if (Math.abs(x - left) < ttX) {
              delta.x = x - bounds.x;
              ttX = Math.abs(x - left);
              override = true;
            } else if (Math.abs(x - right) < ttX) {
              delta.x = x - bounds.x - bounds.width;
              ttX = Math.abs(x - right);
              override = true;
            }
          }
          if (override) {
            stateX = state;
            valueX = x;
            if (!this.guideX) {
              this.guideX = this.createGuideShape(true);
              this.guideX.dialect = "svg";
              this.guideX.pointerEvents = false;
              this.guideX.init(this.graph.getView().getOverlayPane());
            }
          }
          overrideX = overrideX || override;
        };
        const snapY = (y, state, centerAlign) => {
          let override = false;
          if (centerAlign && Math.abs(y - middle) < ttY) {
            delta.y = y - bounds.getCenterY();
            ttY = Math.abs(y - middle);
            override = true;
          } else if (!centerAlign) {
            if (Math.abs(y - top) < ttY) {
              delta.y = y - bounds.y;
              ttY = Math.abs(y - top);
              override = true;
            } else if (Math.abs(y - bottom) < ttY) {
              delta.y = y - bounds.y - bounds.height;
              ttY = Math.abs(y - bottom);
              override = true;
            }
          }
          if (override) {
            stateY = state;
            valueY = y;
            if (!this.guideY) {
              this.guideY = this.createGuideShape(false);
              this.guideY.dialect = "svg";
              this.guideY.pointerEvents = false;
              this.guideY.init(this.graph.getView().getOverlayPane());
            }
          }
          overrideY = overrideY || override;
        };
        for (let i = 0; i < this.states.length; i += 1) {
          const state = this.states[i];
          if (state && !this.isStateIgnored(state)) {
            if (this.horizontal) {
              snapX(state.getCenterX(), state, true);
              snapX(state.x, state, false);
              snapX(state.x + state.width, state, false);
              if (!state.cell) {
                snapX(state.getCenterX(), state, false);
              }
            }
            if (this.vertical) {
              snapY(state.getCenterY(), state, true);
              snapY(state.y, state, false);
              snapY(state.y + state.height, state, false);
              if (!state.cell) {
                snapY(state.getCenterY(), state, false);
              }
            }
          }
        }
        this.graph.snapDelta(delta, bounds, !gridEnabled, overrideX, overrideY);
        delta = this.getDelta(bounds, stateX, delta.x, stateY, delta.y);
        const c = this.graph.container;
        if (!overrideX && this.guideX) {
          this.guideX.node.style.visibility = "hidden";
        } else if (this.guideX) {
          let minY = null;
          let maxY = null;
          if (stateX) {
            minY = Math.min(bounds.y + delta.y - this.graph.getPanDy(), stateX.y);
            maxY = Math.max(
              bounds.y + bounds.height + delta.y - this.graph.getPanDy(),
              // @ts-ignore stateX! doesn't work for some reason...
              stateX.y + stateX.height
            );
          }
          if (minY !== null && maxY !== null) {
            this.guideX.points = [new Point_default(valueX, minY), new Point_default(valueX, maxY)];
          } else {
            this.guideX.points = [
              new Point_default(valueX, -this.graph.getPanDy()),
              new Point_default(valueX, c.scrollHeight - 3 - this.graph.getPanDy())
            ];
          }
          this.guideX.stroke = this.getGuideColor(stateX, true);
          this.guideX.node.style.visibility = "visible";
          this.guideX.redraw();
        }
        if (!overrideY && this.guideY != null) {
          this.guideY.node.style.visibility = "hidden";
        } else if (this.guideY != null) {
          let minX = null;
          let maxX = null;
          if (stateY != null && bounds != null) {
            minX = Math.min(bounds.x + delta.x - this.graph.getPanDx(), stateY.x);
            maxX = Math.max(
              bounds.x + bounds.width + delta.x - this.graph.getPanDx(),
              // @ts-ignore
              stateY.x + stateY.width
            );
          }
          if (minX != null && maxX != null && valueY !== null) {
            this.guideY.points = [new Point_default(minX, valueY), new Point_default(maxX, valueY)];
          } else if (valueY !== null) {
            this.guideY.points = [
              new Point_default(-this.graph.getPanDx(), valueY),
              new Point_default(c.scrollWidth - 3 - this.graph.getPanDx(), valueY)
            ];
          }
          this.guideY.stroke = this.getGuideColor(stateY, false);
          this.guideY.node.style.visibility = "visible";
          this.guideY.redraw();
        }
      }
      return delta;
    }
    /**
     * Rounds to pixels for virtual states (eg. page guides)
     */
    getDelta(bounds, stateX = null, dx, stateY = null, dy) {
      const s = this.graph.view.scale;
      if (this.rounded || stateX != null && stateX.cell == null) {
        dx = Math.round((bounds.x + dx) / s) * s - bounds.x;
      }
      if (this.rounded || stateY != null && stateY.cell == null) {
        dy = Math.round((bounds.y + dy) / s) * s - bounds.y;
      }
      return new Point_default(dx, dy);
    }
    /**
     * Hides all current guides.
     */
    getGuideColor(state, horizontal) {
      return GUIDE_COLOR;
    }
    /**
     * Hides all current guides.
     */
    hide() {
      this.setVisible(false);
    }
    /**
     * Shows or hides the current guides.
     */
    setVisible(visible) {
      if (this.guideX) {
        this.guideX.node.style.visibility = visible ? "visible" : "hidden";
      }
      if (this.guideY) {
        this.guideY.node.style.visibility = visible ? "visible" : "hidden";
      }
    }
    /**
     * Destroys all resources that this object uses.
     */
    destroy() {
      if (this.guideX) {
        this.guideX.destroy();
        this.guideX = null;
      }
      if (this.guideY) {
        this.guideY.destroy();
        this.guideY = null;
      }
    }
  };
  var Guide_default = Guide;

  // node_modules/@maxgraph/core/lib/esm/view/plugin/SelectionHandler.js
  var SelectionHandler = class {
    /**
     * Constructs an event handler that creates handles for the selection cells.
     *
     * @param graph Reference to the enclosing {@link AbstractGraph}.
     */
    constructor(graph) {
      this.refreshThread = null;
      this.maxCells = 50;
      this.enabled = true;
      this.highlightEnabled = true;
      this.cloneEnabled = true;
      this.moveEnabled = true;
      this.guidesEnabled = false;
      this.handlesVisible = true;
      this.guide = null;
      this.currentDx = 0;
      this.currentDy = 0;
      this.updateCursor = true;
      this.selectEnabled = true;
      this.removeCellsFromParent = true;
      this.removeEmptyParents = false;
      this.connectOnDrop = false;
      this.scrollOnMove = true;
      this.minimumSize = 6;
      this.previewColor = "black";
      this.htmlPreview = false;
      this.shape = null;
      this.scaleGrid = false;
      this.rotationEnabled = true;
      this.maxLivePreview = 0;
      this.allowLivePreview = Client_default.IS_SVG;
      this.cell = null;
      this.delayedSelection = false;
      this.first = null;
      this.cells = null;
      this.bounds = null;
      this.pBounds = null;
      this.allCells = /* @__PURE__ */ new Map();
      this.cellWasClicked = false;
      this.cloning = false;
      this.cellCount = 0;
      this.target = null;
      this.suspended = false;
      this.livePreviewActive = false;
      this.livePreviewUsed = false;
      this.highlight = null;
      this.graph = graph;
      this.graph.addMouseListener(this);
      this.panHandler = () => {
        if (!this.suspended) {
          this.updatePreview();
          this.updateHint();
        }
      };
      this.graph.addListener(InternalEvent_default.PAN, this.panHandler);
      this.escapeHandler = (sender, evt) => {
        this.reset();
      };
      this.graph.addListener(InternalEvent_default.ESCAPE, this.escapeHandler);
      this.refreshHandler = (sender, evt) => {
        if (this.refreshThread) {
          window.clearTimeout(this.refreshThread);
        }
        this.refreshThread = window.setTimeout(() => {
          var _a2, _b;
          this.refreshThread = null;
          if (this.first && !this.suspended && this.cells) {
            const dx = this.currentDx;
            const dy = this.currentDy;
            this.currentDx = 0;
            this.currentDy = 0;
            this.updatePreview();
            this.bounds = this.graph.getView().getBounds(this.cells);
            this.pBounds = this.getPreviewBounds(this.cells);
            if (this.pBounds == null && !this.livePreviewUsed) {
              this.reset();
            } else {
              this.currentDx = dx;
              this.currentDy = dy;
              this.updatePreview();
              this.updateHint();
              if (this.livePreviewUsed) {
                this.setHandlesVisibleForCells((_b = (_a2 = this.getSelectionCellsHandler()) == null ? void 0 : _a2.getHandledSelectionCells()) != null ? _b : [], false, true);
                this.updatePreview();
              }
            }
          }
        }, 0);
      };
      this.graph.getDataModel().addListener(InternalEvent_default.CHANGE, this.refreshHandler);
      this.graph.addListener(InternalEvent_default.REFRESH, this.refreshHandler);
      this.keyHandler = (e) => {
        if (this.graph.container != null && this.graph.container.style.visibility !== "hidden" && this.first != null && !this.suspended) {
          const clone2 = this.graph.isCloneEvent(e) && this.graph.isCellsCloneable() && this.isCloneEnabled();
          if (clone2 !== this.cloning) {
            this.cloning = clone2;
            this.checkPreview();
            this.updatePreview();
          }
        }
      };
      if (typeof document !== "undefined") {
        InternalEvent_default.addListener(document, "keydown", this.keyHandler);
        InternalEvent_default.addListener(document, "keyup", this.keyHandler);
      }
    }
    /**
     * Returns <enabled>.
     */
    isEnabled() {
      return this.enabled;
    }
    /**
     * Sets <enabled>.
     */
    setEnabled(value) {
      this.enabled = value;
    }
    /**
     * Returns <cloneEnabled>.
     */
    isCloneEnabled() {
      return this.cloneEnabled;
    }
    /**
     * Sets <cloneEnabled>.
     *
     * @param value Boolean that specifies the new clone enabled state.
     */
    setCloneEnabled(value) {
      this.cloneEnabled = value;
    }
    /**
     * Returns {@link oveEnabled}.
     */
    isMoveEnabled() {
      return this.moveEnabled;
    }
    /**
     * Sets {@link oveEnabled}.
     */
    setMoveEnabled(value) {
      this.moveEnabled = value;
    }
    /**
     * Returns <selectEnabled>.
     */
    isSelectEnabled() {
      return this.selectEnabled;
    }
    /**
     * Sets <selectEnabled>.
     */
    setSelectEnabled(value) {
      this.selectEnabled = value;
    }
    /**
     * Returns <removeCellsFromParent>.
     */
    isRemoveCellsFromParent() {
      return this.removeCellsFromParent;
    }
    /**
     * Sets <removeCellsFromParent>.
     */
    setRemoveCellsFromParent(value) {
      this.removeCellsFromParent = value;
    }
    /**
     * Returns true if the given cell and parent should propagate
     * selection state to the parent.
     */
    isPropagateSelectionCell(cell, immediate, me) {
      const parent = cell.getParent();
      if (immediate) {
        const geo = cell.isEdge() ? null : cell.getGeometry();
        return !this.graph.isSiblingSelected(cell) && geo && geo.relative || !this.graph.isSwimlane(parent);
      }
      return (!this.graph.isToggleEvent(me.getEvent()) || !this.graph.isSiblingSelected(cell) && !this.graph.isCellSelected(cell) && !this.graph.isSwimlane(parent) || this.graph.isCellSelected(parent)) && (this.graph.isToggleEvent(me.getEvent()) || !this.graph.isCellSelected(parent));
    }
    /**
     * Hook to return initial cell for the given event.
     */
    getInitialCellForEvent(me) {
      let state = me.getState();
      if ((!this.graph.isToggleEvent(me.getEvent()) || !isAltDown(me.getEvent())) && state && !this.graph.isCellSelected(state.cell)) {
        let parent = state.cell.getParent();
        let next = parent ? this.graph.view.getState(parent) : null;
        while (next && !this.graph.isCellSelected(next.cell) && (next.cell.isVertex() || next.cell.isEdge()) && this.isPropagateSelectionCell(state.cell, true, me)) {
          state = next;
          parent = state.cell.getParent();
          next = parent ? this.graph.view.getState(parent) : null;
        }
      }
      return state ? state.cell : null;
    }
    /**
     * Hook to return true for delayed selections.
     */
    isDelayedSelection(cell, me) {
      let c = cell;
      const selectionCellsHandler = this.getSelectionCellsHandler();
      if (!this.graph.isToggleEvent(me.getEvent()) || !isAltDown(me.getEvent())) {
        while (c) {
          if (selectionCellsHandler == null ? void 0 : selectionCellsHandler.isHandled(c)) {
            const cellEditorHandler = this.graph.getPlugin("CellEditorHandler");
            return (cellEditorHandler == null ? void 0 : cellEditorHandler.getEditingCell()) !== c;
          }
          c = c.getParent();
        }
      }
      return this.graph.isToggleEvent(me.getEvent()) && !isAltDown(me.getEvent());
    }
    /**
     * Implements the delayed selection for the given mouse event.
     */
    selectDelayed(me) {
      const popupMenuHandler = this.graph.getPlugin("PopupMenuHandler");
      if (!popupMenuHandler || !popupMenuHandler.isPopupTrigger(me)) {
        let cell = me.getCell();
        if (cell === null) {
          cell = this.cell;
        }
        if (cell)
          this.selectCellForEvent(cell, me);
      }
    }
    /**
     * Selects the given cell for the given {@link MouseEvent}.
     */
    selectCellForEvent(cell, me) {
      const state = this.graph.view.getState(cell);
      if (state) {
        if (me.isSource(state.control)) {
          this.graph.selectCellForEvent(cell, me.getEvent());
        } else {
          if (!this.graph.isToggleEvent(me.getEvent()) || !isAltDown(me.getEvent())) {
            let parent = cell.getParent();
            while (parent && this.graph.view.getState(parent) && (parent.isVertex() || parent.isEdge()) && this.isPropagateSelectionCell(cell, false, me)) {
              cell = parent;
              parent = cell.getParent();
            }
          }
          this.graph.selectCellForEvent(cell, me.getEvent());
        }
      }
      return cell;
    }
    /**
     * Consumes the given mouse event.
     *
     * **NOTE**: This may be used to enable click events for links in labels on iOS as follows as consuming the initial
     * touchStart disables firing the subsequent click event on the link.
     *
     * ```js
     * consumeMouseEvent(evtName, me) {
     *   const source = eventUtils.getSource(me.getEvent());
     *
     *   if (!eventUtils.isTouchEvent(me.getEvent()) || source.nodeName != 'A') {
     *     me.consume();
     *   }
     * }
     * ```
     */
    consumeMouseEvent(_evtName, me) {
      me.consume();
    }
    /**
     * Handles the event by selecting the given cell and creating a handle for it.
     * By consuming the event all subsequent events of the gesture are redirected to this handler.
     */
    mouseDown(_sender, me) {
      if (!me.isConsumed() && this.isEnabled() && this.graph.isEnabled() && me.getState() && !isMultiTouchEvent(me.getEvent())) {
        const cell = this.getInitialCellForEvent(me);
        if (cell) {
          this.delayedSelection = this.isDelayedSelection(cell, me);
          this.cell = null;
          if (this.isSelectEnabled() && !this.delayedSelection) {
            this.graph.selectCellForEvent(cell, me.getEvent());
          }
          if (this.isMoveEnabled()) {
            const geo = cell.getGeometry();
            if (geo && this.graph.isCellMovable(cell) && (!cell.isEdge() || this.graph.getSelectionCount() > 1 || geo.points && geo.points.length > 0 || !cell.getTerminal(true) || !cell.getTerminal(false) || this.graph.isAllowDanglingEdges() || this.graph.isCloneEvent(me.getEvent()) && this.graph.isCellsCloneable())) {
              this.start(cell, me.getX(), me.getY());
            } else if (this.delayedSelection) {
              this.cell = cell;
            }
            this.cellWasClicked = true;
            this.consumeMouseEvent(InternalEvent_default.MOUSE_DOWN, me);
          }
        }
      }
    }
    /**
     * Creates an array of cell states which should be used as guides.
     */
    getGuideStates() {
      const parent = this.graph.getDefaultParent();
      const filter = (cell) => {
        const geo = cell.getGeometry();
        return !!this.graph.view.getState(cell) && cell.isVertex() && !!geo && !geo.relative;
      };
      return this.graph.view.getCellStates(parent.filterDescendants(filter));
    }
    /**
     * Returns the cells to be modified by this handler. This implementation
     * returns all selection cells that are movable, or the given initial cell if
     * the given cell is not selected and movable. This handles the case of moving
     * unselectable or unselected cells.
     *
     * @param initialCell <Cell> that triggered this handler.
     */
    getCells(initialCell) {
      if (!this.delayedSelection && this.graph.isCellMovable(initialCell)) {
        return [initialCell];
      }
      return this.graph.getMovableCells(this.graph.getSelectionCells());
    }
    /**
     * Returns the {@link Rectangle} used as the preview bounds for
     * moving the given cells.
     */
    getPreviewBounds(cells) {
      const bounds = this.getBoundingBox(cells);
      if (bounds) {
        bounds.width = Math.max(0, bounds.width - 1);
        bounds.height = Math.max(0, bounds.height - 1);
        if (bounds.width < this.minimumSize) {
          const dx = this.minimumSize - bounds.width;
          bounds.x -= dx / 2;
          bounds.width = this.minimumSize;
        } else {
          bounds.x = Math.round(bounds.x);
          bounds.width = Math.ceil(bounds.width);
        }
        if (bounds.height < this.minimumSize) {
          const dy = this.minimumSize - bounds.height;
          bounds.y -= dy / 2;
          bounds.height = this.minimumSize;
        } else {
          bounds.y = Math.round(bounds.y);
          bounds.height = Math.ceil(bounds.height);
        }
      }
      return bounds;
    }
    /**
     * Returns the union of the {@link CellStates} for the given array of {@link Cells}.
     * For vertices, this method uses the bounding box of the corresponding shape
     * if one exists. The bounding box of the corresponding text label and all
     * controls and overlays are ignored. See also: {@link GraphView#getBounds} and
     * {@link AbstractGraph.getBoundingBox}.
     *
     * @param cells Array of {@link Cells} whose bounding box should be returned.
     */
    getBoundingBox(cells) {
      let result = null;
      if (cells.length > 0) {
        for (let i = 0; i < cells.length; i += 1) {
          if (cells[i].isVertex() || cells[i].isEdge()) {
            const state = this.graph.view.getState(cells[i]);
            if (state) {
              let bbox = null;
              if (cells[i].isVertex() && state.shape && state.shape.boundingBox) {
                bbox = state.shape.boundingBox;
              }
              if (bbox) {
                if (!result) {
                  result = Rectangle_default.fromRectangle(bbox);
                } else {
                  result.add(bbox);
                }
              }
            }
          }
        }
      }
      return result;
    }
    /**
     * Creates the shape used to draw the preview for the given bounds.
     */
    createPreviewShape(bounds) {
      const shape = new RectangleShape_default(bounds, NONE, this.previewColor);
      shape.isDashed = true;
      if (this.htmlPreview) {
        shape.dialect = "strictHtml";
        shape.init(this.graph.container);
      } else {
        shape.dialect = "svg";
        shape.init(this.graph.getView().getOverlayPane());
        shape.pointerEvents = false;
        if (Client_default.IS_IOS) {
          shape.getSvgScreenOffset = () => {
            return 0;
          };
        }
      }
      return shape;
    }
    createGuide() {
      return new Guide_default(this.graph, this.getGuideStates());
    }
    /**
     * Starts the handling of the mouse gesture.
     */
    start(cell, x, y, cells) {
      this.cell = cell;
      this.first = convertPoint(this.graph.container, x, y);
      this.cells = cells ? cells : this.getCells(this.cell);
      this.bounds = this.graph.getView().getBounds(this.cells);
      this.pBounds = this.getPreviewBounds(this.cells);
      this.cloning = false;
      this.cellCount = 0;
      for (let i = 0; i < this.cells.length; i += 1) {
        this.cellCount += this.addStates(this.cells[i], this.allCells);
      }
      if (this.guidesEnabled) {
        this.guide = this.createGuide();
        const parent = cell.getParent();
        const ignore = parent.getChildCount() < 2;
        const connected = /* @__PURE__ */ new Map();
        const opps = this.graph.getOpposites(this.graph.getEdges(this.cell), this.cell);
        for (let i = 0; i < opps.length; i += 1) {
          const state = this.graph.view.getState(opps[i]);
          if (state && !connected.get(state)) {
            connected.set(state, true);
          }
        }
        this.guide.isStateIgnored = (state) => {
          const p = state.cell.getParent();
          return !!state.cell && (!this.cloning && !!this.isCellMoving(state.cell) || state.cell !== (this.target || parent) && !ignore && !connected.get(state) && (!this.target || this.target.getChildCount() >= 2) && p !== (this.target || parent));
        };
      }
    }
    /**
     * Adds the states for the given cell recursively to the given Map.
     * @param cell
     * @param dict
     */
    addStates(cell, dict) {
      const state = this.graph.view.getState(cell);
      let count = 0;
      if (state && !dict.get(cell)) {
        dict.set(cell, state);
        count++;
        const childCount = cell.getChildCount();
        for (let i = 0; i < childCount; i += 1) {
          count += this.addStates(cell.getChildAt(i), dict);
        }
      }
      return count;
    }
    /**
     * Returns true if the given cell is currently being moved.
     */
    isCellMoving(cell) {
      return this.allCells.has(cell);
    }
    /**
     * Returns true if the guides should be used for the given {@link MouseEvent}.
     * This implementation returns {@link Guide#isEnabledForEvent}.
     */
    useGuidesForEvent(me) {
      return this.guide ? this.guide.isEnabledForEvent(me.getEvent()) && !this.graph.isConstrainedEvent(me.getEvent()) : true;
    }
    /**
     * Snaps the given vector to the grid and returns the given mxPoint instance.
     */
    snap(vector) {
      const scale = this.scaleGrid ? this.graph.view.scale : 1;
      vector.x = this.graph.snap(vector.x / scale) * scale;
      vector.y = this.graph.snap(vector.y / scale) * scale;
      return vector;
    }
    /**
     * Returns an {@link Point} that represents the vector for moving the cells
     * for the given {@link MouseEvent}.
     */
    getDelta(me) {
      const point = convertPoint(this.graph.container, me.getX(), me.getY());
      if (!this.first)
        return new Point_default();
      return new Point_default(point.x - this.first.x - this.graph.getPanDx(), point.y - this.first.y - this.graph.getPanDy());
    }
    /**
     * Hook for subclassers do show details while the handler is active.
     */
    updateHint(me) {
      return;
    }
    /**
     * Hooks for subclassers to hide details when the handler gets inactive.
     */
    removeHint() {
      return;
    }
    /**
     * Hook for rounding the unscaled vector. This uses Math.round.
     */
    roundLength(length) {
      return Math.round(length * 100) / 100;
    }
    /**
     * Returns true if the given cell is a valid drop target.
     */
    isValidDropTarget(target, me) {
      return this.cell ? this.cell.getParent() !== target : false;
    }
    /**
     * Updates the preview if cloning state has changed.
     */
    checkPreview() {
      if (this.livePreviewActive && this.cloning) {
        this.resetLivePreview();
        this.livePreviewActive = false;
      } else if (this.maxLivePreview >= this.cellCount && !this.livePreviewActive && this.allowLivePreview) {
        if (!this.cloning || !this.livePreviewActive) {
          this.livePreviewActive = true;
          this.livePreviewUsed = true;
        }
      } else if (!this.livePreviewUsed && !this.shape && this.bounds) {
        this.shape = this.createPreviewShape(this.bounds);
      }
    }
    /**
     * Handles the event by highlighting possible drop targets and updating the preview.
     */
    mouseMove(_sender, me) {
      const { graph } = this;
      if (!me.isConsumed() && graph.isMouseDown && this.cell && this.first && this.bounds && !this.suspended) {
        if (isMultiTouchEvent(me.getEvent())) {
          this.reset();
          return;
        }
        let delta = this.getDelta(me);
        const tol = graph.getEventTolerance();
        if (this.shape || this.livePreviewActive || Math.abs(delta.x) > tol || Math.abs(delta.y) > tol) {
          if (!this.highlight) {
            this.highlight = new CellHighlight_default(this.graph, DROP_TARGET_COLOR, 3);
          }
          const clone2 = graph.isCloneEvent(me.getEvent()) && graph.isCellsCloneable() && this.isCloneEnabled();
          const gridEnabled = graph.isGridEnabledEvent(me.getEvent());
          const cell = me.getCell();
          let hideGuide = true;
          let target = null;
          this.cloning = clone2;
          if (graph.isDropEnabled() && this.highlightEnabled && this.cells) {
            target = graph.getDropTarget(this.cells, me.getEvent(), cell, clone2);
          }
          let state = target ? graph.getView().getState(target) : null;
          let highlight = false;
          if (state && (clone2 || target && this.isValidDropTarget(target, me))) {
            if (this.target !== target) {
              this.target = target;
              this.setHighlightColor(DROP_TARGET_COLOR);
            }
            highlight = true;
          } else {
            this.target = null;
            if (this.connectOnDrop && cell && this.cells && this.cells.length === 1 && cell.isVertex() && cell.isConnectable()) {
              state = graph.getView().getState(cell);
              if (state) {
                const error = graph.getEdgeValidationError(null, this.cell, cell);
                const color = error === null ? VALID_COLOR : INVALID_CONNECT_TARGET_COLOR;
                this.setHighlightColor(color);
                highlight = true;
              }
            }
          }
          if (state && highlight) {
            this.highlight.highlight(state);
          } else {
            this.highlight.hide();
          }
          if (this.guide && this.useGuidesForEvent(me)) {
            delta = this.guide.move(this.bounds, delta, gridEnabled, clone2);
            hideGuide = false;
          } else {
            delta = this.graph.snapDelta(delta, this.bounds, !gridEnabled, false, false);
          }
          if (this.guide && hideGuide) {
            this.guide.hide();
          }
          if (graph.isConstrainedEvent(me.getEvent())) {
            if (Math.abs(delta.x) > Math.abs(delta.y)) {
              delta.y = 0;
            } else {
              delta.x = 0;
            }
          }
          this.checkPreview();
          if (this.currentDx !== delta.x || this.currentDy !== delta.y) {
            this.currentDx = delta.x;
            this.currentDy = delta.y;
            this.updatePreview();
          }
        }
        this.updateHint(me);
        this.consumeMouseEvent(InternalEvent_default.MOUSE_MOVE, me);
        InternalEvent_default.consume(me.getEvent());
      } else if ((this.isMoveEnabled() || this.isCloneEnabled()) && this.updateCursor && !me.isConsumed() && (me.getState() || me.sourceState) && !graph.isMouseDown) {
        let cursor = graph.getCursorForMouseEvent(me);
        const cell = me.getCell();
        if (!cursor && cell && graph.isEnabled() && graph.isCellMovable(cell)) {
          if (cell.isEdge()) {
            cursor = EdgeHandlerConfig.cursorMovable;
          } else {
            cursor = VertexHandlerConfig.cursorMovable;
          }
        }
        if (cursor && me.sourceState) {
          me.sourceState.setCursor(cursor);
        }
      }
    }
    /**
     * Updates the bounds of the preview shape.
     */
    updatePreview(remote = false) {
      var _a2, _b;
      if (this.livePreviewUsed && !remote) {
        if (this.cells) {
          this.setHandlesVisibleForCells((_b = (_a2 = this.getSelectionCellsHandler()) == null ? void 0 : _a2.getHandledSelectionCells()) != null ? _b : [], false);
          this.updateLivePreview(this.currentDx, this.currentDy);
        }
      } else {
        this.updatePreviewShape();
      }
    }
    /**
     * Updates the bounds of the preview shape.
     */
    updatePreviewShape() {
      if (this.shape && this.pBounds) {
        this.shape.bounds = new Rectangle_default(Math.round(this.pBounds.x + this.currentDx), Math.round(this.pBounds.y + this.currentDy), this.pBounds.width, this.pBounds.height);
        this.shape.redraw();
      }
    }
    /**
     * Updates the bounds of the preview shape.
     */
    updateLivePreview(dx, dy) {
      if (!this.suspended) {
        const states = [];
        if (this.allCells) {
          this.allCells.forEach((state) => {
            const realState = state ? this.graph.view.getState(state.cell) : null;
            if (realState !== state && state) {
              state.destroy();
              if (realState) {
                this.allCells.set(state.cell, realState);
              } else {
                this.allCells.delete(state.cell);
              }
              state = realState;
            }
            if (state) {
              const tempState = state.clone();
              states.push([state, tempState]);
              if (state.shape) {
                if (state.shape.originalPointerEvents === null) {
                  state.shape.originalPointerEvents = state.shape.pointerEvents;
                }
                state.shape.pointerEvents = false;
                if (state.text) {
                  if (state.text.originalPointerEvents === null) {
                    state.text.originalPointerEvents = state.text.pointerEvents;
                  }
                  state.text.pointerEvents = false;
                }
              }
              if (state.cell.isVertex()) {
                state.x += dx;
                state.y += dy;
                if (!this.cloning) {
                  state.view.graph.cellRenderer.redraw(state, true);
                  state.view.invalidate(state.cell);
                  state.invalid = false;
                  if (state.control && state.control.node) {
                    state.control.node.style.visibility = "hidden";
                  }
                } else if (state.text) {
                  state.text.updateBoundingBox();
                  if (state.text.boundingBox) {
                    state.text.boundingBox.x += dx;
                    state.text.boundingBox.y += dy;
                  }
                  if (state.text.unrotatedBoundingBox) {
                    state.text.unrotatedBoundingBox.x += dx;
                    state.text.unrotatedBoundingBox.y += dy;
                  }
                }
              }
            }
          });
        }
        if (states.length === 0) {
          this.reset();
        } else {
          const s = this.graph.view.scale;
          for (let i = 0; i < states.length; i += 1) {
            const state = states[i][0];
            if (state.cell.isEdge()) {
              const geometry = state.cell.getGeometry();
              const points = [];
              if (geometry && geometry.points) {
                for (let j = 0; j < geometry.points.length; j++) {
                  if (geometry.points[j]) {
                    points.push(new Point_default(geometry.points[j].x + dx / s, geometry.points[j].y + dy / s));
                  }
                }
              }
              let source = state.visibleSourceState;
              let target = state.visibleTargetState;
              const pts = states[i][1].absolutePoints;
              if (source == null || !this.isCellMoving(source.cell)) {
                const pt0 = pts[0];
                if (pt0) {
                  state.setAbsoluteTerminalPoint(new Point_default(pt0.x + dx, pt0.y + dy), true);
                  source = null;
                }
              } else {
                state.view.updateFixedTerminalPoint(state, source, true, this.graph.getConnectionConstraint(state, source, true));
              }
              if (target == null || !this.isCellMoving(target.cell)) {
                const ptn = pts[pts.length - 1];
                if (ptn) {
                  state.setAbsoluteTerminalPoint(new Point_default(ptn.x + dx, ptn.y + dy), false);
                  target = null;
                }
              } else {
                state.view.updateFixedTerminalPoint(state, target, false, this.graph.getConnectionConstraint(state, target, false));
              }
              state.view.updatePoints(state, points, source, target);
              state.view.updateFloatingTerminalPoints(state, source, target);
              state.view.updateEdgeLabelOffset(state);
              state.invalid = false;
              if (!this.cloning) {
                state.view.graph.cellRenderer.redraw(state, true);
              }
            }
          }
          this.graph.view.validate();
          this.redrawHandles(states);
          this.resetPreviewStates(states);
        }
      }
    }
    /**
     * Redraws the preview shape for the given states array.
     */
    redrawHandles(states) {
      const selectionCellsHandler = this.getSelectionCellsHandler();
      for (let i = 0; i < states.length; i += 1) {
        const handler = selectionCellsHandler == null ? void 0 : selectionCellsHandler.getHandler(states[i][0].cell);
        handler == null ? void 0 : handler.redraw(true);
      }
    }
    /**
     * Resets the given preview states array.
     */
    resetPreviewStates(states) {
      for (let i = 0; i < states.length; i += 1) {
        states[i][0].setState(states[i][1]);
      }
    }
    /**
     * Suspends the livew preview.
     */
    suspend() {
      if (!this.suspended) {
        if (this.livePreviewUsed) {
          this.updateLivePreview(0, 0);
        }
        if (this.shape) {
          this.shape.node.style.visibility = "hidden";
        }
        if (this.guide) {
          this.guide.setVisible(false);
        }
        this.suspended = true;
      }
    }
    /**
     * Suspends the livew preview.
     */
    resume() {
      if (this.suspended) {
        this.suspended = false;
        if (this.livePreviewUsed) {
          this.livePreviewActive = true;
        }
        if (this.shape) {
          this.shape.node.style.visibility = "visible";
        }
        if (this.guide) {
          this.guide.setVisible(true);
        }
      }
    }
    /**
     * Resets the livew preview.
     */
    resetLivePreview() {
      this.allCells.forEach((state) => {
        if (state.shape && state.shape.originalPointerEvents !== null) {
          state.shape.pointerEvents = state.shape.originalPointerEvents;
          state.shape.originalPointerEvents = null;
          state.shape.bounds = null;
          if (state.text && state.text.originalPointerEvents !== null) {
            state.text.pointerEvents = state.text.originalPointerEvents;
            state.text.originalPointerEvents = null;
          }
        }
        if (state.control && state.control.node && state.control.node.style.visibility === "hidden") {
          state.control.node.style.visibility = "";
        }
        if (!this.cloning) {
          if (state.text) {
            state.text.updateBoundingBox();
          }
        }
        state.view.invalidate(state.cell);
      });
      this.graph.view.validate();
    }
    /**
     * Sets whether the handles attached to the given cells are visible.
     *
     * @param cells Array of {@link Cell}s.
     * @param visible Boolean that specifies if the handles should be visible.
     * @param force Forces an update of the handler regardless of the last used value.
     */
    setHandlesVisibleForCells(cells, visible, force = false) {
      if (force || this.handlesVisible !== visible) {
        this.handlesVisible = visible;
        const selectionCellsHandler = this.getSelectionCellsHandler();
        for (let i = 0; i < cells.length; i += 1) {
          const handler = selectionCellsHandler == null ? void 0 : selectionCellsHandler.getHandler(cells[i]);
          if (handler) {
            handler.setHandlesVisible(visible);
            if (visible) {
              handler.redraw();
            }
          }
        }
      }
    }
    /**
     * Sets the color of the rectangle used to highlight drop targets.
     *
     * @param color String that represents the new highlight color.
     */
    setHighlightColor(color) {
      if (this.highlight) {
        this.highlight.setHighlightColor(color);
      }
    }
    /**
     * Handles the event by applying the changes to the selection cells.
     */
    mouseUp(_sender, me) {
      if (!me.isConsumed()) {
        if (this.livePreviewUsed) {
          this.resetLivePreview();
        }
        if (this.cell && this.first && (this.shape || this.livePreviewUsed) && isNumeric(this.currentDx) && isNumeric(this.currentDy)) {
          const { graph } = this;
          const cell = me.getCell();
          if (this.connectOnDrop && !this.target && cell && cell.isVertex() && cell.isConnectable() && graph.isEdgeValid(null, this.cell, cell)) {
            const connectionHandler = graph.getPlugin("ConnectionHandler");
            connectionHandler == null ? void 0 : connectionHandler.connect(this.cell, cell, me.getEvent());
          } else {
            const clone2 = graph.isCloneEvent(me.getEvent()) && graph.isCellsCloneable() && this.isCloneEnabled();
            const { scale } = graph.getView();
            const dx = this.roundLength(this.currentDx / scale);
            const dy = this.roundLength(this.currentDy / scale);
            const target = this.target;
            if (target && graph.isSplitEnabled() && this.cells && graph.isSplitTarget(target, this.cells, me.getEvent())) {
              graph.splitEdge(target, this.cells, null, dx, dy, me.getGraphX(), me.getGraphY());
            } else if (this.cells) {
              this.moveCells(this.cells, dx, dy, clone2, this.target, me.getEvent());
            }
          }
        } else if (this.isSelectEnabled() && this.delayedSelection && this.cell != null) {
          this.selectDelayed(me);
        }
      }
      if (this.cellWasClicked) {
        this.consumeMouseEvent(InternalEvent_default.MOUSE_UP, me);
      }
      this.reset();
    }
    /**
     * Resets the state of this handler.
     */
    reset() {
      var _a2, _b;
      if (this.livePreviewUsed) {
        this.resetLivePreview();
        this.setHandlesVisibleForCells((_b = (_a2 = this.getSelectionCellsHandler()) == null ? void 0 : _a2.getHandledSelectionCells()) != null ? _b : [], true);
      }
      this.destroyShapes();
      this.removeHint();
      this.delayedSelection = false;
      this.livePreviewActive = false;
      this.livePreviewUsed = false;
      this.cellWasClicked = false;
      this.suspended = false;
      this.currentDx = 0;
      this.currentDy = 0;
      this.cellCount = 0;
      this.cloning = false;
      this.allCells.clear();
      this.pBounds = null;
      this.target = null;
      this.first = null;
      this.cells = null;
      this.cell = null;
    }
    /**
     * Returns true if the given cells should be removed from the parent for the specified
     * mousereleased event.
     */
    shouldRemoveCellsFromParent(parent, cells, evt) {
      var _a2;
      if (parent.isVertex()) {
        const pState = this.graph.getView().getState(parent);
        if (pState) {
          let pt = convertPoint(this.graph.container, getClientX(evt), getClientY(evt));
          const alpha = toRadians((_a2 = pState.style.rotation) != null ? _a2 : 0);
          if (alpha !== 0) {
            const cos = Math.cos(-alpha);
            const sin = Math.sin(-alpha);
            const cx = new Point_default(pState.getCenterX(), pState.getCenterY());
            pt = getRotatedPoint(pt, cos, sin, cx);
          }
          return !contains(pState, pt.x, pt.y);
        }
      }
      return false;
    }
    /**
     * Moves the given cells by the specified amount.
     */
    moveCells(cells, dx, dy, clone2, target, evt) {
      if (!this.cell)
        return;
      if (clone2) {
        cells = this.graph.getCloneableCells(cells);
      }
      const parent = this.cell.getParent();
      if (!target && parent && this.isRemoveCellsFromParent() && this.shouldRemoveCellsFromParent(parent, cells, evt)) {
        target = this.graph.getDefaultParent();
      }
      clone2 = !!clone2 && !this.graph.isCellLocked(target || this.graph.getDefaultParent());
      this.graph.batchUpdate(() => {
        const parents = [];
        if (!clone2 && target && this.removeEmptyParents) {
          const dict = /* @__PURE__ */ new Map();
          for (let i = 0; i < cells.length; i += 1) {
            dict.set(cells[i], true);
          }
          for (let i = 0; i < cells.length; i += 1) {
            const par = cells[i].getParent();
            if (par && !dict.get(par)) {
              dict.set(par, true);
              parents.push(par);
            }
          }
        }
        cells = this.graph.moveCells(cells, dx, dy, clone2, target, evt);
        const temp = [];
        for (let i = 0; i < parents.length; i += 1) {
          if (this.shouldRemoveParent(parents[i])) {
            temp.push(parents[i]);
          }
        }
        this.graph.removeCells(temp, false);
      });
      if (clone2) {
        this.graph.setSelectionCells(cells);
      }
      if (this.isSelectEnabled() && this.scrollOnMove) {
        this.graph.scrollCellToVisible(cells[0]);
      }
    }
    /**
     * Returns true if the given parent should be removed after removal of child cells.
     */
    shouldRemoveParent(parent) {
      const state = this.graph.view.getState(parent);
      return state != null && (state.cell.isEdge() || state.cell.isVertex()) && this.graph.isCellDeletable(state.cell) && state.cell.getChildCount() === 0 && state.isTransparentState();
    }
    /**
     * Destroy the preview and highlight shapes.
     */
    destroyShapes() {
      if (this.shape) {
        this.shape.destroy();
        this.shape = null;
      }
      if (this.guide) {
        this.guide.destroy();
        this.guide = null;
      }
      if (this.highlight) {
        this.highlight.destroy();
        this.highlight = null;
      }
    }
    /**
     * Destroys the handler and all its resources and DOM nodes.
     */
    onDestroy() {
      this.graph.removeMouseListener(this);
      this.graph.removeListener(this.panHandler);
      this.graph.removeListener(this.escapeHandler);
      this.graph.getDataModel().removeListener(this.refreshHandler);
      this.graph.removeListener(this.refreshHandler);
      InternalEvent_default.removeListener(document, "keydown", this.keyHandler);
      InternalEvent_default.removeListener(document, "keyup", this.keyHandler);
      this.destroyShapes();
      this.removeHint();
    }
    getSelectionCellsHandler() {
      return this.graph.getPlugin("SelectionCellsHandler");
    }
  };
  SelectionHandler.pluginId = "SelectionHandler";
  var SelectionHandler_default = SelectionHandler;

  // node_modules/@maxgraph/core/lib/esm/view/other/PanningManager.js
  var PanningManager = class {
    constructor(graph) {
      this.damper = 1 / 6;
      this.delay = 10;
      this.handleMouseOut = true;
      this.border = 0;
      this.thread = null;
      this.active = false;
      this.tdx = 0;
      this.tdy = 0;
      this.t0x = 0;
      this.t0y = 0;
      this.dx = 0;
      this.dy = 0;
      this.scrollbars = false;
      this.scrollLeft = 0;
      this.scrollTop = 0;
      this.thread = null;
      this.active = false;
      this.tdx = 0;
      this.tdy = 0;
      this.t0x = 0;
      this.t0y = 0;
      this.dx = 0;
      this.dy = 0;
      this.scrollbars = false;
      this.scrollLeft = 0;
      this.scrollTop = 0;
      this.mouseListener = {
        mouseDown: () => {
        },
        mouseMove: () => {
        },
        mouseUp: () => {
          if (this.active) {
            this.stop();
          }
        }
      };
      graph.addMouseListener(this.mouseListener);
      this.mouseUpListener = () => {
        if (this.active) {
          this.stop();
        }
      };
      InternalEvent_default.addListener(document, "mouseup", this.mouseUpListener);
      const createThread = () => {
        this.scrollbars = hasScrollbars(graph.container);
        this.scrollLeft = graph.container.scrollLeft;
        this.scrollTop = graph.container.scrollTop;
        return window.setInterval(() => {
          this.tdx -= this.dx;
          this.tdy -= this.dy;
          if (this.scrollbars) {
            const left = -graph.container.scrollLeft - Math.ceil(this.dx);
            const top = -graph.container.scrollTop - Math.ceil(this.dy);
            graph.panGraph(left, top);
            graph.setPanDx(this.scrollLeft - graph.container.scrollLeft);
            graph.setPanDy(this.scrollTop - graph.container.scrollTop);
            graph.fireEvent(new EventObject_default(InternalEvent_default.PAN));
          } else {
            graph.panGraph(this.getDx(), this.getDy());
          }
        }, this.delay);
      };
      this.isActive = () => {
        return this.active;
      };
      this.getDx = () => {
        return Math.round(this.tdx);
      };
      this.getDy = () => {
        return Math.round(this.tdy);
      };
      this.start = () => {
        this.t0x = graph.view.translate.x;
        this.t0y = graph.view.translate.y;
        this.active = true;
      };
      this.panTo = (x, y, w = 0, h = 0) => {
        if (!this.active) {
          this.start();
        }
        this.scrollLeft = graph.container.scrollLeft;
        this.scrollTop = graph.container.scrollTop;
        const c = graph.container;
        this.dx = x + w - c.scrollLeft - c.clientWidth;
        if (this.dx < 0 && Math.abs(this.dx) < this.border) {
          this.dx = this.border + this.dx;
        } else if (this.handleMouseOut) {
          this.dx = Math.max(this.dx, 0);
        } else {
          this.dx = 0;
        }
        if (this.dx == 0) {
          this.dx = x - c.scrollLeft;
          if (this.dx > 0 && this.dx < this.border) {
            this.dx -= this.border;
          } else if (this.handleMouseOut) {
            this.dx = Math.min(0, this.dx);
          } else {
            this.dx = 0;
          }
        }
        this.dy = y + h - c.scrollTop - c.clientHeight;
        if (this.dy < 0 && Math.abs(this.dy) < this.border) {
          this.dy = this.border + this.dy;
        } else if (this.handleMouseOut) {
          this.dy = Math.max(this.dy, 0);
        } else {
          this.dy = 0;
        }
        if (this.dy == 0) {
          this.dy = y - c.scrollTop;
          if (this.dy > 0 && this.dy < this.border) {
            this.dy -= this.border;
          } else if (this.handleMouseOut) {
            this.dy = Math.min(0, this.dy);
          } else {
            this.dy = 0;
          }
        }
        if (this.dx != 0 || this.dy != 0) {
          this.dx *= this.damper;
          this.dy *= this.damper;
          if (this.thread == null) {
            this.thread = createThread();
          }
        } else if (this.thread != null) {
          window.clearInterval(this.thread);
          this.thread = null;
        }
      };
      this.stop = () => {
        if (this.active) {
          this.active = false;
          if (this.thread != null) {
            window.clearInterval(this.thread);
            this.thread = null;
          }
          this.tdx = 0;
          this.tdy = 0;
          if (!this.scrollbars) {
            const px = graph.getPanDx();
            const py = graph.getPanDy();
            if (px != 0 || py != 0) {
              graph.panGraph(0, 0);
              graph.view.setTranslate(this.t0x + px / graph.view.scale, this.t0y + py / graph.view.scale);
            }
          } else {
            graph.setPanDx(0);
            graph.setPanDy(0);
            graph.fireEvent(new EventObject_default(InternalEvent_default.PAN));
          }
        }
      };
      this.destroy = () => {
        this.stop();
        graph.removeMouseListener(this.mouseListener);
        InternalEvent_default.removeListener(document, "mouseup", this.mouseUpListener);
      };
    }
  };
  var PanningManager_default = PanningManager;

  // node_modules/@maxgraph/core/lib/esm/view/plugin/PanningHandler.js
  var PanningHandler = class extends EventSource_default {
    constructor(graph) {
      super();
      this.getPanningManager = () => this.panningManager;
      this.useLeftButtonForPanning = false;
      this.usePopupTrigger = true;
      this.ignoreCell = false;
      this.previewEnabled = true;
      this.useGrid = false;
      this.panningEnabled = false;
      this.pinchEnabled = true;
      this.initialScale = 0;
      this.maxScale = 8;
      this.minScale = 0.01;
      this.dx = 0;
      this.dy = 0;
      this.startX = 0;
      this.startY = 0;
      this.dx0 = 0;
      this.dy0 = 0;
      this.panningTrigger = false;
      this.active = false;
      this.mouseDownEvent = null;
      this.graph = graph;
      this.graph.addMouseListener(this);
      this.forcePanningHandler = (_sender, eo) => {
        const evtName = eo.getProperty("eventName");
        const me = eo.getProperty("event");
        if (evtName === InternalEvent_default.MOUSE_DOWN && this.isForcePanningEvent(me)) {
          this.start(me);
          this.active = true;
          this.fireEvent(new EventObject_default(InternalEvent_default.PAN_START, { event: me }));
          me.consume();
        }
      };
      this.graph.addListener(InternalEvent_default.FIRE_MOUSE_EVENT, this.forcePanningHandler);
      this.gestureHandler = (_sender, eo) => {
        if (this.isPinchEnabled()) {
          const evt = eo.getProperty("event");
          if (!isConsumed(evt) && evt.type === "gesturestart") {
            this.initialScale = this.graph.view.scale;
            if (!this.active && this.mouseDownEvent) {
              this.start(this.mouseDownEvent);
              this.mouseDownEvent = null;
            }
          } else if (evt.type === "gestureend" && this.initialScale !== 0) {
            this.initialScale = 0;
          }
          if (this.initialScale !== 0) {
            this.zoomGraph(evt);
          }
        }
      };
      this.graph.addListener(InternalEvent_default.GESTURE, this.gestureHandler);
      this.mouseUpListener = () => {
        if (this.active) {
          this.reset();
        }
      };
      InternalEvent_default.addListener(document, "mouseup", this.mouseUpListener);
      this.panningManager = new PanningManager_default(graph);
    }
    /**
     * Returns true if the handler is currently active.
     */
    isActive() {
      return this.active || this.initialScale !== null;
    }
    /**
     * Returns <panningEnabled>.
     */
    isPanningEnabled() {
      return this.panningEnabled;
    }
    /**
     * Sets <panningEnabled>.
     */
    setPanningEnabled(value) {
      this.panningEnabled = value;
    }
    /**
     * Returns <pinchEnabled>.
     */
    isPinchEnabled() {
      return this.pinchEnabled;
    }
    /**
     * Sets <pinchEnabled>.
     */
    setPinchEnabled(value) {
      this.pinchEnabled = value;
    }
    /**
     * Returns `true` if the given event is a panning trigger for the optional given cell.
     *
     * This returns true if control-shift is pressed or if {@link usePopupTrigger} is `true` and the event is a popup trigger.
     */
    isPanningTrigger(me) {
      const evt = me.getEvent();
      return this.useLeftButtonForPanning && !me.getState() && isLeftMouseButton(evt) || isControlDown(evt) && isShiftDown(evt) || this.usePopupTrigger && isPopupTrigger(evt);
    }
    /**
     * Returns true if the given {@link MouseEvent} should start panning.
     *
     * This implementation always returns `true` if {@link ignoreCell} is `true` or for multi touch events.
     */
    isForcePanningEvent(me) {
      return this.ignoreCell || isMultiTouchEvent(me.getEvent());
    }
    /**
     * Handles the event by initiating the panning. By consuming the event all
     * subsequent events of the gesture are redirected to this handler.
     */
    mouseDown(_sender, me) {
      this.mouseDownEvent = me;
      if (!me.isConsumed() && this.isPanningEnabled() && !this.active && this.isPanningTrigger(me)) {
        this.start(me);
        this.consumePanningTrigger(me);
      }
    }
    /**
     * Starts panning at the given event.
     */
    start(me) {
      this.dx0 = -this.graph.container.scrollLeft;
      this.dy0 = -this.graph.container.scrollTop;
      this.startX = me.getX();
      this.startY = me.getY();
      this.dx = 0;
      this.dy = 0;
      this.panningTrigger = true;
    }
    /**
     * Consumes the given {@link MouseEvent} if it was a panning trigger in
     * {@link ouseDown}. The default is to invoke {@link MouseEvent#consume}. Note that this
     * will block any further event processing. If you haven't disabled built-in
     * context menus and require immediate selection of the cell on mouseDown in
     * Safari and/or on the Mac, then use the following code:
     *
     * ```javascript
     * consumePanningTrigger(me)
     * {
     *   if (me.evt.preventDefault)
     *   {
     *     me.evt.preventDefault();
     *   }
     *
     *   // Stops event processing in IE
     *   me.evt.returnValue = false;
     *
     *   // Sets local consumed state
     *   if (!Client.IS_SF && !Client.IS_MAC)
     *   {
     *     me.consumed = true;
     *   }
     * };
     * ```
     */
    consumePanningTrigger(me) {
      me.consume();
    }
    /**
     * Handles the event by updating the panning on the graph.
     */
    mouseMove(_sender, me) {
      this.dx = me.getX() - this.startX;
      this.dy = me.getY() - this.startY;
      if (this.active) {
        if (this.previewEnabled) {
          if (this.useGrid) {
            this.dx = this.graph.snap(this.dx);
            this.dy = this.graph.snap(this.dy);
          }
          this.graph.panGraph(this.dx + this.dx0, this.dy + this.dy0);
        }
        this.fireEvent(new EventObject_default(InternalEvent_default.PAN, { event: me }));
      } else if (this.panningTrigger) {
        const tmp = this.active;
        this.active = Math.abs(this.dx) > this.graph.getSnapTolerance() || Math.abs(this.dy) > this.graph.getSnapTolerance();
        if (!tmp && this.active) {
          this.fireEvent(new EventObject_default(InternalEvent_default.PAN_START, { event: me }));
        }
      }
      if (this.active || this.panningTrigger) {
        me.consume();
      }
    }
    /**
     * Handles the event by setting the translation on the view or showing the
     * popupmenu.
     */
    mouseUp(_sender, me) {
      if (this.active) {
        if (this.dx !== 0 && this.dy !== 0) {
          if (!this.graph.isUseScrollbarsForPanning() || !hasScrollbars(this.graph.container)) {
            const { scale } = this.graph.getView();
            const t = this.graph.getView().translate;
            this.graph.panGraph(0, 0);
            this.panGraph(t.x + this.dx / scale, t.y + this.dy / scale);
          }
          me.consume();
        }
        this.fireEvent(new EventObject_default(InternalEvent_default.PAN_END, { event: me }));
      }
      this.reset();
    }
    /**
     * Zooms the graph to the given value and consumed the event if needed.
     */
    zoomGraph(evt) {
      let value = Math.round(this.initialScale * evt.scale * 100) / 100;
      value = Math.max(this.minScale, value);
      value = Math.min(this.maxScale, value);
      if (this.graph.view.scale !== value) {
        this.graph.zoomTo(value);
        InternalEvent_default.consume(evt);
      }
    }
    /**
     * Handles the event by setting the translation on the view or showing the
     * popupmenu.
     */
    reset() {
      this.panningTrigger = false;
      this.mouseDownEvent = null;
      this.active = false;
      this.dx = 0;
      this.dy = 0;
    }
    /**
     * Pans {@link graph} by the given amount.
     */
    panGraph(dx, dy) {
      this.graph.getView().setTranslate(dx, dy);
    }
    /**
     * Destroys the handler and all its resources and DOM nodes.
     */
    onDestroy() {
      this.graph.removeMouseListener(this);
      this.graph.removeListener(this.forcePanningHandler);
      this.graph.removeListener(this.gestureHandler);
      InternalEvent_default.removeListener(document, "mouseup", this.mouseUpListener);
      this.panningManager.destroy();
      super.destroy();
    }
  };
  PanningHandler.pluginId = "PanningHandler";
  var PanningHandler_default = PanningHandler;

  // node_modules/@maxgraph/core/lib/esm/view/plugin/FitPlugin.js
  function keep2digits(value) {
    return Number(value.toFixed(2));
  }
  var FitPlugin = class {
    /**
     * Constructs the plugin that provides `fit` methods.
     *
     * @param graph Reference to the enclosing {@link AbstractGraph}.
     */
    constructor(graph) {
      this.graph = graph;
      this.minFitScale = 0.1;
      this.maxFitScale = 8;
    }
    /**
     * Scales the graph such that the complete diagram fits into {@link Graph.container} and returns the current scale in the view.
     * To fit an initial graph prior to rendering, set {@link GraphView.rendering} to `false` prior to changing the model
     * and execute the following after changing the model.
     *
     * ```javascript
     * graph.view.rendering = false;
     * // here, change the model
     * graph.getPlugin<FitPlugin>('fit')?.fit();
     * graph.view.rendering = true;
     * graph.refresh();
     * ```
     *
     * To fit and center the graph, use {@link fitCenter}.
     *
     * @param options Optional number that specifies the border.
     * @since 0.21.0
     */
    fit(options = {}) {
      var _a2, _b;
      const { border = this.graph.getBorder(), keepOrigin = false, margin = 0, enabled = true, ignoreWidth = false, ignoreHeight = false, maxHeight = null } = options;
      const { backgroundImage, container, view } = this.graph;
      if (!container) {
        return view.scale;
      }
      let bounds = view.getGraphBounds();
      if (!(bounds.width > 0 && bounds.height > 0)) {
        return view.scale;
      }
      const cssBorder = this.graph.getBorderSizes();
      let w1 = container.offsetWidth - cssBorder.x - cssBorder.width - 1;
      let h1 = maxHeight != null ? maxHeight : container.offsetHeight - cssBorder.y - cssBorder.height - 1;
      if (keepOrigin && bounds.x != null && bounds.y != null) {
        bounds = bounds.clone();
        bounds.width += bounds.x;
        bounds.height += bounds.y;
        bounds.x = 0;
        bounds.y = 0;
      }
      const originalScale = view.scale;
      let w2 = bounds.width / originalScale;
      let h2 = bounds.height / originalScale;
      if (backgroundImage) {
        w2 = Math.max(w2, backgroundImage.width - bounds.x / originalScale);
        h2 = Math.max(h2, backgroundImage.height - bounds.y / originalScale);
      }
      const b = (keepOrigin ? border : 2 * border) + margin + 1;
      w1 -= b;
      h1 -= b;
      let newScale = ignoreWidth ? h1 / h2 : ignoreHeight ? w1 / w2 : Math.min(w1 / w2, h1 / h2);
      const minScale = (_a2 = this.minFitScale) != null ? _a2 : 0;
      const maxScale = (_b = this.maxFitScale) != null ? _b : Infinity;
      newScale = Math.max(Math.min(newScale, maxScale), minScale);
      if (enabled) {
        if (!keepOrigin) {
          if (!hasScrollbars(container)) {
            const x0 = bounds.x != null ? Math.floor(view.translate.x - bounds.x / originalScale + border / newScale + margin / 2) : border;
            const y0 = bounds.y != null ? Math.floor(view.translate.y - bounds.y / originalScale + border / newScale + margin / 2) : border;
            view.scaleAndTranslate(newScale, x0, y0);
          } else {
            view.setScale(newScale);
            const newBounds = this.graph.getGraphBounds();
            if (newBounds.x != null) {
              container.scrollLeft = newBounds.x;
            }
            if (newBounds.y != null) {
              container.scrollTop = newBounds.y;
            }
          }
        } else if (view.scale != newScale) {
          view.setScale(newScale);
        }
      } else {
        return newScale;
      }
      return view.scale;
    }
    /**
     * Fit and center the graph within its container.
     *
     * @param options Optional options to customize the fit behavior.
     * @returns The current scale in the view.
     */
    fitCenter(options) {
      var _a2, _b;
      const margin = (_a2 = options == null ? void 0 : options.margin) != null ? _a2 : 2;
      const { container, view } = this.graph;
      const clientWidth = container.clientWidth - 2 * margin;
      const clientHeight = container.clientHeight - 2 * margin;
      const bounds = this.graph.getGraphBounds();
      const originalScale = view.scale;
      const width = bounds.width / originalScale;
      const height = bounds.height / originalScale;
      let newScale = Math.min((_b = this.maxFitScale) != null ? _b : Infinity, clientWidth / width, clientHeight / height);
      if (!Number.isFinite(newScale)) {
        newScale = originalScale;
      }
      const translateX = Math.floor(view.translate.x + (container.clientWidth - width * newScale) / (2 * newScale) - bounds.x / originalScale);
      const translateY = Math.floor(view.translate.y + (container.clientHeight - height * newScale) / (2 * newScale) - bounds.y / originalScale);
      newScale = keep2digits(newScale);
      view.scaleAndTranslate(newScale, translateX, translateY);
      return newScale;
    }
    /** Do nothing here. */
    onDestroy() {
    }
  };
  FitPlugin.pluginId = "fit";

  // node_modules/@maxgraph/core/lib/esm/view/plugin/ImageBundlePlugin.js
  var ImageBundlePlugin = class {
    /**
     * Constructs the plugin that manages image bundles.
     *
     * @param graph Reference to the enclosing {@link AbstractGraph}. Accepted to conform with the
     *   {@link GraphPluginConstructor} contract; not retained because this plugin does not interact
     *   with the graph directly.
     */
    constructor(graph) {
      this.imageBundles = [];
    }
    /**
     * Adds the specified {@link ImageBundle}.
     */
    addImageBundle(bundle) {
      this.imageBundles.push(bundle);
    }
    /**
     * Removes all occurrences of the specified {@link ImageBundle}.
     */
    removeImageBundle(bundle) {
      this.imageBundles = this.imageBundles.filter((b) => b !== bundle);
    }
    /**
     * Searches all {@link imageBundles} for the specified key and returns the value for the first match or
     * `null` if the key is not found.
     */
    getImageFromBundles(key) {
      if (key) {
        for (const bundle of this.imageBundles) {
          const image = bundle.getImage(key);
          if (image) {
            return image;
          }
        }
      }
      return null;
    }
    /** Releases the registered bundles to help garbage collection. */
    onDestroy() {
      this.imageBundles = [];
    }
  };
  ImageBundlePlugin.pluginId = "image-bundle";

  // node_modules/@maxgraph/core/lib/esm/view/plugin/index.js
  var getDefaultPlugins = () => [
    CellEditorHandler_default,
    TooltipHandler_default,
    SelectionCellsHandler_default,
    PopupMenuHandler_default,
    ConnectionHandler_default,
    SelectionHandler_default,
    PanningHandler_default,
    FitPlugin,
    ImageBundlePlugin
  ];

  // node_modules/@maxgraph/core/lib/esm/view/Graph.js
  var Graph = class extends AbstractGraph {
    /**
     * Creates a new {@link CellRenderer} to be used in this graph.
     */
    createCellRenderer() {
      return new CellRenderer_default();
    }
    /**
     * Creates a new {@link GraphDataModel} to be used in this graph.
     */
    createGraphDataModel() {
      return new GraphDataModel_default();
    }
    /**
     * Creates a new {@link GraphView} to be used in this graph.
     */
    createGraphView() {
      return new GraphView_default(this);
    }
    /**
     * Creates a new {@link GraphSelectionModel} to be used in this graph.
     */
    createSelectionModel() {
      return new GraphSelectionModel_default(this);
    }
    /**
     * Creates a new {@link Stylesheet} to be used in this graph.
     */
    createStylesheet() {
      return new Stylesheet();
    }
    // Register all builtins provided by maxGraph
    registerDefaults() {
      registerDefaultEdgeMarkers();
      registerDefaultEdgeStyles();
      registerDefaultPerimeters();
      registerDefaultShapes();
    }
    // Build cellRenderer/selectionModel/view via the factory methods so subclasses can customize them
    // by overriding. Only `model` and `stylesheet` are accepted from options because Graph's positional
    // constructor doesn't expose the others.
    initializeCollaborators(options) {
      var _a2, _b;
      this.cellRenderer = this.createCellRenderer();
      this.model = (_a2 = options == null ? void 0 : options.model) != null ? _a2 : this.createGraphDataModel();
      this.setSelectionModel(this.createSelectionModel());
      this.setStylesheet((_b = options == null ? void 0 : options.stylesheet) != null ? _b : this.createStylesheet());
      this.view = this.createGraphView();
    }
    constructor(container, model, plugins = getDefaultPlugins(), stylesheet) {
      super({ container, model, plugins, stylesheet: stylesheet != null ? stylesheet : void 0 });
    }
  };

  // src/main/frontend/domui-maxgraph.ts
  var instances = /* @__PURE__ */ new Map();
  function create(id, options = {}) {
    const container = document.getElementById(id);
    if (null == container) {
      console.error("DomUIMaxGraph: no element with id '" + id + "'");
      return;
    }
    destroy(id);
    if (options.imageBase) {
      Client_default.setImageBasePath(options.imageBase);
    }
    InternalEvent_default.disableContextMenu(container);
    const graph = new Graph(container);
    readOnly(graph);
    const instance = { graph, version: -1, cellById: /* @__PURE__ */ new Map() };
    instances.set(id, instance);
    WebUI.jsoncall(id, {}, (response) => {
      if (instances.get(id) !== instance) {
        return;
      }
      build(instance, response);
    });
  }
  function destroy(id) {
    const instance = instances.get(id);
    if (void 0 === instance) {
      return;
    }
    instance.graph.destroy();
    instances.delete(id);
  }
  function graphFor(id) {
    var _a2;
    return (_a2 = instances.get(id)) == null ? void 0 : _a2.graph;
  }
  function readOnly(graph) {
    graph.setCellsEditable(false);
    graph.setCellsMovable(false);
    graph.setCellsResizable(false);
    graph.setCellsDeletable(false);
    graph.setCellsBendable(false);
    graph.setConnectable(false);
    graph.setDropEnabled(false);
  }
  function build(instance, doc) {
    var _a2;
    const graph = instance.graph;
    instance.version = doc.version;
    instance.cellById.clear();
    graph.setPanning(((_a2 = doc.options) == null ? void 0 : _a2.panning) !== false);
    zoomOnCtrlWheel(graph);
    graph.batchUpdate(() => {
      var _a3;
      for (const cell of (_a3 = doc.cells) != null ? _a3 : []) {
        if ("node" === cell.kind) {
          addNode(instance, cell);
        } else {
          addEdge(instance, cell);
        }
      }
    });
  }
  function addNode(instance, doc) {
    var _a2, _b, _c, _d, _e, _f;
    const parent = void 0 === doc.parent ? void 0 : instance.cellById.get(doc.parent);
    const cell = instance.graph.insertVertex({
      id: doc.id,
      parent,
      value: (_a2 = doc.label) != null ? _a2 : "",
      position: [(_b = doc.x) != null ? _b : 0, (_c = doc.y) != null ? _c : 0],
      size: [(_d = doc.w) != null ? _d : 0, (_e = doc.h) != null ? _e : 0],
      style: (_f = doc.style) != null ? _f : {}
    });
    instance.cellById.set(doc.id, cell);
  }
  function addEdge(instance, doc) {
    var _a2, _b;
    const cell = instance.graph.insertEdge({
      id: doc.id,
      source: void 0 === doc.source ? null : instance.cellById.get(doc.source),
      target: void 0 === doc.target ? null : instance.cellById.get(doc.target),
      value: (_a2 = doc.label) != null ? _a2 : "",
      style: (_b = doc.style) != null ? _b : {}
    });
    const points = doc.points;
    if (void 0 !== points && points.length > 0) {
      const geometry = cell.getGeometry();
      if (null != geometry) {
        geometry.points = points.map((p) => new Point_default(p[0], p[1]));
      }
    }
    instance.cellById.set(doc.id, cell);
  }
  function zoomOnCtrlWheel(graph) {
    const container = graph.container;
    if (null == container) {
      return;
    }
    InternalEvent_default.addMouseWheelListener((event, up) => {
      const wheel = event;
      if (!wheel.ctrlKey && !wheel.metaKey) {
        return;
      }
      if (!container.contains(wheel.target)) {
        return;
      }
      if (up) {
        graph.zoomIn();
      } else {
        graph.zoomOut();
      }
      InternalEvent_default.consume(event);
    }, container);
  }
  return __toCommonJS(domui_maxgraph_exports);
})();
