package com.beantalk.util;

import com.beantalk.model.Group;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Groups
 */
public class GroupDAO {

    /**
     * Tạo group mới
     */
    public static Integer createGroup(String groupName, int createdBy) {
        String sql = "INSERT INTO Groups (group_name, created_by, created_at) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, groupName);
            stmt.setInt(2, createdBy);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating group: " + e.getMessage());
        }
        return null;
    }

    /**
     * Thêm member vào group
     */
    public static boolean addGroupMember(int groupId, int userId) {
        String sql = "INSERT INTO GroupMembers (group_id, user_id, joined_at) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, groupId);
            stmt.setInt(2, userId);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding group member: " + e.getMessage());
        }
        return false;
    }

    /**
     * Lấy danh sách groups của user
     */
    public static List<Group> getUserGroups(int userId) {
        String sql = "SELECT g.* FROM Groups g " +
                "INNER JOIN GroupMembers gm ON g.group_id = gm.group_id " +
                "WHERE gm.user_id = ? " +
                "ORDER BY g.created_at DESC";

        List<Group> groups = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                groups.add(new Group(
                        rs.getInt("group_id"),
                        rs.getString("group_name"),
                        rs.getInt("created_by"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting user groups: " + e.getMessage());
        }
        return groups;
    }

    /**
     * Lấy danh sách members trong group
     */
    public static List<String> getGroupMembers(int groupId) {
        String sql = "SELECT u.username FROM Users u " +
                "INNER JOIN GroupMembers gm ON u.user_id = gm.user_id " +
                "WHERE gm.group_id = ? " +
                "ORDER BY u.username";

        List<String> members = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                members.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting group members: " + e.getMessage());
        }
        return members;
    }

    /**
     * Cập nhật tên group
     */
    public static boolean updateGroupName(int groupId, String newName) {
        String sql = "UPDATE Groups SET group_name = ? WHERE group_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newName);
            stmt.setInt(2, groupId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating group name: " + e.getMessage());
        }
        return false;
    }

    /**
     * Lấy thông tin group
     */
    public static Group getGroupById(int groupId) {
        String sql = "SELECT * FROM Groups WHERE group_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Group(
                        rs.getInt("group_id"),
                        rs.getString("group_name"),
                        rs.getInt("created_by"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting group: " + e.getMessage());
        }
        return null;
    }

    /**
     * Xóa member khỏi group
     */
    public static boolean removeGroupMember(int groupId, int userId) {
        String sql = "DELETE FROM GroupMembers WHERE group_id = ? AND user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, groupId);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error removing group member: " + e.getMessage());
        }
        return false;
    }

    /**
     * Kiểm tra user có phải member của group không
     */
    public static boolean isMember(int groupId, int userId) {
        String sql = "SELECT COUNT(*) FROM GroupMembers WHERE group_id = ? AND user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, groupId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking membership: " + e.getMessage());
        }
        return false;
    }

    /**
     * Lấy tất cả user IDs trong group
     */
    public static List<Integer> getGroupMemberIds(int groupId) {
        String sql = "SELECT user_id FROM GroupMembers WHERE group_id = ? ORDER BY joined_at";
        List<Integer> memberIds = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                memberIds.add(rs.getInt("user_id"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting group member IDs: " + e.getMessage());
        }
        return memberIds;
    }
}