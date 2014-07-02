CREATE TABLE speakers (
	id serial primary key,
	name text unique not null
);

CREATE TABLE speakers_parties (
	speaker integer references speakers(id),
	party integer references parties(id),
	unique (speaker, party)
);

CREATE TABLE speakers_public_offices (
	speaker integer references speakers(id),
	public_office integer references public_offices(id),
	unique (speaker, public_office)
);