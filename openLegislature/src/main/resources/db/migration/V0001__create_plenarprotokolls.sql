CREATE TABLE plenarprotokolls (
	id serial primary key,
	proceeding integer,
	periode integer,
	unique (proceeding, periode)
);