if objVar("player", "x") < -4175 then 
objVar("player", "pos", {-4115, -270, -5535})
objVar("mistress", "pos", {-3231, -270, -6615})
objVar("mistress", "rotY", 180)
setMusicPitch(0.7) 
else
objVar("player", "pos", {-4245, -270, -5535})
setMusicPitch(1)
end