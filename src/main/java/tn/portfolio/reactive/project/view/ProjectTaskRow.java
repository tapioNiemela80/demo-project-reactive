package tn.portfolio.reactive.project.view;

import java.util.UUID;

record ProjectTaskRow(UUID id,
                      String name,
                      String description,
                      Integer projectEstimateHours,
                      Integer projectEstimateMinutes,
                      String projectStatus,
                      String contactPersonEmail,

                      UUID taskId,
                      String taskTitle,
                      String taskDescription,
                      String taskStatus,
                      Integer taskEstimateHours,
                      Integer taskEstimateMinutes,
                      Integer actualHours,
                      Integer actualMinutes) {
}
