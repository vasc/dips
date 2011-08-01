#!/usr/bin/python

from jinja2 import Environment, FileSystemLoader
import yaml
import markdown

from pygments import highlight
from pygments.lexers import CoffeeScriptLexer
from pygments.formatters import HtmlFormatter

class annotated:
	def __init__(self, lines):
		self.code = []
		self.current = None
		self.apply_current()
		for line in lines:
			if line.lstrip().startswith('#'):
				self.append_comment(line.lstrip()[1:])
			else:
				self.append_code(line)
		self.apply_current()


	def apply_current(self):
		if self.current: self.code.append(self.current)
		self.current = { 'type': 'comment', 'value': ([], []) }


	def append_comment(self, line):
		if self.current['type'] == 'code':
			self.apply_current()
		self.current['value'][0].append(line)
		self.current['type'] = 'comment'

	def append_code(self, line):
		self.current['value'][1].append(line)
		self.current['type'] = 'code'

	def get(self):
		return map( lambda x: (''.join(x['value'][0]), ''.join(x['value'][1])), self.code)

def make_text(item):
	if item['type'] == 'code':
		with open(item['path']) as f:
			lines = f.readlines()
			text = annotated(lines).get()
			text = map(lambda x: (markdown.markdown(x[0]), highlight(x[1], CoffeeScriptLexer(), HtmlFormatter())), text)			
			item['annotated_code'] = text
			item['code'] = highlight(''.join(lines), CoffeeScriptLexer(), HtmlFormatter())

	elif item['type'] == 'text':
		with open(item['path']) as f:
			item['html'] = markdown.markdown(f.read())
	return item

def strip_whitespace(value):
	return value.replace(' ', '_')

def make():
	env = Environment(loader=FileSystemLoader('templates'))
	env.filters['sws'] = strip_whitespace

	with open('index.yaml') as f:
		files = yaml.load(f.read())

	values = map(make_text, files)
	template = env.get_template('index.html')
	return template.render(items=values)

def main():
	with open('index.html', 'w') as f:
		f.write(make())

if __name__ == '__main__':
	main()
