from tkinter import Tk

r = Tk()
r.withdraw()

while True:
	s = input("Input string: ")
	if (s == ""):
		r.destroy()
		break
	chars = list(s)
	chars = sorted(set(chars))
	str = ''.join(chars)
	print(str)
	r.clipboard_clear()
	r.clipboard_append(str)

