#!/usr/bin/env Rscript

# Load necessary libraries
suppressPackageStartupMessages(library(dplyr))
suppressPackageStartupMessages(library(tidyr))
suppressPackageStartupMessages(library(gridExtra))
suppressPackageStartupMessages(library(grid))

# Define the directory containing the result files
results_dir <- "results/"

# Get a list of all .txt files in the directory
txt_files <- list.files(path = results_dir, pattern = "\\.txt$", full.names = TRUE)

# Initialize an empty data frame to store the results
results_summary <- data.frame(
  project = character(),
  test_type = character(),
  passed_count = integer(),
  stringsAsFactors = FALSE
)

# Loop through each file and count the number of "passed" lines for each test type
for (file in txt_files) {
  # Read the file
  lines <- readLines(file)
  
  # Initialize variables
  current_test_type <- NULL
  passed_count <- 0
  
  # Extract project name by removing the "_tests.txt" suffix
  project_name <- gsub("-final_tests\\.txt$", "", basename(file))
  
  # Loop through each line
  for (line in lines) {
    if (grepl("^TEST: ", line)) {
      # If we encounter a new test type, save the previous results
      if (!is.null(current_test_type)) {
        results_summary <- dplyr::bind_rows(results_summary, data.frame(project = project_name, test_type = current_test_type, passed_count = passed_count))
      }
      # Update the current test type and reset the passed count
      current_test_type <- gsub("^TEST: ", "", line)
      passed_count <- 0
    } else if (grepl("^  Pass ", line)) {
      # Increment the passed count for the current test type
      passed_count <- passed_count + 1
    }
  }
  
  # Save the results for the last test type in the file
  if (!is.null(current_test_type)) {
    results_summary <- dplyr::bind_rows(results_summary, data.frame(project = project_name, test_type = current_test_type, passed_count = passed_count))
  }
}

# Create a table with test_type as rows, project as columns, and passed_count as values
results_table <- results_summary %>%
  tidyr::pivot_wider(names_from = project, values_from = passed_count, values_fill = list(passed_count = 0))

# Convert the results table to a data frame for printing
results_df <- as.data.frame(results_table)

# Set the row names to the test_type column and remove the test_type column
rownames(results_df) <- results_df$test_type
results_df <- results_df[, -1]

# Create a table grob with rotated headings and colored cells
reference_col <- "byopl24-00a"
reference_values <- results_df[[reference_col]]

table_grob <- tableGrob(results_df, theme = ttheme_default(
  core = list(
    fg_params = list(cex = 0.8),
    bg_params = list(
      fill = ifelse(results_df != reference_values, "lightpink", "white")
    )
  ),
  colhead = list(fg_params = list(cex = 0.8, rot = 90))
))

# Adjust the table grob to fit on the page
table_grob <- gridExtra::grid.arrange(table_grob, ncol = 1)

# Save the table as a PDF file
pdf("tests.pdf", width = 8, height = 6)
grid.draw(table_grob)
dev.off()

# Write the results to a CSV file
write.csv(results_df, "tests.csv", row.names = TRUE)

# Print the summary table
print(results_df)