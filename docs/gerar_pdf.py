import re
from fpdf import FPDF

SRC = r"C:\Users\Guilherme\OneDrive - SPTech School\4 Semestre\Backend\hashmap\docs\parte1-pesquisa.md"
OUT = r"C:\Users\Guilherme\OneDrive - SPTech School\4 Semestre\Backend\hashmap\docs\parte1-pesquisa.pdf"
FONT = r"C:\Windows\Fonts\arial.ttf"

text = open(SRC, encoding="utf-8").read()

lines = []
for raw in text.splitlines():
    line = raw.rstrip()
    if not line.strip():
        lines.append(("blank", ""))
        continue
    if line.startswith("# "):
        lines.append(("title", line[2:]))
    elif line.startswith("## "):
        lines.append(("h1", line[3:]))
    elif line.startswith("### "):
        lines.append(("h2", line[4:]))
    elif line.startswith("---"):
        lines.append(("hr", ""))
    elif re.match(r"^\d+\.\s", line):
        lines.append(("li", re.sub(r"^\d+\.\s", "", line)))
    elif line.startswith("- "):
        lines.append(("li", line[2:]))
    elif line.startswith("|"):
        continue
    elif line.startswith("```"):
        lines.append(("blank", ""))
    else:
        lines.append(("p", line))


def clean(s):
    s = re.sub(r"\*\*(.+?)\*\*", r"\1", s)
    s = re.sub(r"\*(.+?)\*", r"\1", s)
    s = re.sub(r"`(.+?)`", r"\1", s)
    s = re.sub(r"\[(.+?)\]\((.+?)\)", r"\1 (\2)", s)
    words = []
    for w in s.split(" "):
        while len(w) > 45:
            words.append(w[:45])
            w = w[45:]
        words.append(w)
    return " ".join(words)


pdf = FPDF()
pdf.add_page()
pdf.add_font("arial", "", FONT)
pdf.add_font("arial", "B", FONT)
pdf.set_auto_page_break(True, margin=15)

for kind, content in lines:
    content = clean(content)
    if kind == "title":
        pdf.set_font("arial", "B", 16)
        pdf.multi_cell(0, 9, content, new_x="LMARGIN", new_y="NEXT")
        pdf.ln(2)
    elif kind == "h1":
        pdf.ln(4)
        pdf.set_font("arial", "B", 13)
        pdf.multi_cell(0, 7, content, new_x="LMARGIN", new_y="NEXT")
        pdf.ln(1)
    elif kind == "h2":
        pdf.ln(2)
        pdf.set_font("arial", "B", 11)
        pdf.multi_cell(0, 6, content, new_x="LMARGIN", new_y="NEXT")
    elif kind == "hr":
        pdf.ln(2)
    elif kind == "li":
        pdf.set_font("arial", "", 10)
        pdf.set_x(pdf.l_margin + 6)
        pdf.multi_cell(0, 5, "- " + content, new_x="LMARGIN", new_y="NEXT")
    elif kind == "blank":
        pdf.ln(3)
    else:
        pdf.set_font("arial", "", 10)
        pdf.multi_cell(0, 5, content, new_x="LMARGIN", new_y="NEXT")

pdf.output(OUT)
print("PDF gerado:", OUT)
