playSource("tv switch")
local active = not objVar("ghost on tv mesh", "visible")

objVar("ghost on tv mesh", "visible", active)
setMusicPitch(active and 0.5 or 1);
objVar("ghost on tv", "pos", active and {230,145,150} or {0,-999999,0})
objVar("tv light", "color", active and 140 or 0)

objVar("tv static_1", "volume", active and 0.3 or 0)
objVar("tv static_2", "volume", active and 0.3 or 0)
objVar("mumbling", "volume", active and 0.3 or 0)