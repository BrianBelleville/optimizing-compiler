DOT := dot
DOTFLAGS := -Tsvg
SOURCES := $(wildcard *.gv)
OUTPUT := $(patsubst %.gv, %.svg, $(SOURCES))

all: $(OUTPUT)

%.svg : %.gv
	$(DOT) $(DOTFLAGS) $< -o $@

clean:
	rm -f $(OUTPUT)

.PHONY: default
