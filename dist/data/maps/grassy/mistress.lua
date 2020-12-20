local catch = objVar("mistress", "x") > -2600
local fall = {"Mistress:*I caught you when you fell off the roof, thankfully you weren't hurt.*Be more careful in the future, okay?"}

local catchF = function ()
	showDialog(fall[1])
end

if not save.grassyMeetMistress then
	local text = "Woman:*Hello, I am the mistress of this city, I take care of it and its inhabitants.*I give shelter to those who are in need, if someone falls down from the rooftops, then I catch them and bring here so that they can recover.*You can come to this apartment at any time!";
	if catch then text = text.."@"..fall[1] end
	
	showDialog(text)
	save.grassyMeetMistress = true
else 
	if catch then 
		catchF()
	else
		showDialog("Mistress:*Do you need help?")
	end
end