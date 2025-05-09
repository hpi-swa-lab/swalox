library(tidyverse)
library(knitr)
library(readr)
library(ggplot2)

# Read the benchmark data
benchmark_data <- read_delim(
  "benchmark.data",
  delim = "\t",
  comment = "#",
  col_names = TRUE
)

# Clean and process the data
benchmark_data <- benchmark_data %>%
  mutate(benchmark = as.factor(benchmark),
         value = as.numeric(value),
         iteration = as.integer(iteration))

# Create a summary table
summary_table <- benchmark_data %>%
  group_by(benchmark) %>%
  summarise(
    Min = min(value),
    Q1 = quantile(value, 0.25),
    Median = median(value),
    Mean = mean(value),
    Q3 = quantile(value, 0.75),
    Max = max(value),
    .groups = 'drop'
  )

# Print the summary table
kable(summary_table, caption = "Summary of Benchmark Results")

# Create a box plot
box_plot <- ggplot(benchmark_data, aes(x = benchmark, y = value, fill = benchmark)) +
  geom_boxplot() +
  labs(
    title = "Benchmark Results by Benchmark",
    x = "Benchmark",
    y = "Execution Time (ms)"
  ) +
  theme_minimal() +
  theme(legend.position = "none")

# Save the plot
ggsave("box_plot.png", plot = box_plot, width = 10, height = 6)

# Display the box plot
print(box_plot)