playSource("tv_switch")
local active = not objVar("ghost_on_tv_mesh", "visible")

objVar("ghost_on_tv_mesh", "visible", active)
setMusicPitch(active and 0.5 or 1);
objVar("ghost_on_tv", "activable", active)
objVar("tv_light", "color", active and 140 or 0)

objVar("tv_static_1", "volume", active and 0.3 or 0)
objVar("tv_static_2", "volume", active and 0.3 or 0)
objVar("mumbling", "volume", active and 0.3 or 0)