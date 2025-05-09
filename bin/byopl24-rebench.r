#!/usr/bin/env Rscript

# Load necessary library
suppressPackageStartupMessages(library(dplyr))
suppressPackageStartupMessages(library(ggplot2))
suppressPackageStartupMessages(library(gridExtra))
suppressPackageStartupMessages(library(grid))  # Add this line
suppressPackageStartupMessages(library(RColorBrewer))  # Add this line

# Load the data, skipping the first line and comments
data <- read.table("benchmark.data", header = TRUE, comment.char = "#", skip = 1, fill = TRUE)

# Rename the executor column to project
data <- data %>% rename(project = executor)

# Ensure data is sorted by iteration within each group
data_sorted <- data %>%
  group_by(benchmark, project) %>%
  arrange(iteration)

# Filter out the first iteration for each benchmark and project
data_filtered <- data_sorted %>%
  group_by(benchmark, project) %>%
  slice(-1) # remove warmup

# Calculate average, geometric mean, median, and standard deviation for each benchmark run and project
summary <- data_filtered %>%
  group_by(benchmark, project) %>%
  summarise(
    avg_value = mean(value),
    geo_mean = exp(mean(log(value))),
    median_value = median(value),
    std_dev = sd(value),
    .groups = 'drop'
  )

# Rank projects within each benchmark
summary <- summary %>%
  group_by(benchmark) %>%
  mutate(rank = rank(avg_value)) %>%
  ungroup()

# Calculate total rank for each project across all benchmarks
total_ranking <- summary %>%
  group_by(project) %>%
  summarise(total_rank = sum(rank)) %>%
  arrange(total_rank)

# Print the summary
print(summary)

# Print the total ranking
print(total_ranking)

# Output the summary as a CSV file with column names and comma as the delimiter
write.csv(summary, "summary.csv", row.names = FALSE)

# Output the total ranking as a CSV file
write.csv(total_ranking, "total_ranking.csv", row.names = FALSE)

# Generate a PDF with a table and plot for each benchmark on one page
timestamp <- format(Sys.time(), "%Y%m%d%H%M%S")
# pdf_filename <- paste0("summary_", timestamp, ".pdf")
pdf_filename <- paste0("summary.pdf")
pdf(pdf_filename, height = 11, width = 8.5)


# Create and print a table and plot for each benchmark
benchmarks <- unique(summary$benchmark)
for (benchmark in benchmarks) {
  plot_data <- summary %>% filter(benchmark == !!benchmark)
  
  # Create the plot for the benchmark
  p <- ggplot(plot_data, aes(x = project, y = avg_value, fill = project)) +
    geom_bar(stat = "identity") +
    geom_errorbar(aes(ymin = avg_value - std_dev, ymax = avg_value + std_dev), width = 0.2) +
    ggtitle(paste("Benchmark:", benchmark, "(lower is better)")) +
    xlab("Project") +
    ylab("Average Value (microseconds)") +  # Add unit to y-axis label
    theme(plot.margin = unit(c(1, 1, 1, 1), "cm"), axis.text.x = element_text(angle = 20, hjust = 1)) +
    scale_fill_manual(values = colorRampPalette(brewer.pal(12, "Set3"))(17)) # +
    # scale_y_log10()  # Add log scale for y-axis
  
  # Create a title for the table
  table_title <- textGrob(paste("Summary for ", benchmark), gp = gpar(fontsize = 14, fontface = "bold"))
  
  # Arrange the table and plot on one page with the plot taking half the page height
  table_grob <- gridExtra::tableGrob(plot_data)
  table_grob <- gridExtra::arrangeGrob(table_title, table_grob, ncol = 1, heights = c(0.1, 0.9))
  plot_grob <- ggplotGrob(p)
  combined_grob <- gridExtra::arrangeGrob(table_grob, plot_grob, ncol = 1, heights = c(1, 1))
  grid::grid.draw(combined_grob)
  
  # Add a new page for the next benchmark
  grid::grid.newpage()
}


# Create a final page with the total ranking
total_ranking_grob <- gridExtra::tableGrob(total_ranking)
grid::grid.draw(total_ranking_grob)

dev.off()
