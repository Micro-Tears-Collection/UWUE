local catch = grassyFall == true
grassyFall = nil
local fall = {"Mistress:*I caught you when you fell off the roof, thankfully you weren't hurt.*Be more careful in the future, okay?"}

local catchF = function ()
	showDialog(fall[1])
end

--objVar("mistress", "rotY", atan2(objVar("mistress", "pos"), objVar("player", "pos")))
lookAt("mistress", "player")
if not save.grassyMeetMistress then
	local text = "Woman:*Hello, I am the mistress of this city, I take care of it and its inhabitants.*I give shelter to those who are in need, if someone falls down from the rooftops, then I catch them and bring here so that they can recover.*You can come to this apartment at any time!";
	if catch then text = text.."@"..fall[1] end
	
	showDialog(text)
	save.grassyMeetMistress = true
else 
	if catch then 
		catchF()
	else
		loadDialog("/maps/grassy/mistress-help.txt")
	end
end