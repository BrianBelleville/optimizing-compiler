DOT := dot
DOTFLAGS := -Tpng
SOURCES := $(wildcard *.gv)
OUTPUT := $(patsubst %.gv, %.png, $(SOURCES))

all: $(OUTPUT)

%.png : %.gv
	$(DOT) $(DOTFLAGS) $< -o $@

clean:
	rm -f $(OUTPUT)

.PHONY: default
