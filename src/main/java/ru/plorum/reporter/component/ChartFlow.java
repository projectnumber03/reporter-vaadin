package ru.plorum.reporter.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.logging.log4j.util.Strings;

import java.util.List;
import java.util.stream.Collectors;

@Tag("canvas")
@JsModule("./js/chart.js")
public class ChartFlow extends Component {

    public ChartFlow(final String id, final Type type, final List<String> labels, final List<String> data, final String label) {
        getElement().setProperty("id", id);
        final var script = """
                const ctx = document.getElementById('%s');
                                
                  new Chart(ctx, {
                    type: '%s',
                    data: {
                      labels: [%s],
                      datasets: [{
                        label: '%s',
                        data: [%s],
                        borderWidth: 1
                      }]
                    },
                    options: {
                      scales: {
                        y: {
                          beginAtZero: true
                        }
                      }
                    }
                  });
                """;
        final var labelsString = labels.stream().map(Strings::quote).collect(Collectors.joining(","));
        final var dataString = data.stream().map(Strings::quote).collect(Collectors.joining(","));
        final var formattedScript = String.format(script, id, type.name, labelsString, label, dataString);
        getElement().executeJs(formattedScript);
    }

    @Getter
    @AllArgsConstructor
    public enum Type {
        BAR("bar", "Шкала"),
        DOUGHNUT("doughnut", "Пончик"),
        PIE("pie", "Пирог"),
        LINE("line", "Линия"),
        POLAR_AREA("polarArea", "Полярная область")
        ;

        private final String name;

        private final String description;

    }

}
