stopMusic()
stopSource({"tv_static_1", "tv_static_2", "mumbling"})
objVar("closet_trigger", "activable", false)

pause(2000, function()  
	playSource("closet_snd")
	objVar("closet_open", "visible", true)
	objVar("closet_closed", "visible", false)
end)

pause(2000, function()  
	loadMap("pike", {fade={color=0, time=1500}})
end)