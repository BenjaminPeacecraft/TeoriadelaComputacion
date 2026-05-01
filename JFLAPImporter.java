package TeriaComputacion.io;

import TeriaComputacion.modelo.AFD;
import TeriaComputacion.modelo.State;
import TeriaComputacion.modelo.Transition;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.awt.Point;
import java.io.File;
import java.util.*;

/**
  JFLAP XML structura:
  <structure>
    <type>fa</type>
    <automaton>
      <state id="0" name="q0"><x>...</x><y>...</y><initial/><final/></state>
      ...
      <transition><from>0</from><to>1</to><read>a</read></transition>
      ...
    </automaton>
  </structure>
 */
public class JFLAPImporter {

    public static class ImportResult {
        public final AFD afd;
        public final List<String> warnings;
        public final String error;

        public ImportResult(AFD afd, List<String> warnings) {
            this.afd = afd;
            this.warnings = warnings;
            this.error = null;
        }

        public ImportResult(String error) {
            this.afd = null;
            this.warnings = Collections.emptyList();
            this.error = error;
        }

        public boolean isSuccess() { return error == null; }
    }

    public ImportResult importFile(File file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();
            NodeList typeNodes = doc.getElementsByTagName("type");
            if (typeNodes.getLength() > 0) {
                String type = typeNodes.item(0).getTextContent().trim().toLowerCase();
                if (!type.equals("fa")) {
                    return new ImportResult("El archivo JFLAP no contiene un autómata finito (type='" + type + "'). Solo se soporta type='fa'.");
                }
            }

            List<String> warnings = new ArrayList<>();
            AFD afd = new AFD(file.getName().replace(".jff", ""));
            Map<String, State> stateById = new LinkedHashMap<>();
            NodeList stateNodes = doc.getElementsByTagName("state");
            for (int i = 0; i < stateNodes.getLength(); i++) {
                Element el = (Element) stateNodes.item(i);
                String id   = el.getAttribute("id");
                String name = el.getAttribute("name");
                int x = 100, y = 100;
                NodeList xs = el.getElementsByTagName("x");
                NodeList ys = el.getElementsByTagName("y");
                if (xs.getLength() > 0) x = (int) Double.parseDouble(xs.item(0).getTextContent().trim());
                if (ys.getLength() > 0) y = (int) Double.parseDouble(ys.item(0).getTextContent().trim());

                boolean isInitial = el.getElementsByTagName("initial").getLength() > 0;
                boolean isFinal   = el.getElementsByTagName("final").getLength() > 0;

                State s = new State(id, name, isInitial, isFinal, new Point(x, y));
                stateById.put(id, s);
                afd.addState(s);
            }

            NodeList transNodes = doc.getElementsByTagName("transition");
            for (int i = 0; i < transNodes.getLength(); i++) {
                Element el = (Element) transNodes.item(i);
                String fromId = el.getElementsByTagName("from").item(0).getTextContent().trim();
                String toId   = el.getElementsByTagName("to").item(0).getTextContent().trim();
                NodeList readNodes = el.getElementsByTagName("read");
                String symbol = "";
                if (readNodes.getLength() > 0) {
                    symbol = readNodes.item(0).getTextContent().trim();
                }

                State from = stateById.get(fromId);
                State to   = stateById.get(toId);

                if (from == null || to == null) {
                    warnings.add("Transición ignorada: estado no encontrado (from=" + fromId + ", to=" + toId + ")");
                    continue;
                }

                if (symbol.isEmpty()) {
                    warnings.add("Transición epsilon detectada desde '" + from.getName() + "' a '" + to.getName() + "'. Esto podría no ser un AFD puro.");
                    symbol = "ε";
                }

                afd.addTransition(new Transition(from, to, symbol));
            }
            AFD.ValidationResult val = afd.validate();
            if (!val.valid) {
                for (String e : val.errors) warnings.add("Advertencia de validación: " + e);
            }

            return new ImportResult(afd, warnings);

        } catch (Exception e) {
            return new ImportResult("Error al leer el archivo JFLAP: " + e.getMessage());
        }
    }
}
