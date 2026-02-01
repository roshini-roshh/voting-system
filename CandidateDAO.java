package dao;

import models.Candidate;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class for managing Candidate data
 */
public class CandidateDAO {

    /**
     * Register a new candidate
     */
    public boolean registerCandidate(Candidate candidate) {
        String sql = "INSERT INTO candidates (rollno, name, dept, symbol_filename, photo_path, description_path, is_approved, vote_count) " +
                "VALUES (?, ?, ?, ?, ?, ?, FALSE, 0)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, candidate.getRollNumber());
            ps.setString(2, candidate.getFullName());
            ps.setString(3, candidate.getDepartment());
            ps.setString(4, candidate.getSymbolFilename());
            ps.setString(5, candidate.getPhotoPath());
            ps.setString(6, candidate.getDescriptionPath());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) candidate.setCandidateId(rs.getInt(1));
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error registering candidate: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get all candidates
     */
    public List<Candidate> getAllCandidates() {
        List<Candidate> list = new ArrayList<>();
        String sql = "SELECT * FROM candidates ORDER BY name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) list.add(extractCandidate(rs));

        } catch (SQLException e) {
            System.err.println("❌ Error fetching candidates: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get a candidate by ID (required for voting confirmation)
     */
    public Candidate getCandidateById(int candidateId) {
        String sql = "SELECT * FROM candidates WHERE candidate_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, candidateId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return extractCandidate(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching candidate by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all pending candidates
     */
    public List<Candidate> getPendingCandidates() {
        List<Candidate> list = new ArrayList<>();
        String sql = "SELECT * FROM candidates WHERE is_approved = FALSE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(extractCandidate(rs));

        } catch (SQLException e) {
            System.err.println("❌ Error fetching pending candidates: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get all approved candidates
     */
    public List<Candidate> getApprovedCandidates() {
        List<Candidate> list = new ArrayList<>();
        String sql = "SELECT * FROM candidates WHERE is_approved = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(extractCandidate(rs));

        } catch (SQLException e) {
            System.err.println("❌ Error fetching approved candidates: " + e.getMessage());
        }
        return list;
    }

    /**
     * Approve a candidate
     */
    public boolean approveCandidate(int candidateId) {
        String sql = "UPDATE candidates SET is_approved = TRUE WHERE candidate_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, candidateId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error approving candidate: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a candidate
     */
    public boolean deleteCandidate(int candidateId) {
        String sql = "DELETE FROM candidates WHERE candidate_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, candidateId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error deleting candidate: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update candidate info
     */
    public boolean updateCandidate(Candidate candidate) {
        String sql = "UPDATE candidates SET name = ?, dept = ?, photo_path = ?, description_path = ? WHERE candidate_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, candidate.getFullName());
            ps.setString(2, candidate.getDepartment());
            ps.setString(3, candidate.getPhotoPath());
            ps.setString(4, candidate.getDescriptionPath());
            ps.setInt(5, candidate.getCandidateId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error updating candidate: " + e.getMessage());
            return false;
        }
    }

    /**
     * Increase vote count by 1 (CRITICAL FOR VOTING)
     */
    public boolean incrementVoteCount(int candidateId) {
        String sql = "UPDATE candidates SET vote_count = vote_count + 1 WHERE candidate_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, candidateId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error incrementing vote count: " + e.getMessage());
        }
        return false;
    }

    /**
     * Extract candidate object from ResultSet
     */
    private Candidate extractCandidate(ResultSet rs) throws SQLException {
        Candidate c = new Candidate();
        c.setCandidateId(rs.getInt("candidate_id"));
        c.setRollNumber(rs.getString("rollno"));
        c.setFullName(rs.getString("name"));
        c.setDepartment(rs.getString("dept"));
        c.setSymbolFilename(rs.getString("symbol_filename"));
        c.setPhotoPath(rs.getString("photo_path"));
        c.setDescriptionPath(rs.getString("description_path"));
        c.setApproved(rs.getBoolean("is_approved"));
        c.setVoteCount(rs.getInt("vote_count"));
        return c;
    }
}
