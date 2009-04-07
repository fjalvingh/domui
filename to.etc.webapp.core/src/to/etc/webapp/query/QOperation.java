package to.etc.webapp.query;

public enum QOperation {
	AND,
	OR,
	EQ,
	NE,
	LE,
	GE,
	LT,
	GT,
	ILIKE,
	LIKE,
	BETWEEN,

	PROP,
	PARAM,
	LITERAL,
	ORDER,
	ISNULL,
	ISNOTNULL,
	SQL
}
