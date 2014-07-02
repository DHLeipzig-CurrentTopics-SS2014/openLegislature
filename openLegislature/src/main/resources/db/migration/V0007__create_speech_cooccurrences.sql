CREATE TABLE speech_cooccurrences (
	id serial primary key,
	token integer references tokens(id),
	cooccurrence integer references tokens(id),
	count integer default 1,
	speech integer references speeches(id)
);