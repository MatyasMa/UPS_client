# Nastavení proměnných
SRC_DIR = src
BIN_DIR = bin
JAR_FILE = client.jar
MAIN_CLASS = Client

# Seznam všech Java souborů
SOURCES = $(wildcard $(SRC_DIR)/**/*.java $(SRC_DIR)/*.java)

# Překlad všech Java souborů do class ve složce bin
CLASSES = $(patsubst $(SRC_DIR)/%.java,$(BIN_DIR)/%.class,$(SOURCES))

# Výchozí cíl
.PHONY: all
all: $(JAR_FILE)

# Kompilace do class
# TODO: nemusím vytvářet složku -p, při mazání smazat
$(BIN_DIR)/%.class: $(SRC_DIR)/%.java
	mkdir -p $(BIN_DIR)
	javac -d $(BIN_DIR) -sourcepath $(SRC_DIR) $<

# Vytvoření JAR souboru
$(JAR_FILE): $(CLASSES)
	jar cfe $(JAR_FILE) $(MAIN_CLASS) -C $(BIN_DIR) .

# Debugging - výpis zdrojů a tříd
.PHONY: debug
debug:
	@echo "SOURCES: $(SOURCES)"
	@echo "CLASSES: $(CLASSES)"

# Spuštění aplikace
.PHONY: run
run: $(JAR_FILE)
	java -jar $(JAR_FILE)

# Vyčištění
.PHONY: clean
clean:
	rm -rf $(BIN_DIR) $(JAR_FILE)
