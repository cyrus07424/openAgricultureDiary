package models;

import io.ebean.Model;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Column;

import java.time.LocalDateTime;

@MappedSuperclass
public class BaseModel extends Model {
   @Id
   private Long id;

   @Column(name = "created_at")
   private LocalDateTime createdAt;

   @Column(name = "updated_at")
   private LocalDateTime updatedAt;

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public LocalDateTime getCreatedAt() {
      return createdAt;
   }

   public void setCreatedAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
   }

   public LocalDateTime getUpdatedAt() {
      return updatedAt;
   }

   public void setUpdatedAt(LocalDateTime updatedAt) {
      this.updatedAt = updatedAt;
   }

   @Override
   public void save() {
      if (createdAt == null) {
         createdAt = LocalDateTime.now();
      }
      updatedAt = LocalDateTime.now();
      super.save();
   }

   @Override
   public void update() {
      updatedAt = LocalDateTime.now();
      super.update();
   }
}
