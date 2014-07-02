CREATE TABLE speeches (
	id serial primary key,
	speech text not null,
	speaker integer references speakers(id)
);