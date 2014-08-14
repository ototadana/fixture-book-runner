var fso = new ActiveXObject("Scripting.FileSystemObject");
var a = fso.CreateTextFile("test03.txt", true);
a.WriteLine("test03:" + WScript.Arguments(0));
a.Close();
